package com.example.appcenter_project.domain.openChat.fixture;

import com.example.appcenter_project.domain.openChat.dto.request.RequestCreateDerivedRoomDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseDerivedRoomCreatedDto;

import java.lang.reflect.Field;

public class OpenChatDerivedRoomLinkFixture {

    private static RequestCreateDerivedRoomDto buildRequest(
            Long originRoomId, String name, String description,
            Integer maxParticipants, Boolean isPublic, String password) {
        try {
            RequestCreateDerivedRoomDto dto = new RequestCreateDerivedRoomDto();
            setField(dto, "originRoomId", originRoomId);
            setField(dto, "name", name);
            setField(dto, "description", description);
            setField(dto, "maxParticipants", maxParticipants);
            setField(dto, "isPublic", isPublic);
            setField(dto, "password", password);
            return dto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException ignored) {
            // originRoomId 필드가 아직 없으면 무시 — 구현 후 존재
        }
    }

    public static RequestCreateDerivedRoomDto createRequest() {
        return buildRequest(1L, "토론방", "자유롭게 토론해요", 30, true, null);
    }

    public static RequestCreateDerivedRoomDto createRequestWithNullOriginRoomId() {
        return buildRequest(null, "토론방", "자유롭게 토론해요", 30, true, null);
    }

    public static RequestCreateDerivedRoomDto createRequestWithNullName() {
        return buildRequest(1L, null, "자유롭게 토론해요", 30, true, null);
    }

    public static RequestCreateDerivedRoomDto createRequestWithBlankName() {
        return buildRequest(1L, "   ", "자유롭게 토론해요", 30, true, null);
    }

    public static RequestCreateDerivedRoomDto createRequestWithNullMaxParticipants() {
        return buildRequest(1L, "토론방", "자유롭게 토론해요", null, true, null);
    }

    public static RequestCreateDerivedRoomDto createRequestWithNullIsPublic() {
        return buildRequest(1L, "토론방", "자유롭게 토론해요", 30, null, null);
    }

    public static RequestCreateDerivedRoomDto createRequestWithNullDescription() {
        return buildRequest(1L, "토론방", null, 30, true, null);
    }

    public static ResponseDerivedRoomCreatedDto createDerivedRoomCreatedResponse() {
        return ResponseDerivedRoomCreatedDto.of(42L);
    }
}
