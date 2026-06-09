package com.example.appcenter_project.domain.studentIdDisclosure.service;

import com.example.appcenter_project.domain.fcm.service.FcmMessageService;
import com.example.appcenter_project.domain.studentIdDisclosure.dto.request.RequestCreateDisclosureDto;
import com.example.appcenter_project.domain.studentIdDisclosure.dto.response.ResponseDisclosureAcceptDto;
import com.example.appcenter_project.domain.studentIdDisclosure.dto.response.ResponseDisclosureSendDto;
import com.example.appcenter_project.domain.studentIdDisclosure.dto.response.ResponseDisclosureStatusDto;
import com.example.appcenter_project.domain.studentIdDisclosure.entity.StudentIdDisclosureRequest;
import com.example.appcenter_project.domain.studentIdDisclosure.enums.DisclosureRequestStatus;
import com.example.appcenter_project.domain.studentIdDisclosure.fixture.StudentIdDisclosureFixture;
import com.example.appcenter_project.domain.studentIdDisclosure.repository.StudentIdDisclosureRequestRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.Role;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.example.appcenter_project.domain.studentIdDisclosure.enums.DisclosureRequestStatus.ACCEPTED;
import static com.example.appcenter_project.domain.studentIdDisclosure.enums.DisclosureRequestStatus.PENDING;
import static com.example.appcenter_project.domain.studentIdDisclosure.fixture.StudentIdDisclosureFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class StudentIdDisclosureServiceTest {

    @Mock
    private StudentIdDisclosureTransactionService transactionService;

    @Mock
    private StudentIdDisclosureRequestRepository disclosureRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FcmMessageService fcmMessageService;

    @InjectMocks
    private StudentIdDisclosureRequestService disclosureService;

    // =========================================================================
    // Happy Path
    // =========================================================================

    @Test
    @DisplayName("요청 발송 성공 — 정상 요청 시 requestId 반환")
    void should_return_requestId_when_sendRequest_valid() {
        RequestCreateDisclosureDto dto = StudentIdDisclosureFixture.createSendRequestDto();
        given(transactionService.saveRequest(REQUESTER_ID, dto)).willReturn(REQUEST_ID);
        given(userRepository.findById(TARGET_ID)).willReturn(Optional.empty());

        ResponseDisclosureSendDto result = disclosureService.sendRequest(REQUESTER_ID, dto);

        assertThat(result.getRequestId()).isEqualTo(REQUEST_ID);
    }

    @Test
    @DisplayName("요청 취소 성공 — PENDING 상태 전이 → CANCELED")
    void should_transition_to_CANCELED_when_cancel_valid() {
        willDoNothing().given(transactionService).cancelRequest(REQUESTER_ID, REQUEST_ID);

        disclosureService.cancel(REQUESTER_ID, REQUEST_ID);

        then(transactionService).should(times(1)).cancelRequest(REQUESTER_ID, REQUEST_ID);
    }

    @Test
    @DisplayName("요청 수락 성공 — PENDING 상태 전이 → ACCEPTED 및 요청자 학번 반환")
    void should_return_requesterStudentNumber_when_accept_valid() {
        ResponseDisclosureAcceptDto dto = ResponseDisclosureAcceptDto.builder()
                .requestId(REQUEST_ID)
                .requesterStudentNumber(REQUESTER_STUDENT_NUMBER)
                .build();
        StudentIdDisclosureTransactionService.AcceptResult acceptResult =
                new StudentIdDisclosureTransactionService.AcceptResult(dto, REQUESTER_ID);
        given(transactionService.acceptRequest(TARGET_ID, REQUEST_ID)).willReturn(acceptResult);
        given(userRepository.findById(REQUESTER_ID)).willReturn(Optional.empty());

        ResponseDisclosureAcceptDto result = disclosureService.accept(TARGET_ID, REQUEST_ID);

        assertThat(result.getRequesterStudentNumber()).isEqualTo(REQUESTER_STUDENT_NUMBER);
    }

    @Test
    @DisplayName("요청 거절 성공 — PENDING 상태 전이 → REJECTED")
    void should_transition_to_REJECTED_when_reject_valid() {
        given(transactionService.rejectRequest(TARGET_ID, REQUEST_ID)).willReturn(REQUESTER_ID);
        given(userRepository.findById(REQUESTER_ID)).willReturn(Optional.empty());

        disclosureService.reject(TARGET_ID, REQUEST_ID);

        then(transactionService).should(times(1)).rejectRequest(TARGET_ID, REQUEST_ID);
    }

    @Test
    @DisplayName("상태 조회 성공 — ACCEPTED 레코드 존재 시 DISCLOSED 반환")
    void should_return_DISCLOSED_when_accepted_record_exists() {
        StudentIdDisclosureRequest accepted = StudentIdDisclosureFixture.createAcceptedRequest();
        User target = createMockUser(TARGET_ID, TARGET_STUDENT_NUMBER);
        given(disclosureRequestRepository.findByRoomIdAndRequesterIdAndTargetIdAndStatus(
                ROOM_ID, REQUESTER_ID, TARGET_ID, ACCEPTED)).willReturn(Optional.of(accepted));
        given(userRepository.findById(TARGET_ID)).willReturn(Optional.of(target));

        ResponseDisclosureStatusDto result = disclosureService.getStatus(REQUESTER_ID, ROOM_ID, TARGET_ID);

        assertThat(result.getStatus()).isEqualTo("DISCLOSED");
        assertThat(result.getTargetStudentNumber()).isNotNull();
    }

    @Test
    @DisplayName("상태 조회 성공 — 내가 보낸 PENDING 존재 시 PENDING_SENT 반환")
    void should_return_PENDING_SENT_when_sent_pending_exists() {
        StudentIdDisclosureRequest pending = StudentIdDisclosureFixture.createPendingRequest();
        given(disclosureRequestRepository.findByRoomIdAndRequesterIdAndTargetIdAndStatus(
                ROOM_ID, REQUESTER_ID, TARGET_ID, ACCEPTED)).willReturn(Optional.empty());
        given(disclosureRequestRepository.findByRoomIdAndRequesterIdAndTargetIdAndStatus(
                ROOM_ID, TARGET_ID, REQUESTER_ID, ACCEPTED)).willReturn(Optional.empty());
        given(disclosureRequestRepository.findByRoomIdAndRequesterIdAndTargetIdAndStatus(
                ROOM_ID, REQUESTER_ID, TARGET_ID, PENDING)).willReturn(Optional.of(pending));

        ResponseDisclosureStatusDto result = disclosureService.getStatus(REQUESTER_ID, ROOM_ID, TARGET_ID);

        assertThat(result.getStatus()).isEqualTo("PENDING_SENT");
    }

    @Test
    @DisplayName("상태 조회 성공 — 내가 받은 PENDING 존재 시 PENDING_RECEIVED 반환")
    void should_return_PENDING_RECEIVED_when_received_pending_exists() {
        StudentIdDisclosureRequest pending = StudentIdDisclosureRequest.create(TARGET_ID, REQUESTER_ID, ROOM_ID);
        given(disclosureRequestRepository.findByRoomIdAndRequesterIdAndTargetIdAndStatus(
                ROOM_ID, REQUESTER_ID, TARGET_ID, ACCEPTED)).willReturn(Optional.empty());
        given(disclosureRequestRepository.findByRoomIdAndRequesterIdAndTargetIdAndStatus(
                ROOM_ID, TARGET_ID, REQUESTER_ID, ACCEPTED)).willReturn(Optional.empty());
        given(disclosureRequestRepository.findByRoomIdAndRequesterIdAndTargetIdAndStatus(
                ROOM_ID, REQUESTER_ID, TARGET_ID, PENDING)).willReturn(Optional.empty());
        given(disclosureRequestRepository.findByRoomIdAndRequesterIdAndTargetIdAndStatus(
                ROOM_ID, TARGET_ID, REQUESTER_ID, PENDING)).willReturn(Optional.of(pending));

        ResponseDisclosureStatusDto result = disclosureService.getStatus(REQUESTER_ID, ROOM_ID, TARGET_ID);

        assertThat(result.getStatus()).isEqualTo("PENDING_RECEIVED");
    }

    @Test
    @DisplayName("상태 조회 성공 — 활성 요청 없음 시 NONE 반환")
    void should_return_NONE_when_no_active_request() {
        given(disclosureRequestRepository.findByRoomIdAndRequesterIdAndTargetIdAndStatus(
                ROOM_ID, REQUESTER_ID, TARGET_ID, ACCEPTED)).willReturn(Optional.empty());
        given(disclosureRequestRepository.findByRoomIdAndRequesterIdAndTargetIdAndStatus(
                ROOM_ID, TARGET_ID, REQUESTER_ID, ACCEPTED)).willReturn(Optional.empty());
        given(disclosureRequestRepository.findByRoomIdAndRequesterIdAndTargetIdAndStatus(
                ROOM_ID, REQUESTER_ID, TARGET_ID, PENDING)).willReturn(Optional.empty());
        given(disclosureRequestRepository.findByRoomIdAndRequesterIdAndTargetIdAndStatus(
                ROOM_ID, TARGET_ID, REQUESTER_ID, PENDING)).willReturn(Optional.empty());

        ResponseDisclosureStatusDto result = disclosureService.getStatus(REQUESTER_ID, ROOM_ID, TARGET_ID);

        assertThat(result.getStatus()).isEqualTo("NONE");
    }

    @Test
    @DisplayName("퇴장 시 레코드 삭제 성공 — 해당 방·사용자 모든 레코드 hard delete")
    void should_delete_all_records_when_deleteByRoomAndUser() {
        willDoNothing().given(transactionService).deleteByRoomAndUser(ROOM_ID, REQUESTER_ID);

        disclosureService.deleteByRoomAndUser(ROOM_ID, REQUESTER_ID);

        then(transactionService).should(times(1)).deleteByRoomAndUser(ROOM_ID, REQUESTER_ID);
    }

    // =========================================================================
    // Business Rule — BR-01 동일 방 참여자 검증
    // =========================================================================

    @Test
    @DisplayName("CustomException 발생 — BR-01 요청자가 해당 방 비참여자")
    void should_throw_CustomException_when_BR01_requester_not_in_room() {
        RequestCreateDisclosureDto dto = StudentIdDisclosureFixture.createSendRequestDto();
        given(transactionService.saveRequest(REQUESTER_ID, dto))
                .willThrow(new CustomException(ErrorCode.DISCLOSURE_NOT_IN_SAME_ROOM));

        ThrowingCallable action = () -> disclosureService.sendRequest(REQUESTER_ID, dto);

        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DISCLOSURE_NOT_IN_SAME_ROOM);
    }

    @Test
    @DisplayName("CustomException 발생 — BR-01 대상자가 해당 방 비참여자")
    void should_throw_CustomException_when_BR01_target_not_in_room() {
        RequestCreateDisclosureDto dto = StudentIdDisclosureFixture.createSendRequestDto();
        given(transactionService.saveRequest(REQUESTER_ID, dto))
                .willThrow(new CustomException(ErrorCode.DISCLOSURE_NOT_IN_SAME_ROOM));

        ThrowingCallable action = () -> disclosureService.sendRequest(REQUESTER_ID, dto);

        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DISCLOSURE_NOT_IN_SAME_ROOM);
    }

    // =========================================================================
    // Business Rule — BR-02 자기 자신 요청 불가 (INV-02)
    // =========================================================================

    @Test
    @DisplayName("CustomException 발생 — BR-02 자기 자신에게 요청 불가 (INV-02)")
    void should_throw_CustomException_when_BR02_request_to_self() {
        RequestCreateDisclosureDto dto = RequestCreateDisclosureDto.builder()
                .roomId(ROOM_ID)
                .targetId(REQUESTER_ID)
                .build();
        given(transactionService.saveRequest(REQUESTER_ID, dto))
                .willThrow(new CustomException(ErrorCode.DISCLOSURE_CANNOT_REQUEST_SELF));

        ThrowingCallable action = () -> disclosureService.sendRequest(REQUESTER_ID, dto);

        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DISCLOSURE_CANNOT_REQUEST_SELF);
    }

    // =========================================================================
    // Business Rule — BR-03 PENDING/ACCEPTED 중복 방지 (INV-01)
    // =========================================================================

    @Test
    @DisplayName("CustomException 발생 — BR-03 이미 PENDING 요청 존재 시 409 (INV-01)")
    void should_throw_CustomException_when_BR03_pending_already_exists() {
        RequestCreateDisclosureDto dto = StudentIdDisclosureFixture.createSendRequestDto();
        given(transactionService.saveRequest(REQUESTER_ID, dto))
                .willThrow(new CustomException(ErrorCode.DISCLOSURE_REQUEST_ALREADY_EXISTS));

        ThrowingCallable action = () -> disclosureService.sendRequest(REQUESTER_ID, dto);

        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DISCLOSURE_REQUEST_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("CustomException 발생 — BR-03 이미 ACCEPTED 요청 존재 시 409 (INV-01)")
    void should_throw_CustomException_when_BR03_accepted_already_exists() {
        RequestCreateDisclosureDto dto = StudentIdDisclosureFixture.createSendRequestDto();
        given(transactionService.saveRequest(REQUESTER_ID, dto))
                .willThrow(new CustomException(ErrorCode.DISCLOSURE_REQUEST_ALREADY_EXISTS));

        ThrowingCallable action = () -> disclosureService.sendRequest(REQUESTER_ID, dto);

        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DISCLOSURE_REQUEST_ALREADY_EXISTS);
    }

    // =========================================================================
    // Business Rule — BR-04 REJECTED/CANCELED 재요청 시 기존 삭제 후 신규 저장 (ADR-01)
    // =========================================================================

    @Test
    @DisplayName("재요청 성공 — BR-04 REJECTED 레코드 삭제 후 신규 PENDING 저장 (ADR-01)")
    void should_delete_rejected_and_save_new_pending_when_BR04_rerequest_after_rejected() {
        RequestCreateDisclosureDto dto = StudentIdDisclosureFixture.createSendRequestDto();
        given(transactionService.saveRequest(REQUESTER_ID, dto)).willReturn(REQUEST_ID);
        given(userRepository.findById(TARGET_ID)).willReturn(Optional.empty());

        ResponseDisclosureSendDto result = disclosureService.sendRequest(REQUESTER_ID, dto);

        then(transactionService).should(times(1)).saveRequest(REQUESTER_ID, dto);
        assertThat(result.getRequestId()).isNotNull();
    }

    @Test
    @DisplayName("재요청 성공 — BR-04 CANCELED 레코드 삭제 후 신규 PENDING 저장 (ADR-01)")
    void should_delete_canceled_and_save_new_pending_when_BR04_rerequest_after_canceled() {
        RequestCreateDisclosureDto dto = StudentIdDisclosureFixture.createSendRequestDto();
        given(transactionService.saveRequest(REQUESTER_ID, dto)).willReturn(REQUEST_ID);
        given(userRepository.findById(TARGET_ID)).willReturn(Optional.empty());

        ResponseDisclosureSendDto result = disclosureService.sendRequest(REQUESTER_ID, dto);

        then(transactionService).should(times(1)).saveRequest(REQUESTER_ID, dto);
        assertThat(result.getRequestId()).isNotNull();
    }

    // =========================================================================
    // Business Rule — BR-05 취소: 요청자 본인만 가능 (INV-03·04)
    // =========================================================================

    @Test
    @DisplayName("CustomException 발생 — BR-05 취소 시 요청자 본인 아님 (INV-03)")
    void should_throw_CustomException_when_BR05_cancel_by_non_requester() {
        Long otherUserId = 999L;
        willThrow(new CustomException(ErrorCode.DISCLOSURE_REQUEST_FORBIDDEN))
                .given(transactionService).cancelRequest(otherUserId, REQUEST_ID);

        ThrowingCallable action = () -> disclosureService.cancel(otherUserId, REQUEST_ID);

        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DISCLOSURE_REQUEST_FORBIDDEN);
    }

    @Test
    @DisplayName("CustomException 발생 — BR-05 취소 시 PENDING 상태 아님 (INV-04)")
    void should_throw_CustomException_when_BR05_cancel_non_pending_status() {
        willThrow(new CustomException(ErrorCode.DISCLOSURE_INVALID_STATUS))
                .given(transactionService).cancelRequest(REQUESTER_ID, REQUEST_ID);

        ThrowingCallable action = () -> disclosureService.cancel(REQUESTER_ID, REQUEST_ID);

        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DISCLOSURE_INVALID_STATUS);
    }

    // =========================================================================
    // Business Rule — BR-06 수락: 대상자 본인만 가능 (INV-03·04)
    // =========================================================================

    @Test
    @DisplayName("CustomException 발생 — BR-06 수락 시 대상자 본인 아님 (INV-03)")
    void should_throw_CustomException_when_BR06_accept_by_non_target() {
        Long otherUserId = 999L;
        given(transactionService.acceptRequest(otherUserId, REQUEST_ID))
                .willThrow(new CustomException(ErrorCode.DISCLOSURE_REQUEST_FORBIDDEN));

        ThrowingCallable action = () -> disclosureService.accept(otherUserId, REQUEST_ID);

        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DISCLOSURE_REQUEST_FORBIDDEN);
    }

    @Test
    @DisplayName("CustomException 발생 — BR-06 수락 시 PENDING 상태 아님 (INV-04)")
    void should_throw_CustomException_when_BR06_accept_non_pending_status() {
        given(transactionService.acceptRequest(TARGET_ID, REQUEST_ID))
                .willThrow(new CustomException(ErrorCode.DISCLOSURE_INVALID_STATUS));

        ThrowingCallable action = () -> disclosureService.accept(TARGET_ID, REQUEST_ID);

        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DISCLOSURE_INVALID_STATUS);
    }

    @Test
    @DisplayName("CustomException 발생 — BR-06 거절 시 대상자 본인 아님 (INV-03)")
    void should_throw_CustomException_when_BR06_reject_by_non_target() {
        Long otherUserId = 999L;
        given(transactionService.rejectRequest(otherUserId, REQUEST_ID))
                .willThrow(new CustomException(ErrorCode.DISCLOSURE_REQUEST_FORBIDDEN));

        ThrowingCallable action = () -> disclosureService.reject(otherUserId, REQUEST_ID);

        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DISCLOSURE_REQUEST_FORBIDDEN);
    }

    @Test
    @DisplayName("CustomException 발생 — BR-06 거절 시 PENDING 상태 아님 (INV-04)")
    void should_throw_CustomException_when_BR06_reject_non_pending_status() {
        given(transactionService.rejectRequest(TARGET_ID, REQUEST_ID))
                .willThrow(new CustomException(ErrorCode.DISCLOSURE_INVALID_STATUS));

        ThrowingCallable action = () -> disclosureService.reject(TARGET_ID, REQUEST_ID);

        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DISCLOSURE_INVALID_STATUS);
    }

    // =========================================================================
    // Edge Case
    // =========================================================================

    @Test
    @DisplayName("재요청 성공 — REJECTED 직후 즉시 재요청 허용 (BR-04 엣지케이스)")
    void should_succeed_when_rerequest_immediately_after_rejected() {
        RequestCreateDisclosureDto dto = StudentIdDisclosureFixture.createSendRequestDto();
        given(transactionService.saveRequest(REQUESTER_ID, dto)).willReturn(200L);
        given(userRepository.findById(TARGET_ID)).willReturn(Optional.empty());

        ResponseDisclosureSendDto result = disclosureService.sendRequest(REQUESTER_ID, dto);

        assertThat(result.getRequestId()).isEqualTo(200L);
    }

    @Test
    @DisplayName("요청 발송 성공 — 역방향(B→A) PENDING 중에 정방향(A→B) 요청은 별개 레코드로 허용")
    void should_succeed_when_forward_request_while_reverse_pending_exists() {
        RequestCreateDisclosureDto dto = StudentIdDisclosureFixture.createSendRequestDto();
        given(transactionService.saveRequest(REQUESTER_ID, dto)).willReturn(REQUEST_ID);
        given(userRepository.findById(TARGET_ID)).willReturn(Optional.empty());

        ResponseDisclosureSendDto result = disclosureService.sendRequest(REQUESTER_ID, dto);

        assertThat(result.getRequestId()).isNotNull();
    }

    @Test
    @DisplayName("상태 조회 성공 — 퇴장 후 조회 시 NONE 반환 (ACCEPTED 레코드 삭제됨)")
    void should_return_NONE_when_getStatus_after_leave() {
        given(disclosureRequestRepository.findByRoomIdAndRequesterIdAndTargetIdAndStatus(
                ROOM_ID, REQUESTER_ID, TARGET_ID, ACCEPTED)).willReturn(Optional.empty());
        given(disclosureRequestRepository.findByRoomIdAndRequesterIdAndTargetIdAndStatus(
                ROOM_ID, TARGET_ID, REQUESTER_ID, ACCEPTED)).willReturn(Optional.empty());
        given(disclosureRequestRepository.findByRoomIdAndRequesterIdAndTargetIdAndStatus(
                ROOM_ID, REQUESTER_ID, TARGET_ID, PENDING)).willReturn(Optional.empty());
        given(disclosureRequestRepository.findByRoomIdAndRequesterIdAndTargetIdAndStatus(
                ROOM_ID, TARGET_ID, REQUESTER_ID, PENDING)).willReturn(Optional.empty());

        ResponseDisclosureStatusDto result = disclosureService.getStatus(REQUESTER_ID, ROOM_ID, TARGET_ID);

        assertThat(result.getStatus()).isEqualTo("NONE");
    }

    // =========================================================================
    // Error Case — 존재하지 않는 리소스 조회
    // =========================================================================

    @Test
    @DisplayName("CustomException 발생 — 방 없음 시 OPEN_CHAT_ROOM_NOT_FOUND")
    void should_throw_CustomException_when_sendRequest_room_not_found() {
        RequestCreateDisclosureDto dto = StudentIdDisclosureFixture.createSendRequestDto();
        given(transactionService.saveRequest(REQUESTER_ID, dto))
                .willThrow(new CustomException(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND));

        ThrowingCallable action = () -> disclosureService.sendRequest(REQUESTER_ID, dto);

        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND);
    }

    @Test
    @DisplayName("CustomException 발생 — 취소 시 요청 없음 시 DISCLOSURE_REQUEST_NOT_FOUND")
    void should_throw_CustomException_when_cancel_request_not_found() {
        willThrow(new CustomException(ErrorCode.DISCLOSURE_REQUEST_NOT_FOUND))
                .given(transactionService).cancelRequest(REQUESTER_ID, REQUEST_ID);

        ThrowingCallable action = () -> disclosureService.cancel(REQUESTER_ID, REQUEST_ID);

        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DISCLOSURE_REQUEST_NOT_FOUND);
    }

    @Test
    @DisplayName("CustomException 발생 — 수락 시 요청 없음 시 DISCLOSURE_REQUEST_NOT_FOUND")
    void should_throw_CustomException_when_accept_request_not_found() {
        given(transactionService.acceptRequest(TARGET_ID, REQUEST_ID))
                .willThrow(new CustomException(ErrorCode.DISCLOSURE_REQUEST_NOT_FOUND));

        ThrowingCallable action = () -> disclosureService.accept(TARGET_ID, REQUEST_ID);

        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DISCLOSURE_REQUEST_NOT_FOUND);
    }

    @Test
    @DisplayName("CustomException 발생 — 거절 시 요청 없음 시 DISCLOSURE_REQUEST_NOT_FOUND")
    void should_throw_CustomException_when_reject_request_not_found() {
        given(transactionService.rejectRequest(TARGET_ID, REQUEST_ID))
                .willThrow(new CustomException(ErrorCode.DISCLOSURE_REQUEST_NOT_FOUND));

        ThrowingCallable action = () -> disclosureService.reject(TARGET_ID, REQUEST_ID);

        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DISCLOSURE_REQUEST_NOT_FOUND);
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private User createMockUser(Long userId, String studentNumber) {
        User user = User.createTestUser(studentNumber, "password", "name", null, null, Role.ROLE_USER);
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }
}
