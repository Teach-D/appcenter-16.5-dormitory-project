package com.example.appcenter_project.domain.groupOrder.service;

import com.example.appcenter_project.domain.groupOrder.repository.GroupOrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncViewCountServiceTest {

    @Mock
    private GroupOrderRepository groupOrderRepository;

    @InjectMocks
    private AsyncViewCountService asyncViewCountService;

    @Test
    @DisplayName("조회수 증가 - 메모리에 정상 집계")
    void incrementViewCount_정상_집계() {
        asyncViewCountService.incrementViewCount(1L);
        asyncViewCountService.incrementViewCount(1L);

        // DB 반영 없이 메모리에만 누적됨을 확인
        verifyNoInteractions(groupOrderRepository);
    }

    @Test
    @DisplayName("flush - 빈 맵이면 DB 호출 없음")
    void flushViewCountDB_빈_맵이면_스킵() {
        asyncViewCountService.flushViewCountDB();

        verifyNoInteractions(groupOrderRepository);
    }

    @Test
    @DisplayName("flush - 집계된 카운트를 DB에 반영하고 맵 초기화")
    void flushViewCountDB_카운트_DB_반영() {
        asyncViewCountService.incrementViewCount(1L);
        asyncViewCountService.incrementViewCount(1L);
        asyncViewCountService.incrementViewCount(2L);

        asyncViewCountService.flushViewCountDB();

        verify(groupOrderRepository).incrementViewCountBy(1L, 2L);
        verify(groupOrderRepository).incrementViewCountBy(2L, 1L);
    }

    @Test
    @DisplayName("flush - 두 번 호출해도 이미 반영된 카운트 중복 반영 안됨")
    void flushViewCountDB_두번_호출시_중복_방지() {
        asyncViewCountService.incrementViewCount(1L);

        asyncViewCountService.flushViewCountDB();
        asyncViewCountService.flushViewCountDB();

        // 두 번째 flush는 빈 맵이므로 DB 호출 1회만 발생
        verify(groupOrderRepository, times(1)).incrementViewCountBy(eq(1L), anyLong());
    }

    @Test
    @DisplayName("flush - 여러 공동구매 각각 독립적으로 DB 반영")
    void flushViewCountDB_여러_공동구매_독립_반영() {
        asyncViewCountService.incrementViewCount(10L);
        asyncViewCountService.incrementViewCount(20L);
        asyncViewCountService.incrementViewCount(30L);

        asyncViewCountService.flushViewCountDB();

        verify(groupOrderRepository).incrementViewCountBy(10L, 1L);
        verify(groupOrderRepository).incrementViewCountBy(20L, 1L);
        verify(groupOrderRepository).incrementViewCountBy(30L, 1L);
    }
}
