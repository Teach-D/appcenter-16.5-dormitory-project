# BR-668 단체·개인 채팅방 생성 (비밀번호·공개 여부)

## 기능 요약

`POST /open-chat-rooms`(단체 채팅방)에 비밀번호·공개 여부 필드를 추가하고,
`POST /open-chat-rooms/personal`(개인 채팅방, 최대 2명, 항상 비공개)을 신규 구현한다.

---

## 동작 명세

### 단체 채팅방 생성 (`POST /open-chat-rooms` 수정)

**정상 흐름**
1. 인증된 사용자가 `name`, `description`(opt), `scope`, `maxParticipants`, `isPublic`(opt), `password`(opt)를 전송
2. `scope=DORMITORY`이면 생성자의 dormType을 조회해 `creatorDormitory` 저장
3. `isPublic`이 null이면 `true`로 처리
4. `password`가 null이면 비밀번호 없는 방
5. `OpenChatRoomType.OPEN` 방 생성, 생성자를 host 참여자로 저장
6. `roomId` 반환 (HTTP 201)

**분기**
- `isPublic=false` 방은 전체 채팅방 목록 조회(ALL 탭)에 노출되지 않는다

---

### 개인 채팅방 생성 (`POST /open-chat-rooms/personal` 신규)

**정상 흐름**
1. 인증된 사용자가 `name`, `targetUserId`(필수), `password`(opt)를 전송
2. `targetUserId != 생성자 ID` 검증
3. `targetUserId`에 해당하는 사용자 존재 여부 검증
4. `maxParticipants=2`, `isPublic=false`, `scope=ALL` 고정 적용
5. `OpenChatRoomType.PERSONAL` 방 생성
6. 생성자를 host 참여자로, `targetUserId` 사용자를 일반 참여자로 즉시 등록
7. `roomId` 반환 (HTTP 201)

---

## 도메인 데이터

### OpenChatRoomType (enum 변경)
| 값 | 설명 |
|---|---|
| OPEN | 기존 단체 오픈 채팅방 |
| DERIVED | 파생 톡방 |
| **PERSONAL** | 신규. 최대 2명, 항상 비공개 |

### OpenChatRoom (기존 엔티티, 필드 추가 없음)
- `password` (length=50, nullable): 이미 존재. 단체 채팅방에도 적용
- `isPublic` (boolean, default true): 이미 존재. 단체 채팅방에도 적용

### RequestCreateOpenChatRoomDto (수정)
| 필드 | 타입 | 제약 |
|---|---|---|
| name | String | NotBlank, max=30 |
| description | String | nullable, max=100 |
| scope | OpenChatRoomScope | NotNull |
| maxParticipants | Integer | NotNull, min=2, max=100 |
| **isPublic** | Boolean | nullable → 서비스에서 null 시 true 적용 |
| **password** | String | nullable, max=50 |

### RequestCreatePersonalRoomDto (신규)
| 필드 | 타입 | 제약 |
|---|---|---|
| name | String | NotBlank, max=30 |
| targetUserId | Long | NotNull |
| password | String | nullable, max=50 |

### 정적 팩토리 메서드 추가 (OpenChatRoom)
- `OpenChatRoom.createPersonal(name, createdBy, password)` 신규

---

## 비즈니스 규칙 / 제약

- 단체 채팅방: `password` 설정 시 `isPublic`은 false/true 모두 허용 (공개 비밀번호방 가능)
- 단체 채팅방: `isPublic=false`이면 ALL 탭 `findAllPublicRooms` 쿼리에서 제외 (기존 쿼리 이미 `isPublic` 컬럼 사용 여부 확인 후 적용)
- 개인 채팅방: `targetUserId`는 생성자 본인 ID와 달라야 한다
- 개인 채팅방: `targetUserId`에 해당하는 사용자가 존재해야 한다
- 개인 채팅방: 생성 즉시 생성자(host)·targetUser(일반 참여자) 2명이 등록되어 정원이 차게 된다
- 개인 채팅방: `maxParticipants=2` 서버 고정, 클라이언트 입력값 없음
- 개인 채팅방: `isPublic=false` 서버 고정
- 개인 채팅방: `description=null` 고정
- 개인 채팅방: `scope=ALL` 고정 (기숙사 제한 없음)
- 비밀번호는 평문 저장 (기존 `matchesPassword` 방식 유지)
- 모든 방: 생성자는 자동으로 host 참여자로 등록

---

## 예외 · 경계 상황

| 상황 | 응답 |
|---|---|
| targetUserId가 생성자 본인인 경우 | 400 BAD_REQUEST (신규 ErrorCode: OPEN_CHAT_SELF_PERSONAL_FORBIDDEN) |
| targetUserId에 해당하는 사용자 없음 | 404 USER_NOT_FOUND |
| PERSONAL 방 생성 후 다른 사용자 입장 시도 | 400 OPEN_CHAT_ROOM_FULL (생성 시 이미 정원 2명 차 있음) |
| 단체 채팅방 비밀번호 불일치 (입장 시) | OPEN 타입은 기존 joinRoom에서 비밀번호 검증 미적용 상태. 이번 범위에서 OPEN 타입 입장 시 비밀번호 검증 추가하지 않음 |
| `isPublic` null 전송 | true로 처리 |
| `password` 빈 문자열 전송 | null과 동일하게 처리(비밀번호 없음) |

---

## 비목표 (Non-goals)

- 비밀번호 암호화(해시) — 기존 평문 방식 유지
- 기존 OPEN 타입 방 입장 시 비밀번호 검증 — 이번 범위 외
- 채팅방 수정(비밀번호 변경, 공개 여부 변경) API
- 개인 채팅방의 방장 위임·강퇴 등 관리 기능
- 채팅방 목록 조회에서 PERSONAL 탭 추가 — MY 탭에서 기존 방식으로 노출됨
- targetUser에게 초대 알림 발송
- joinRoom의 PERSONAL 분기 비밀번호 검증 — 생성 시 이미 정원이 차므로 불필요
- 인증/로깅/캐싱

---

## 수용 기준 (Acceptance Criteria)

### 단체 채팅방 생성

**AC-1** 비밀번호·공개 여부 없이 생성 (기존 호환)
- Given 인증된 사용자, `{name, scope, maxParticipants}` 전송
- When `POST /open-chat-rooms`
- Then HTTP 201, `roomId` 반환. 생성된 방의 `isPublic=true`, `password=null`, `roomType=OPEN`

**AC-2** 비밀번호와 `isPublic=false` 포함 생성
- Given `{name, scope, maxParticipants, password: "1234", isPublic: false}`
- When `POST /open-chat-rooms`
- Then HTTP 201, 방의 `password="1234"`, `isPublic=false`

**AC-3** `isPublic=false` 방은 ALL 탭에 미노출
- Given isPublic=false인 OPEN 방 존재
- When `GET /open-chat-rooms?tab=ALL`
- Then 해당 방이 목록에 없음

**AC-4** `isPublic=null` 전송 시 기본값 true 적용
- Given `{name, scope, maxParticipants}` (isPublic 필드 없음)
- When `POST /open-chat-rooms`
- Then 방의 `isPublic=true`

**AC-5** 생성자는 자동으로 host 참여자로 등록
- Given 단체 채팅방 생성 성공
- When `GET /open-chat-rooms/{roomId}/participants`
- Then 생성자가 isHost=true로 존재

---

### 개인 채팅방 생성

**AC-6** 기본 생성 — targetUser 즉시 등록
- Given 인증된 사용자 A, 다른 사용자 B, `{name: "우리방", targetUserId: B.id}`
- When `POST /open-chat-rooms/personal`
- Then HTTP 201, `roomId` 반환. 방의 `roomType=PERSONAL`, `isPublic=false`, `maxParticipants=2`, `password=null`. A는 isHost=true, B는 isHost=false로 참여자 등록됨

**AC-7** 비밀번호 포함 생성
- Given `{name: "비밀방", targetUserId: B.id, password: "pass"}`
- When `POST /open-chat-rooms/personal`
- Then HTTP 201, 방의 `password="pass"`

**AC-8** 생성 후 참여자 2명 확인
- Given 개인 채팅방 생성 성공 (A 생성, targetUserId=B)
- When 참여자 목록 조회
- Then A(isHost=true), B(isHost=false) 2명 존재

**AC-9** targetUserId가 본인인 경우 거부
- Given `{name: "혼자방", targetUserId: 생성자 본인 ID}`
- When `POST /open-chat-rooms/personal`
- Then HTTP 400

**AC-10** targetUserId가 존재하지 않는 사용자인 경우 거부
- Given `{name: "방", targetUserId: 99999}` (존재하지 않는 userId)
- When `POST /open-chat-rooms/personal`
- Then HTTP 404
