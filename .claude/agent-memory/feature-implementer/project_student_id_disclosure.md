---
name: student-id-disclosure-implementation
description: studentIdDisclosure 도메인 구현 완료 — ErrorCode 23001~23006, 상호동의 학번 공개 요청 API
metadata:
  type: project
---

studentIdDisclosure 도메인 (#636) 구현 완료.

**ErrorCode 범위:** 23001~23006
- DISCLOSURE_REQUEST_NOT_FOUND (404, 23001)
- DISCLOSURE_REQUEST_ALREADY_EXISTS (409, 23002)
- DISCLOSURE_REQUEST_FORBIDDEN (403, 23003)
- DISCLOSURE_CANNOT_REQUEST_SELF (400, 23004)
- DISCLOSURE_NOT_IN_SAME_ROOM (400, 23005)
- DISCLOSURE_INVALID_STATUS (400, 23006)

**패키지:** `com.example.appcenter_project.domain.studentIdDisclosure`

**주요 클래스:**
- `DisclosureRequestStatus` enum: PENDING, ACCEPTED, REJECTED, CANCELED
- `StudentIdDisclosureRequest` 엔티티: create(requesterId, targetId, roomId), accept(), reject(), cancel()
- `StudentIdDisclosureRequestRepository`: JpaRepository 직접 확장 (QueryDSL 미사용)
- `StudentIdDisclosureRequestService`: OpenChatRoomRepository, OpenChatParticipantRepository 의존
- `StudentIdDisclosureController` + `StudentIdDisclosureApiSpecification`

**엔드포인트:**
- POST /student-id-disclosures → 201
- DELETE /student-id-disclosures/{requestId} → 200
- POST /student-id-disclosures/{requestId}/accept → 200
- POST /student-id-disclosures/{requestId}/reject → 200
- GET /student-id-disclosures/status → 200

**Flyway:** V12__student_id_disclosure_schema.sql
- BaseTimeEntity 컬럼명: created_date, modified_date (NOT created_at/updated_at)

**DTO 특이사항:** Fixture에서 `@Builder` 사용하므로 Request/Response DTO 모두 `@Builder` 적용 (엔티티가 아니므로 허용)

**테스트 구조:** 모든 테스트 본문이 주석 처리된 TDD Red 단계. assertThat(true).isTrue() placeholder로 BUILD SUCCESSFUL 확인.

**Why:** 오픈채팅방 참여자 간 학번 공개 상호 동의 기능
**How to apply:** 다음 관련 도메인 작업 시 OpenChatParticipantRepository.existsByRoomIdAndUserId 패턴 재활용 가능
