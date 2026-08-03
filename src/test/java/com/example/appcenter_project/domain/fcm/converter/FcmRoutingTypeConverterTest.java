package com.example.appcenter_project.domain.fcm.converter;

import com.example.appcenter_project.domain.fcm.enums.FcmRoutingType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FcmRoutingTypeConverterTest {

    private final FcmRoutingTypeConverter converter = new FcmRoutingTypeConverter();

    @Test
    @DisplayName("CHAT_OPEN 저장 — convertToDatabaseColumn CHAT_OPEN 반환")
    void should_return_CHAT_OPEN_string_when_convert_CHAT_OPEN_to_database_column() {
        // given
        FcmRoutingType routingType = FcmRoutingType.CHAT_OPEN;

        // when
        String result = converter.convertToDatabaseColumn(routingType);

        // then
        assertThat(result).isEqualTo("CHAT_OPEN");
    }

    @Test
    @DisplayName("null 저장 — convertToDatabaseColumn null 반환")
    void should_return_null_when_convert_null_to_database_column() {
        // given / when
        String result = converter.convertToDatabaseColumn(null);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("ANNOUNCEMENT 조회 — convertToEntityAttribute ANNOUNCEMENT 반환")
    void should_return_ANNOUNCEMENT_when_convert_ANNOUNCEMENT_string_to_entity_attribute() {
        // given
        String dbValue = "ANNOUNCEMENT";

        // when
        FcmRoutingType result = converter.convertToEntityAttribute(dbValue);

        // then
        assertThat(result).isEqualTo(FcmRoutingType.ANNOUNCEMENT);
    }

    @Test
    @DisplayName("구 값 CHAT 조회 → null 반환 — DB 레거시 값 null 처리")
    void should_return_null_when_convert_legacy_CHAT_string_to_entity_attribute() {
        // given
        String legacyValue = "CHAT";

        // when
        FcmRoutingType result = converter.convertToEntityAttribute(legacyValue);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("구 값 NOTICE 조회 → null 반환 — DB 레거시 값 null 처리")
    void should_return_null_when_convert_legacy_NOTICE_string_to_entity_attribute() {
        // given
        String legacyValue = "NOTICE";

        // when
        FcmRoutingType result = converter.convertToEntityAttribute(legacyValue);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("null 조회 → null 반환 — convertToEntityAttribute null 입력")
    void should_return_null_when_convert_null_string_to_entity_attribute() {
        // given / when
        FcmRoutingType result = converter.convertToEntityAttribute(null);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("알 수 없는 값 조회 → null 반환 — 미등록 DB 값 null 처리")
    void should_return_null_when_convert_unknown_string_to_entity_attribute() {
        // given
        String unknownValue = "UNKNOWN_TYPE";

        // when
        FcmRoutingType result = converter.convertToEntityAttribute(unknownValue);

        // then
        assertThat(result).isNull();
    }
}
