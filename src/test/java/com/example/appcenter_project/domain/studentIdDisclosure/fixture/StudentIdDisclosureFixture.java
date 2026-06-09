package com.example.appcenter_project.domain.studentIdDisclosure.fixture;

import com.example.appcenter_project.domain.studentIdDisclosure.dto.request.RequestCreateDisclosureDto;
import com.example.appcenter_project.domain.studentIdDisclosure.dto.response.ResponseDisclosureAcceptDto;
import com.example.appcenter_project.domain.studentIdDisclosure.dto.response.ResponseDisclosureSendDto;
import com.example.appcenter_project.domain.studentIdDisclosure.dto.response.ResponseDisclosureStatusDto;
import com.example.appcenter_project.domain.studentIdDisclosure.entity.StudentIdDisclosureRequest;

public class StudentIdDisclosureFixture {

    public static final Long REQUESTER_ID = 1L;
    public static final Long TARGET_ID = 2L;
    public static final Long ROOM_ID = 10L;
    public static final Long REQUEST_ID = 100L;
    public static final String REQUESTER_STUDENT_NUMBER = "20210001";
    public static final String TARGET_STUDENT_NUMBER = "20210002";

    public static StudentIdDisclosureRequest createPendingRequest() {
        return StudentIdDisclosureRequest.create(REQUESTER_ID, TARGET_ID, ROOM_ID);
    }

    public static StudentIdDisclosureRequest createAcceptedRequest() {
        StudentIdDisclosureRequest req = createPendingRequest();
        req.accept();
        return req;
    }

    public static StudentIdDisclosureRequest createRejectedRequest() {
        StudentIdDisclosureRequest req = createPendingRequest();
        req.reject();
        return req;
    }

    public static StudentIdDisclosureRequest createCanceledRequest() {
        StudentIdDisclosureRequest req = createPendingRequest();
        req.cancel();
        return req;
    }

    public static RequestCreateDisclosureDto createSendRequestDto() {
        return RequestCreateDisclosureDto.builder()
                .roomId(ROOM_ID)
                .targetId(TARGET_ID)
                .build();
    }

    public static RequestCreateDisclosureDto createSendRequestDtoWithNullRoomId() {
        return RequestCreateDisclosureDto.builder()
                .targetId(TARGET_ID)
                .build();
    }

    public static RequestCreateDisclosureDto createSendRequestDtoWithNullTargetId() {
        return RequestCreateDisclosureDto.builder()
                .roomId(ROOM_ID)
                .build();
    }

    public static ResponseDisclosureSendDto createSendResponse() {
        return ResponseDisclosureSendDto.builder()
                .requestId(REQUEST_ID)
                .build();
    }

    public static ResponseDisclosureAcceptDto createAcceptResponse() {
        return ResponseDisclosureAcceptDto.builder()
                .requestId(REQUEST_ID)
                .requesterStudentNumber(REQUESTER_STUDENT_NUMBER)
                .build();
    }

    public static ResponseDisclosureStatusDto createDisclosedStatusResponse() {
        return ResponseDisclosureStatusDto.builder()
                .status("DISCLOSED")
                .requestId(REQUEST_ID)
                .targetStudentNumber(TARGET_STUDENT_NUMBER)
                .build();
    }

    public static ResponseDisclosureStatusDto createPendingSentStatusResponse() {
        return ResponseDisclosureStatusDto.builder()
                .status("PENDING_SENT")
                .requestId(REQUEST_ID)
                .build();
    }

    public static ResponseDisclosureStatusDto createPendingReceivedStatusResponse() {
        return ResponseDisclosureStatusDto.builder()
                .status("PENDING_RECEIVED")
                .requestId(REQUEST_ID)
                .build();
    }

    public static ResponseDisclosureStatusDto createNoneStatusResponse() {
        return ResponseDisclosureStatusDto.builder()
                .status("NONE")
                .build();
    }
}
