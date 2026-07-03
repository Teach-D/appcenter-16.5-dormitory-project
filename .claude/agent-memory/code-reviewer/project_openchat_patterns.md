---
name: openchat-domain-patterns
description: openChat 도메인 리뷰(파생 톡방/초대)에서 발견된 반복 안티패턴 및 설계 결정
metadata:
  type: project
---

## 반복 안티패턴

### VALIDATION_ERROR 남용
- openChat 도메인에서 비즈니스 규칙 위반(파생 방 중첩 금지, 초대 상태 전이 불가)에 범용 VALIDATION_ERROR(22010)를 반복 사용함
- 새 기능 추가 시 ErrorCode 블록(22011+)에 전용 코드를 추가해야 함

### path 파라미터와 엔티티 소유권 검증 누락
- `/{roomId}/invitations/{invitationId}` 패턴에서 invitation.roomId == roomId 검증이 누락된 사례 발견
- 다중 path 파라미터를 받는 모든 서비스 메서드에서 파라미터 간 연관 검증 필수

### BaseTimeEntity 상속 누락
- OpenChatInvitation이 BaseTimeEntity를 상속하지 않고 createdAt을 LocalDateTime.now()로 수동 설정
- openChat 패키지 엔티티: OpenChatRoom, OpenChatParticipant는 BaseTimeEntity 상속 중, OpenChatInvitation만 누락

### 서비스 클래스 책임 혼재
- OpenChatInvitationService가 파생 방 생성 + 초대 CRUD + 참여자 조회를 모두 담당
- "Invitation" 이름이 책임을 설명하지 못함
- 향후 OpenChatDerivedRoomService 분리 권장

## 잘 된 설계 결정

- acceptInvitation에서 findByIdWithLock(비관적 락) 사용하여 정원 초과 race condition 방어 — 유지 필수
- 모든 Request DTO에 @Valid + 세분화된 검증 어노테이션 적용
- 엔티티 상태 전이(accept/reject)가 서비스가 아닌 엔티티 메서드에 캡슐화됨
- Response DTO 모두 static factory of() + final 필드 패턴 일관 적용

## ErrorCode 현황 (openChat)
- 22001: OPEN_CHAT_ROOM_NOT_FOUND
- 22002: OPEN_CHAT_ROOM_FORBIDDEN
- 22003: OPEN_CHAT_ROOM_FULL
- 22004: OPEN_CHAT_PARTICIPANT_NOT_FOUND
- 22005: OPEN_CHAT_NOT_PARTICIPANT
- 22006: OPEN_CHAT_INVITATION_NOT_FOUND
- 22007: OPEN_CHAT_INVITATION_INVALID_TARGET
- 22008: OPEN_CHAT_INVITATION_ALREADY_EXISTS
- 22009: OPEN_CHAT_PARTICIPANT_ALREADY_EXISTS
- 22010: VALIDATION_ERROR (범용 — 남용 주의)
- 22011: OPEN_CHAT_INVITATION_ALREADY_PROCESSED
- 22012: OPEN_CHAT_INVITATION_SELF_INVITE
- 22013~: 미사용 (OPEN_CHAT_IMAGE_COUNT_EXCEEDED, OPEN_CHAT_IMAGE_EMPTY 추가 권장)

## 이미지 전송 기능 (2026-06-15 추가)
- sendImageMessage 검증 로직이 OpenChatMessageService 안에 직접 구현됨 (약 84~115줄)
  - images null/empty → VALIDATION_ERROR 남용 (이미지 전용 ErrorCode 필요)
  - images.size() > 5 → VALIDATION_ERROR 남용
  - 파일 크기/확장자/MIME 검증은 ImageService 또는 전용 validator로 분리 권장
- getMessages()에서 SYSTEM 메시지 발신자 조회 시 userRepository.findById()를 스트림 내부에서 반복 호출 → N+1 잠재 위험
- OpenChatMessageService에 클래스 레벨 @Transactional, getMessages에 중복 @Transactional 선언
- ImageService는 jakarta.transaction.Transactional 사용 (spring의 readOnly=true 지원 안 됨)
- ImageService.findImageByImageTypeAndEntityId(): DB를 2회 조회하는 중복 코드 버그 존재

## ImageService 구조 이슈
- findStaticImageUrl(ImageType, String, String) 내부에 if 체인 8개 반복 (createDirectoryPath, createImageName도 동일 패턴)
- 새 ImageType 추가 시 4곳(createDirectoryPath, createImageName, findStaticImageUrl, 그리고 prefix 상수)을 모두 수정해야 함 → OCP 위반

## 다중 방장 기능 (#643, 2026-06-16 추가)

### @Autowired 필드 주입 잔존
- OpenChatRoomService가 @Autowired 생성자 주입 + @Lazy를 사용 (사유: OpenChatMessageService 순환 의존 해소)
- @RequiredArgsConstructor 패턴을 쓸 수 없는 순환 의존 구조이므로, @Lazy 명시적 생성자 주입은 허용 패턴으로 기록

### OpenChatRoom.hostUserId 레거시 필드 잔존
- 다중 방장 전환 후에도 OpenChatRoom.hostUserId가 유지됨
- 마이그레이션(V13)은 is_host 컬럼만 추가하고 host_user_id는 그대로 보존
- OpenChatRoom.create(), createOfficial(), createDerived() 팩토리 메서드가 여전히 hostUserId 파라미터를 받음 → 진실의 원천(source of truth)이 두 곳(hostUserId + isHost)으로 분리됨

### leaveRoom() 비관적 락 패턴의 이중 조회 문제
- leaveRoom()에서 participant를 findByRoomIdAndUserId로 먼저 조회한 뒤, 다시 findAllByRoomIdWithLock으로 전체를 잠금 조회함
- 두 번째 조회의 잠금이 유효해도, 첫 번째 조회에서 읽은 participant 객체의 상태가 Lock 획득 이전 스냅샷일 수 있음
- 올바른 패턴: 항상 findAllByRoomIdWithLock으로 먼저 락을 걸고, 그 결과에서 자신을 찾아 유효성 검증

### ResponseOpenChatParticipantListDto.of() 오버로드 중복 계산
- 2개의 of() 팩토리 메서드 존재: 하나는 hostCount를 직접 계산, 하나는 외부에서 주입
- OpenChatRoomService.getParticipants()는 participants 스트림에서 hostCount를 계산해 두 번째 of()를 호출하지만
  첫 번째 of()도 동일 계산을 수행하므로 혼란 유발 — 하나의 of()로 통일 권장

### 테스트 형식 — 빈 테스트 다수
- OpenChatMultiHostServiceTest의 많은 케이스가 fixture 생성 후 assertThat(obj).isNotNull() 만 수행 (실질적 동작 검증 없음)
- "Red Phase" TDD 패턴(주석 처리 placeholder)이 아닌 실제 그린 단계 테스트에 이 패턴이 남아있음 → 개선 필요

### ErrorCode 현황 업데이트 (다중 방장 추가분)
- 22015: OPEN_CHAT_ALREADY_HOST (메모리 기록 당시 예정) → 실제 ErrorCode.java 확인 시 미등록
- 22016: OPEN_CHAT_SOLE_HOST_CANNOT_LEAVE (메모리 기록 당시 예정) → 실제 ErrorCode.java 확인 시 미등록
- 주의: 22015는 강제퇴장 기능에서 OPEN_CHAT_KICK_FORBIDDEN으로 실제 사용됨 (2026-06-17 확인)

## 강제퇴장 기능 (#kick, 2026-06-17 추가)

### Controller에서 isAdmin 권한 분기 로직
- kickParticipant()에서 `user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))` 로 isAdmin 판별
- boolean isAdmin을 Service 파라미터로 전달하는 패턴 — Controller에 비즈니스 규칙 판단 포함 논란 있음
- 대안: @PreAuthorize("hasRole('ADMIN')") + 별도 ADMIN 전용 엔드포인트 또는 SecurityContextHolder를 Service에서 직접 참조

### 자기 자신 강제퇴장 방어 위치
- actorId.equals(targetUserId) 체크가 isAdmin/비isAdmin 분기 이전에 수행됨 → ADMIN도 자기 자신은 퇴장 불가
- 이 동작이 의도인지 확인 필요 (ADMIN 자기 자신 퇴장 케이스는 테스트 커버리지 없음)

### TC-09 테스트 케이스 설계 결함
- 서비스 테스트 TC-09 ("방장이 다른 방장을 퇴장 시도")의 fixture가 TC-08과 동일: hostId == anotherHostId == 1L
- 실제로 TC-09는 "방장이 자기 자신 퇴장 시도"와 동일한 케이스 → 테스트 의도와 구현이 불일치
