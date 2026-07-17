package com.example.appcenter_project.domain.fcm.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

import static org.assertj.core.api.Assertions.assertThat;

class FcmOutboxProcessorScheduleTest {

    @Test
    @DisplayName("fixedDelay = 5000 — AC-9: FcmOutboxProcessor process() 스케줄 주기")
    void should_have_fixedDelay_5000_when_process_scheduled() throws NoSuchMethodException {
        // given
        Class<FcmOutboxProcessor> clazz = FcmOutboxProcessor.class;

        // when
        Scheduled scheduled = clazz
                .getDeclaredMethod("process")
                .getAnnotation(Scheduled.class);

        // then
        assertThat(scheduled.fixedDelay()).isEqualTo(5000L);
    }
}
