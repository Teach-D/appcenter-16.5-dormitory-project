package com.example.appcenter_project.global.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MdcTaskDecoratorTest {

    private final MdcTaskDecorator decorator = new MdcTaskDecorator();

    @Test
    @DisplayName("호출 스레드의 MDC traceId가 비동기 스레드에서 동일하게 조회된다")
    void mdc_propagated_to_async_thread() throws InterruptedException {
        MDC.put("traceId", "test-trace-123");
        MDC.put("userId", "42");

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> capturedTraceId = new AtomicReference<>();
        AtomicReference<String> capturedUserId = new AtomicReference<>();

        Runnable decorated = decorator.decorate(() -> {
            capturedTraceId.set(MDC.get("traceId"));
            capturedUserId.set(MDC.get("userId"));
            latch.countDown();
        });

        Thread asyncThread = new Thread(decorated);
        asyncThread.start();
        latch.await();

        assertThat(capturedTraceId.get()).isEqualTo("test-trace-123");
        assertThat(capturedUserId.get()).isEqualTo("42");

        MDC.clear();
    }

    @Test
    @DisplayName("비동기 스레드 실행 완료 후 MDC가 클린업된다")
    void mdc_cleared_after_async_execution() throws InterruptedException {
        MDC.put("traceId", "cleanup-test");

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> mdcAfterRun = new AtomicReference<>();

        Thread asyncThread = new Thread(() -> {
            Runnable decorated = decorator.decorate(() -> {});
            decorated.run();
            mdcAfterRun.set(MDC.get("traceId"));
            latch.countDown();
        });

        asyncThread.start();
        latch.await();

        assertThat(mdcAfterRun.get()).isNull();

        MDC.clear();
    }

    @Test
    @DisplayName("호출 스레드 MDC가 비어 있으면 비동기 스레드에서도 traceId가 null이다")
    void null_mdc_context_does_not_cause_exception() throws InterruptedException {
        MDC.clear();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> capturedTraceId = new AtomicReference<>("INITIAL");

        Runnable decorated = decorator.decorate(() -> {
            capturedTraceId.set(MDC.get("traceId"));
            latch.countDown();
        });

        new Thread(decorated).start();
        latch.await();

        assertThat(capturedTraceId.get()).isNull();
    }
}
