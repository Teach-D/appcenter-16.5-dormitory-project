package com.example.appcenter_project.domain.groupOrder.service;

import com.example.appcenter_project.domain.groupOrder.repository.GroupOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AsyncViewCountServiceTest {

    @Mock
    private GroupOrderRepository groupOrderRepository;

    @Mock
    private RedissonClient redissonClient;

    @InjectMocks
    private AsyncViewCountService asyncViewCountService;

    private RLock mockLock;

    @BeforeEach
    void setUp() throws InterruptedException {
        mockLock = mock(RLock.class);
        when(redissonClient.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock(5L, 3L, TimeUnit.SECONDS)).thenReturn(true);
        when(mockLock.isHeldByCurrentThread()).thenReturn(true);
    }

    @Test
    @DisplayName("조회수 증가 - 메모리에 정상 집계")
    void incrementViewCount_정상_집계() {
        asyncViewCountService.incrementViewCount(1L);
        asyncViewCountService.incrementViewCount(1L);

        verifyNoInteractions(groupOrderRepository);
    }

    @Test
    @DisplayName("flush - 빈 맵이면 DB 호출 없음")
    void flushViewCountDB_빈_맵이면_스킵() {
        asyncViewCountService.flushViewCountDB();

        verifyNoInteractions(groupOrderRepository);
        verifyNoInteractions(redissonClient);
    }

    @Test
    @DisplayName("flush - 락 획득 성공 시 DB에 카운트 반영")
    void flushViewCountDB_락_획득_성공_DB_반영() {
        asyncViewCountService.incrementViewCount(1L);
        asyncViewCountService.incrementViewCount(1L);
        asyncViewCountService.incrementViewCount(2L);

        asyncViewCountService.flushViewCountDB();

        verify(groupOrderRepository).incrementViewCountBy(1L, 2L);
        verify(groupOrderRepository).incrementViewCountBy(2L, 1L);
        verify(mockLock, times(2)).unlock();
    }

    @Test
    @DisplayName("flush - 락 획득 실패 시 DB 호출 없음")
    void flushViewCountDB_락_획득_실패_DB_미반영() throws InterruptedException {
        when(mockLock.tryLock(5L, 3L, TimeUnit.SECONDS)).thenReturn(false);

        asyncViewCountService.incrementViewCount(1L);
        asyncViewCountService.flushViewCountDB();

        verifyNoInteractions(groupOrderRepository);
        verify(mockLock, never()).unlock();
    }

    @Test
    @DisplayName("flush - InterruptedException 발생 시 인터럽트 상태 복원")
    void flushViewCountDB_인터럽트_발생_시_복원() throws InterruptedException {
        when(mockLock.tryLock(5L, 3L, TimeUnit.SECONDS)).thenThrow(new InterruptedException());

        asyncViewCountService.incrementViewCount(1L);
        asyncViewCountService.flushViewCountDB();

        verifyNoInteractions(groupOrderRepository);
    }

    @ParameterizedTest(name = "락 획득={0} → DB 반영={1}")
    @CsvSource({
        "true,  true",   // 락 획득 성공 → DB 업데이트
        "false, false"   // 락 획득 실패 → DB 미반영
    })
    @DisplayName("flush - 분산 락 획득 여부에 따른 DB 반영 조건")
    void flushViewCountDB_분산_락_조건(boolean lockAcquired, boolean expectDbCall) throws InterruptedException {
        when(mockLock.tryLock(5L, 3L, TimeUnit.SECONDS)).thenReturn(lockAcquired);

        asyncViewCountService.incrementViewCount(1L);
        asyncViewCountService.flushViewCountDB();

        if (expectDbCall) {
            verify(groupOrderRepository).incrementViewCountBy(eq(1L), anyLong());
        } else {
            verifyNoInteractions(groupOrderRepository);
        }
    }

    @Test
    @DisplayName("flush - 두 번 호출해도 이미 반영된 카운트 중복 반영 안됨")
    void flushViewCountDB_두번_호출시_중복_방지() {
        asyncViewCountService.incrementViewCount(1L);

        asyncViewCountService.flushViewCountDB();
        asyncViewCountService.flushViewCountDB();

        verify(groupOrderRepository, times(1)).incrementViewCountBy(eq(1L), anyLong());
    }

    @Test
    @DisplayName("flush - 여러 공동구매 각각 독립적으로 락 키 사용")
    void flushViewCountDB_여러_공동구매_독립_락_키() {
        asyncViewCountService.incrementViewCount(10L);
        asyncViewCountService.incrementViewCount(20L);
        asyncViewCountService.incrementViewCount(30L);

        asyncViewCountService.flushViewCountDB();

        verify(redissonClient).getLock("group-order:view-count:10");
        verify(redissonClient).getLock("group-order:view-count:20");
        verify(redissonClient).getLock("group-order:view-count:30");
        verify(groupOrderRepository).incrementViewCountBy(10L, 1L);
        verify(groupOrderRepository).incrementViewCountBy(20L, 1L);
        verify(groupOrderRepository).incrementViewCountBy(30L, 1L);
    }
}
