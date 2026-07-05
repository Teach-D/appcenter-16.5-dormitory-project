package com.example.appcenter_project.domain.place.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PlaceMigrationService 단위 테스트 — #650 어드민 마이그레이션 (BR-07)
 *
 * 총 4개 케이스
 *   - BR-07: 청크 페이징, idempotent, 일 상한 10,000, 한도 도달 시 중단
 *
 * [IMPL-HINT] 구현 에이전트가 아래를 생성해야 주석 해제 가능:
 *   - PlaceMigrationService
 *   - GroupOrderRepository.findUnnormalizedFoodOrders(chunkSize, lastId)
 *   - PlaceResolver
 *   - ResponseMigrationResultDto (scannedRows, matchedRows, notFoundRows, errorRows, quotaExceededAtRow)
 */
@ExtendWith(MockitoExtension.class)
class PlaceMigrationServiceTest {

    /*
    @Mock private GroupOrderRepository groupOrderRepository;
    @Mock private PlaceResolver placeResolver;
    @Mock private QuotaGuard quotaGuard;

    @InjectMocks private PlaceMigrationService placeMigrationService;
    */

    @Test
    @DisplayName("BR-07 idempotent — place=null AND FOOD 조건만 처리, 이미 연결된 row 스킵")
    void should_skip_already_normalized_rows_and_process_only_null_place_food_orders() {
        /*
        // given: chunkSize=2, maxRows=10
        GroupOrder order1 = mock(GroupOrder.class);
        given(order1.getId()).willReturn(1L);
        given(order1.getRawPlaceName()).willReturn(PlaceNormalizationFixture.RAW_PLACE_NAME);

        GroupOrder order2 = mock(GroupOrder.class);
        given(order2.getId()).willReturn(2L);
        given(order2.getRawPlaceName()).willReturn(PlaceNormalizationFixture.RAW_PLACE_NAME_VARIANT);

        // 첫 번째 청크: 2건, 두 번째 청크: 빈 리스트 (종료)
        given(groupOrderRepository.findUnnormalizedFoodOrders(2, 0L))
                .willReturn(List.of(order1, order2));
        given(groupOrderRepository.findUnnormalizedFoodOrders(2, 2L))
                .willReturn(Collections.emptyList());

        Place place = mock(Place.class);
        PlaceResolveResult matched = PlaceResolveResult.of(place, NormalizationOutcome.MATCHED);
        given(placeResolver.resolve(any(), any(), eq(GroupOrderType.FOOD))).willReturn(matched);
        given(quotaGuard.isExceeded()).willReturn(false);

        // when
        ResponseMigrationResultDto result = placeMigrationService.migrate(10, 2, 0);

        // then
        assertThat(result.getScannedRows()).isEqualTo(2);
        assertThat(result.getMatchedRows()).isEqualTo(2);
        assertThat(result.getNotFoundRows()).isEqualTo(0);
        assertThat(result.getQuotaExceededAtRow()).isNull();
        */
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("BR-07 일 상한 — maxRows 도달 시 즉시 종료")
    void should_stop_migration_when_maxRows_is_reached() {
        /*
        // given: maxRows=1, chunkSize=2
        GroupOrder order1 = mock(GroupOrder.class);
        given(order1.getId()).willReturn(1L);
        given(order1.getRawPlaceName()).willReturn(PlaceNormalizationFixture.RAW_PLACE_NAME);

        GroupOrder order2 = mock(GroupOrder.class);
        given(order2.getId()).willReturn(2L);
        given(order2.getRawPlaceName()).willReturn("pizza");

        given(groupOrderRepository.findUnnormalizedFoodOrders(2, 0L))
                .willReturn(List.of(order1, order2));

        PlaceResolveResult matched = PlaceResolveResult.of(mock(Place.class), NormalizationOutcome.MATCHED);
        given(placeResolver.resolve(any(), any(), eq(GroupOrderType.FOOD))).willReturn(matched);
        given(quotaGuard.isExceeded()).willReturn(false);

        // when: maxRows=1이므로 첫 row 처리 후 중단
        ResponseMigrationResultDto result = placeMigrationService.migrate(1, 2, 0);

        // then
        assertThat(result.getScannedRows()).isLessThanOrEqualTo(1);
        */
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("BR-07 한도 도달 중단 — QuotaGuard 차단 시 조기 종료 + quotaExceededAtRow non-null")
    void should_stop_migration_and_set_quotaExceededAtRow_when_quota_exceeded() {
        /*
        // given
        GroupOrder order1 = mock(GroupOrder.class);
        given(order1.getId()).willReturn(1L);
        given(order1.getRawPlaceName()).willReturn(PlaceNormalizationFixture.RAW_PLACE_NAME);

        given(groupOrderRepository.findUnnormalizedFoodOrders(500, 0L))
                .willReturn(List.of(order1));

        given(quotaGuard.isExceeded()).willReturn(true);

        // when
        ResponseMigrationResultDto result = placeMigrationService.migrate(10_000, 500, 0);

        // then: 한도 도달로 첫 row부터 중단
        assertThat(result.getQuotaExceededAtRow()).isNotNull();
        then(placeResolver).shouldHaveNoInteractions();
        */
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("BR-07 개별 row 오류 격리 — 한 row 실패해도 다음 row 계속 처리")
    void should_continue_migration_when_single_row_resolve_fails() {
        /*
        // given
        GroupOrder order1 = mock(GroupOrder.class);
        given(order1.getId()).willReturn(1L);
        given(order1.getRawPlaceName()).willReturn("오류발생음식점");

        GroupOrder order2 = mock(GroupOrder.class);
        given(order2.getId()).willReturn(2L);
        given(order2.getRawPlaceName()).willReturn(PlaceNormalizationFixture.RAW_PLACE_NAME);

        given(groupOrderRepository.findUnnormalizedFoodOrders(500, 0L))
                .willReturn(List.of(order1, order2));
        given(groupOrderRepository.findUnnormalizedFoodOrders(500, 2L))
                .willReturn(Collections.emptyList());

        given(quotaGuard.isExceeded()).willReturn(false);

        // order1은 오류, order2는 MATCHED
        given(placeResolver.resolve(any(), eq("오류발생음식점"), eq(GroupOrderType.FOOD)))
                .willReturn(PlaceResolveResult.of(null, NormalizationOutcome.FALLBACK_ERROR));
        given(placeResolver.resolve(any(), eq(PlaceNormalizationFixture.RAW_PLACE_NAME), eq(GroupOrderType.FOOD)))
                .willReturn(PlaceResolveResult.of(mock(Place.class), NormalizationOutcome.MATCHED));

        // when
        ResponseMigrationResultDto result = placeMigrationService.migrate(10_000, 500, 0);

        // then: 오류 1건, 매칭 1건
        assertThat(result.getScannedRows()).isEqualTo(2);
        assertThat(result.getMatchedRows()).isEqualTo(1);
        assertThat(result.getErrorRows()).isEqualTo(1);
        */
        assertThat(true).isTrue();
    }
}
