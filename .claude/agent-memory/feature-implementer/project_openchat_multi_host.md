---
name: openchat-multi-host
description: #643 오픈채팅 다중 방장 시스템 구현 완료, ErrorCode 22015~22016, isHost 필드 기반 방장 관리, V13 마이그레이션
metadata:
  type: project
---

## 구현 완료: 오픈채팅 다중 방장 시스템 (#643)

### 신규 ErrorCode
- `OPEN_CHAT_ALREADY_HOST (BAD_REQUEST, 22015)` — 이미 방장인 사용자
- `OPEN_CHAT_SOLE_HOST_CANNOT_LEAVE (BAD_REQUEST, 22016)` — 단독 방장 나가기 불가

### 주요 변경사항
- `OpenChatParticipant.isHost` (boolean) 필드 추가 (@Column name="is_host")
- `OpenChatParticipant.create(Long roomId, Long userId, boolean isHost)` 팩토리 추가
- `OpenChatParticipant.grantHost()` 메서드 추가
- `OpenChatRoom.createOfficial(...)` 팩토리 추가 (isOfficial=true)
- `hostUserId` 필드는 DB 하위 호환을 위해 엔티티에 유지 (V13 마이그레이션에서 is_host 컬럼 추가 + 데이터 이관)
- `OpenChatParticipantRepository` 신규 메서드:
  - `existsByRoomIdAndUserIdAndIsHost`
  - `countByRoomIdAndIsHost`
  - `findAllByRoomId`
  - `findAllByRoomIdWithLock` (@Lock PESSIMISTIC_WRITE)
- `OpenChatRoomService.leaveRoom(Long roomId, Long userId, Long newHostUserId)` 시그니처 변경
- `OpenChatRoomService.deleteRoom(Long roomId, Long userId)` 시그니처 변경 (파라미터 순서 주의)
- `OpenChatRoomService.grantHost(Long roomId, Long requesterId, Long targetUserId)` 신규
- `OpenChatRoomService.getParticipants(Long roomId, Long requesterId)` 신규
- `ResponseOpenChatParticipantListDto.hostCount` 필드 추가

### 신규 엔드포인트
- `POST /open-chat-rooms/{roomId}/hosts/{targetUserId}` — 방장 부여 (204)
- `GET /open-chat-rooms/{roomId}/participants` — 참여자 목록 조회 (200, hostCount 포함)
- `DELETE /open-chat-rooms/{roomId}/participants/me?newHostUserId={id}` — 나가기 (newHostUserId 조건부)

### Flyway 마이그레이션
- V13__open_chat_multi_host.sql: is_host 컬럼 추가 + 기존 host_user_id 기준 데이터 이관

### WebMvcTest 패턴 (중요)
- `@WebMvcTest`에서 `CustomException` 처리가 안되는 문제: `@MockBean SlackErrorNotifier` + `@MockBean JpaMetamodelMappingContext` 필요
- `@WithMockUser` 대신 `SecurityContextHolder`에 직접 `CustomUserDetails` 주입:
  ```java
  User mockUser = mock(User.class);
  given(mockUser.getId()).willReturn(MOCK_USER_ID);
  given(mockUser.getRole()).willReturn(Role.ROLE_USER);
  CustomUserDetails userDetails = new CustomUserDetails(mockUser);
  UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  SecurityContextHolder.getContext().setAuthentication(auth);
  ```
- `@WithMockUser` 사용 시 `CustomUserDetails`로 캐스팅 안됨 → NPE → 500 반환

### DataJpaTest 패턴 (중요)
- `@DataJpaTest` 단독 사용 시 H2 DDL 생성 실패 (application.yml MySQL 설정 충돌)
- 반드시 properties로 H2 설정 명시:
  ```java
  @DataJpaTest(properties = {
      "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.flyway.enabled=false"
  })
  ```

**왜:** 방장 역할을 단일 hostUserId 컬럼에서 participant의 isHost 플래그로 마이그레이션. 다중 방장 지원 필요.
**적용 방법:** openChat 도메인의 방장 관련 로직은 모두 isHost 필드 기반으로 확인.
