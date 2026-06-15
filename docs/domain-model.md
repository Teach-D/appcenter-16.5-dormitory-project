# 도메인 모델

> 기반 요구사항: `docs/requirements.md`
> 기능: 오픈 채팅 이미지 전송

---

## 1. 유비쿼터스 언어 (Ubiquitous Language)

| 용어 | 정의 |
|------|------|
| 이미지 메시지 (Image Message) | `OpenChatMessageType.IMAGE`인 메시지 — content는 빈 문자열, 실제 URL은 Image 테이블에서 조회 |
| 채팅 이미지 (Chat Image) | `ImageType.OPEN_CHAT_MESSAGE`인 Image 레코드 — `entityId`가 메시지 ID를 가리킴 |
| 참여자 검증 (Participant Validation) | 이미지 전송 전 요청자가 해당 채팅방의 `OpenChatParticipant`임을 확인하는 절차 |
| 원자적 저장 (Atomic Save) | 이미지 파일 디스크 저장 → 메시지 DB 저장 → Image 레코드 저장을 하나의 흐름으로 처리, 중간 실패 시 전체 롤백 |

---

## 2. 바운디드 컨텍스트

단일 확장 컨텍스트: `openChat`
- 기존 `openChat` 컨텍스트에 이미지 전송 기능을 추가
- `common/image` 컨텍스트 의존: `ImageService`, `ImageType`, `Image` 엔티티

---

## 3. 애그리거트

### Aggregate: OpenChatMessage (확장)

#### 책임
채팅방 내 메시지의 생성·조회·삭제 일관성을 보호하며, IMAGE 타입 메시지의 이미지 라이프사이클을 관리한다.

#### 애그리거트 루트
`OpenChatMessage`

#### 엔티티 & 값 객체

| 구분 | 이름 | 핵심 속성 | 설명 |
|------|------|-----------|------|
| Entity (기존) | `OpenChatMessage` | id, roomId, senderId, content, type | IMAGE 타입일 때 content = "" |
| Entity (공통) | `Image` | id, imageName, imagePath, imageType, entityId | imageType=OPEN_CHAT_MESSAGE, entityId=messageId |
| Entity (기존) | `OpenChatParticipant` | roomId, userId, lastReadMessageId | 전송 권한 검증용 |

#### 비즈니스 불변식 (Invariants)

- **INV-01**: 이미지 전송 요청자는 반드시 해당 채팅방의 `OpenChatParticipant`여야 한다.
  - 위반 시: 403 FORBIDDEN (`OPEN_CHAT_NOT_PARTICIPANT`)
- **INV-02**: 업로드 허용 포맷은 jpg, jpeg, png, gif, webp만 허용한다.
  - 위반 시: 400 BAD_REQUEST (`IMAGE_INVALID_FORMAT`)
- **INV-03**: 이미지 파일이 1개 이상 포함되어야 한다.
  - 위반 시: 400 BAD_REQUEST
- **INV-04**: 이미지 저장 성공 후에만 `OpenChatMessage`가 생성된다. 이미지 저장 실패 시 메시지는 생성되지 않는다.
  - 위반 시: 500, 메시지 미생성

#### 저장 순서 (중요)

```
① 포맷 검증 (모든 파일)
② 이미지 파일 디스크 저장 → imageUrls 확보
③ OpenChatMessage(type=IMAGE, content="") DB 저장 → messageId 확보
④ Image 레코드 DB 저장 (entityId=messageId, imageType=OPEN_CHAT_MESSAGE)
⑤ WebSocket 브로드캐스트 (imageUrls 포함)
```

> ⚠️ 디스크 저장(②)은 트랜잭션 밖에서 발생하므로 ③④ 실패 시 고아 파일이 남을 수 있다.  
> 이는 기존 `ImageService` 전체에 공통된 위험으로, 이번 기능 범위 내 해결 대상이 아니다.

#### 트랜잭션 경계

`OpenChatMessage` 저장과 `Image` 레코드 저장을 하나의 `@Transactional`로 묶는다.  
WebSocket 브로드캐스트는 트랜잭션 커밋 후 수행한다.

#### 동시성 고려사항

동일 메시지에 대한 동시 이미지 저장은 발생하지 않는 구조(요청자 1명)이므로 별도 락 불필요.

#### 도메인 이벤트

- `ImageMessageSent`: 이미지 메시지 저장 완료 시 → WebSocket 브로드캐스트 트리거

---

### Aggregate: Image (공통, 기존 확장)

#### 책임
도메인별 이미지 파일의 저장 경로와 참조 엔티티를 관리한다.

#### 변경 사항

`ImageType` 열거형에 `OPEN_CHAT_MESSAGE` 값 추가.  
`ImageService`에 `OPEN_CHAT_MESSAGE` 분기 처리 추가 (디렉토리 경로, 파일명 prefix, URL prefix).

---

## 4. 애그리거트 관계도

```
OpenChatParticipant --N:1--> OpenChatRoom (roomId로 참조)
OpenChatMessage     --1:N--> Image (entityId=messageId, imageType=OPEN_CHAT_MESSAGE)
OpenChatMessage     참조     OpenChatRoom (roomId, FK 없음)
OpenChatMessage     참조     User (senderId, FK 없음)
```

---

## 5. 도메인 이벤트

| 이벤트명 | 발행 주체 | 발행 시점 | 구독 주체 | 처리 내용 |
|----------|-----------|-----------|-----------|-----------|
| `ImageMessageSent` | `OpenChatMessageService` | 이미지 메시지 DB 저장 완료 후 | `SimpMessagingTemplate` | `/sub/openchat/{roomId}` 브로드캐스트 |

---

## 6. 도메인 서비스

### OpenChatImageMessageService (신규 또는 OpenChatMessageService 확장)

- **책임**: 이미지 파일 저장 → 메시지 생성 → Image 레코드 저장 → 브로드캐스트의 전체 흐름 조율
- **관여 애그리거트**: `OpenChatMessage`, `Image`, `OpenChatParticipant`
- **로직 요약**:
  1. 참여자 검증 (INV-01)
  2. 포맷 검증 (INV-02, INV-03)
  3. `ImageService.saveImages(OPEN_CHAT_MESSAGE, tempId, files)` — 디스크 저장 (트랜잭션 외)
  4. `OpenChatMessage` 저장 (content="", type=IMAGE) — messageId 확보
  5. `Image` 레코드 entityId를 messageId로 갱신하거나, messageId 확보 후 저장
  6. imageUrls 조회 후 WebSocket 브로드캐스트
- **트랜잭션 전략**: ③과 ④⑤를 하나의 `@Transactional`로 묶음

> **설계 주의**: `ImageService.saveImages()`는 `entityId`를 파일명에 포함시키므로, messageId 확보 전에 호출 시 파일명에 임시 ID가 사용된다. messageId 확보 후 이미지를 저장하거나, 파일명과 Image 레코드 entityId를 분리해야 한다.  
> **권장**: `OpenChatMessage`를 먼저 저장해 messageId 확보 → `ImageService.saveImages(OPEN_CHAT_MESSAGE, messageId, files)` 호출 순서로 처리한다.

---

## 7. 크로스-애그리거트 상호작용

| 상황 | 관여 애그리거트 | 일관성 전략 |
|------|----------------|-------------|
| 이미지 메시지 저장 | OpenChatMessage → Image | 단일 트랜잭션 (③④ 묶음) |
| 메시지 삭제 시 이미지 연쇄 삭제 | OpenChatMessage → Image | 단일 트랜잭션 |

---

## 8. 레포지토리 인터페이스

### ImageRepository (기존, 이미 존재)
```
findByImageTypeAndEntityId(imageType, entityId): List<Image>   // messageId로 이미지 목록 조회 — 이미 존재
```

### OpenChatMessageRepository (신규 추가 필요)
```
// 없음 — 기존 save/findById로 충분
```

---

## 9. 패키지 구조 제안

```
domain/openChat/
├── controller/
│   └── OpenChatMessageController     (기존 — sendImageMessage 엔드포인트 추가)
├── dto/
│   ├── request/
│   │   └── (MultipartFile 파라미터로 처리, 별도 DTO 불필요)
│   └── response/
│       └── ResponseOpenChatMessageDto  (기존 — imageUrls: List<String> 필드 추가)
└── service/
    └── OpenChatMessageService          (기존 — sendImageMessage 메서드 추가)

common/image/
├── enums/
│   └── ImageType                       (기존 — OPEN_CHAT_MESSAGE 추가)
└── service/
    └── ImageService                    (기존 — OPEN_CHAT_MESSAGE 분기 추가)
```

---

## 10. 설계 결정 사항 (ADR)

### ADR-01: IMAGE 타입 메시지의 content 필드 처리
- **결정**: content = "" (빈 문자열), 실제 URL은 Image 테이블에서 조회해 `imageUrls` 필드로 응답
- **이유**: 클라이언트가 type=IMAGE일 때 content를 별도 파싱할 필요 없이 `imageUrls` 필드를 바로 사용 가능
- **trade-off**: 메시지 조회 시 Image 테이블 추가 조회 필요 (messageId 목록으로 batch 조회로 최적화 가능)

### ADR-02: messageId 확보 후 이미지 저장 순서
- **결정**: OpenChatMessage를 먼저 저장해 messageId 확보 → imageService.saveImages(messageId) 호출
- **이유**: entityId에 정확한 messageId를 사용해 이미지-메시지 연결을 명확히 함
- **trade-off**: 디스크 저장 실패 시 빈 content의 IMAGE 메시지 레코드가 남을 수 있음 → 트랜잭션 롤백으로 처리, 디스크 파일만 고아로 남음

### ADR-03: 기존 공통 ImageService 재사용
- **결정**: `ImageType.OPEN_CHAT_MESSAGE` 추가, 기존 `ImageService` 분기 확장
- **이유**: 별도 채팅 이미지 저장 로직 없이 코드 일관성 유지
- **trade-off**: `ImageService`의 if 분기가 늘어남 (현재 7개 → 8개)

---

## 11. 아키텍처 위험 요소

- **고아 파일**: 디스크 저장 성공 후 DB 트랜잭션 실패 시 이미지 파일이 디스크에 잔류. 기존 ImageService 전체에 공통된 위험으로 현재 해결책 없음.
- **메시지 조회 N+1**: 메시지 목록 조회 시 IMAGE 타입 메시지마다 Image 테이블을 개별 조회하면 N+1 발생 → `findByImageTypeAndEntityIdIn(messageIds)` 배치 조회로 처리 필요.
- **content NOT NULL 제약**: `OpenChatMessage.content`는 `@Column(nullable = false)`. IMAGE 타입 저장 시 반드시 `""` (빈 문자열)을 사용해야 함.

---

## 12. TBD

- [ ] 메시지당 최대 이미지 첨부 개수 (제한 없으면 악용 가능성)
- [ ] 파일당 최대 크기 (현재 Spring multipart 기본값 1MB)
- [ ] 메시지 삭제 기능 구현 시 IMAGE 타입 연쇄 삭제 처리
- [ ] IMAGE 타입 메시지 조회 N+1 최적화 (배치 조회 적용 범위)
