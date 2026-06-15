# 요구사항 명세서

> **기능**: 오픈 채팅 이미지 전송

---

## 1. 개요

- **서비스 목적**: 오픈 채팅방 참여자가 텍스트 외에 이미지를 전송할 수 있도록 하여 채팅 표현력을 향상시킨다
- **핵심 사용자**: 기숙사 입주 학생(USER) — 오픈 채팅방 참여자
- **범위**
  - In Scope: 이미지 업로드(HTTP REST), 이미지 메시지 WebSocket 브로드캐스트, 메시지 삭제 시 이미지 연쇄 삭제
  - Out of Scope: 이미지 편집·필터, 썸네일 생성, 이미지 압축, GIF 재생 제어

---

## 2. 도메인 모델 후보

### 엔티티 목록

| 엔티티 | 핵심 속성 | 변경 |
|--------|-----------|------|
| `OpenChatMessage` | id, roomId, senderId, content(imageUrl JSON 배열), type(IMAGE) | content 의미 확장 |
| `Image` | id, imageName, imagePath, imageType(OPEN_CHAT_MESSAGE), entityId(messageId) | ImageType에 OPEN_CHAT_MESSAGE 추가 |
| `OpenChatParticipant` | (기존 유지) | 변경 없음 |

### 엔티티 간 관계

- `OpenChatMessage` 1 ↔ N `Image` (messageId = Image.entityId, imageType = OPEN_CHAT_MESSAGE)
- `Image`는 공통 엔티티로 imageType으로 도메인 구분

### 메시지 타입별 content 형식

| type | content 예시 |
|------|-------------|
| TEXT | `"안녕하세요"` |
| IMAGE | `["https://.../img1.jpg", "https://.../img2.png"]` (JSON 배열 문자열) |
| SYSTEM | `"홍길동 님이 입장했습니다"` |

---

## 3. 비즈니스 규칙

1. **BR-01** 이미지 전송은 해당 채팅방의 `OpenChatParticipant`만 가능
   - 위반 시: 403 FORBIDDEN (OPEN_CHAT_NOT_PARTICIPANT)

2. **BR-02** 허용 이미지 포맷은 jpg, jpeg, png, gif, webp만 허용
   - 위반 시: 400 BAD_REQUEST (IMAGE_INVALID_FORMAT)

3. **BR-03** 한 메시지에 여러 이미지를 첨부할 수 있다 (최대 개수 TBD)
   - 이미지 각각 `Image` 테이블에 row 생성, 동일한 `messageId`를 entityId로 사용

4. **BR-04** 이미지 저장 성공 후 `OpenChatMessage`(type=IMAGE)를 생성하고 WebSocket으로 브로드캐스트
   - 이미지 업로드 실패 시 메시지 생성 없이 HTTP 에러 반환 (원자적 처리)

5. **BR-05** 메시지 삭제 시 연결된 `Image` 레코드와 디스크 파일 연쇄 삭제
   - 현재 오픈 채팅 메시지 삭제 기능은 미구현 — 삭제 기능 추가 시 이미지 연쇄 삭제 포함

6. **BR-06** 이미지 업로드는 인증된 사용자(JWT)만 가능, ADMIN/DORMITORY 특별 제한 없음

---

## 4. 사용자 & 권한

| 역할 | 이미지 전송 | 이미지 조회 |
|------|------------|------------|
| `USER` (참여자) | 가능 | 가능 |
| `USER` (비참여자) | 불가 (BR-01) | 불가 |
| `ADMIN` | 참여자인 경우 가능 | 가능 |
| `DORMITORY` | 참여자인 경우 가능 | 가능 |
| 비인증 | 불가 | 불가 |

---

## 5. 주요 시나리오

### Happy Path

1. 참여자가 `POST /open-chat-rooms/{roomId}/messages/image` 에 이미지 파일(들)을 multipart로 전송
2. 서버가 포맷 검증 후 디스크에 파일 저장
3. `OpenChatMessage`(type=IMAGE, content=imageUrl JSON 배열) 생성 → messageId 확보
4. `Image` 레코드를 messageId를 entityId로 저장
5. WebSocket `/sub/openchat/{roomId}` 로 브로드캐스트 (ResponseOpenChatMessageDto)
6. 참여자 전원이 실시간으로 이미지 메시지 수신

### 예외 시나리오

| 시나리오 | 처리 방식 |
|----------|-----------|
| 비참여자가 이미지 전송 시도 | 403 FORBIDDEN |
| 허용되지 않는 포맷(heic, pdf 등) 업로드 | 400 BAD_REQUEST |
| 파일 크기 초과 | 413 PAYLOAD_TOO_LARGE (Spring 기본 multipart 제한 적용) |
| 디스크 저장 실패 | 500 내부 오류, 메시지 미생성 |
| 이미지 없이 요청 | 400 BAD_REQUEST |

---

## 6. 비기능 요구사항

- **성능**: 이미지 업로드 응답 3초 이내 (네트워크 제외 서버 처리 기준)
- **데이터 보존**: 메시지 삭제 시 hard delete (이미지 파일·DB row 모두 삭제)
- **외부 연동**: 없음 (기존 로컬 파일 시스템 저장 방식 유지)
- **저장 방식**: 기존 `ImageService` 재사용, `ImageType.OPEN_CHAT_MESSAGE` 추가

---

## 7. 미결 사항 (TBD)

- [ ] 메시지당 최대 이미지 첨부 개수 (예: 5장)
- [ ] 파일당 최대 업로드 크기 (현재 Spring multipart 기본값 적용)
- [ ] 오픈 채팅 메시지 삭제 기능 구현 범위 (이번 이슈에 포함할지 별도 이슈로 분리할지)
- [ ] 이미지 URL 만료 정책 (현재 서버 로컬 저장이므로 해당 없음)
