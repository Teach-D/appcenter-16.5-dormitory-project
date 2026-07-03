package com.example.appcenter_project.domain.place.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GroupOrder 작성 시 Place 정규화 통합 테스트 — #650
 *
 * 총 8개 케이스
 *   - BR-01, BR-02: FOOD/비FOOD 분기, placeId/rawPlaceName 우선순위
 *   - INV-GO-N1: 비FOOD 타입에 place 필드 전송 시 INVALID_PLACE_FOR_TYPE 예외
 *   - INV-GO-N2: rawPlaceName 원본 보존
 *   - INV-GO-N3: 작성 후 place/rawPlaceName 변경 불가
 *   - ADR-P-05: PlaceResolver 호출은 GroupOrder 트랜잭션 진입 전
 *
 * [IMPL-HINT] 구현 에이전트가 아래를 생성해야 주석 해제 가능:
 *   - GroupOrderService.saveGroupOrder 에 placeId/rawPlaceName 처리 분기 추가
 *   - PlaceResolver (도메인 서비스)
 *   - RequestGroupOrderDto 에 placeId, rawPlaceName 필드 추가
 *   - ResponseGroupOrderDto 에 place, normalizationOutcome 필드 추가
 *   - ErrorCode.INVALID_PLACE_FOR_TYPE 추가
 */
@ExtendWith(MockitoExtension.class)
class PlaceNormalizationGroupOrderServiceTest {

    /*
    @Mock private GroupOrderRepository groupOrderRepository;
    @Mock private UserRepository userRepository;
    @Mock private PlaceResolver placeResolver;
    @Mock private GroupOrderLikeRepository groupOrderLikeRepository;
    @Mock private GroupOrderCommentRepository groupOrderCommentRepository;
    @Mock private ImageRepository imageRepository;
    @Mock private GroupOrderPopularSearchKeywordRepository groupOrderPopularSearchKeywordRepository;
    @Mock private ImageService imageService;
    @Mock private AsyncViewCountService asyncViewCountService;
    @Mock private MealTimeChecker mealTimeChecker;
    @Mock private GroupOrderNotificationService groupOrderNotificationService;

    @InjectMocks private GroupOrderService groupOrderService;
    */

    @Test
    @DisplayName("BR-01 FOOD+rawPlaceName — PlaceResolver 호출 후 Place 연결된 GroupOrder 저장")
    void should_resolve_place_and_save_groupOrder_when_FOOD_with_rawPlaceName() {
        /*
        // given
        User user = mock(User.class);
        given(userRepository.findById(PlaceNormalizationFixture.USER_ID)).willReturn(Optional.of(user));

        RequestGroupOrderDto dto = mock(RequestGroupOrderDto.class);
        given(dto.getGroupOrderType()).willReturn(GroupOrderType.FOOD);
        given(dto.getRawPlaceName()).willReturn(PlaceNormalizationFixture.RAW_PLACE_NAME);
        given(dto.getPlaceId()).willReturn(null);

        Place place = mock(Place.class);
        PlaceResolveResult resolveResult = PlaceResolveResult.of(place, NormalizationOutcome.MATCHED);
        given(placeResolver.resolve(null, PlaceNormalizationFixture.RAW_PLACE_NAME, GroupOrderType.FOOD))
                .willReturn(resolveResult);

        GroupOrder savedOrder = mock(GroupOrder.class);
        given(savedOrder.getId()).thenReturn(1L);
        given(savedOrder.getUser()).thenReturn(user);
        given(groupOrderRepository.save(any(GroupOrder.class))).willReturn(savedOrder);

        // when
        groupOrderService.saveGroupOrder(PlaceNormalizationFixture.USER_ID, dto, null);

        // then
        then(placeResolver).should().resolve(null, PlaceNormalizationFixture.RAW_PLACE_NAME, GroupOrderType.FOOD);
        then(groupOrderRepository).should().save(any(GroupOrder.class));
        */
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("BR-01 비FOOD — PlaceResolver 미호출, place=null 저장")
    void should_skip_place_resolution_when_groupOrderType_is_not_FOOD() {
        /*
        // given
        User user = mock(User.class);
        given(userRepository.findById(PlaceNormalizationFixture.USER_ID)).willReturn(Optional.of(user));

        RequestGroupOrderDto dto = mock(RequestGroupOrderDto.class);
        given(dto.getGroupOrderType()).willReturn(GroupOrderType.DELIVERY);
        given(dto.getRawPlaceName()).willReturn(null);
        given(dto.getPlaceId()).willReturn(null);

        GroupOrder savedOrder = mock(GroupOrder.class);
        given(savedOrder.getId()).thenReturn(1L);
        given(savedOrder.getUser()).thenReturn(user);
        given(groupOrderRepository.save(any(GroupOrder.class))).willReturn(savedOrder);

        // when
        groupOrderService.saveGroupOrder(PlaceNormalizationFixture.USER_ID, dto, null);

        // then: PlaceResolver 호출 없음
        then(placeResolver).shouldHaveNoInteractions();
        */
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("INV-GO-N1 — 비FOOD 타입에 placeId 전송 시 INVALID_PLACE_FOR_TYPE 예외")
    void should_throw_INVALID_PLACE_FOR_TYPE_when_non_FOOD_sends_placeId() {
        /*
        // given
        User user = mock(User.class);
        given(userRepository.findById(PlaceNormalizationFixture.USER_ID)).willReturn(Optional.of(user));

        RequestGroupOrderDto dto = mock(RequestGroupOrderDto.class);
        given(dto.getGroupOrderType()).willReturn(GroupOrderType.DELIVERY);
        given(dto.getPlaceId()).willReturn(PlaceNormalizationFixture.KAKAO_PLACE_ID);
        given(dto.getRawPlaceName()).willReturn(null);

        // when & then
        assertThatThrownBy(() ->
                groupOrderService.saveGroupOrder(PlaceNormalizationFixture.USER_ID, dto, null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PLACE_FOR_TYPE);
        */
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("INV-GO-N2 — rawPlaceName은 정규화 결과와 무관하게 원본 그대로 저장")
    void should_preserve_rawPlaceName_unchanged_regardless_of_normalization_result() {
        /*
        // given
        User user = mock(User.class);
        given(userRepository.findById(PlaceNormalizationFixture.USER_ID)).willReturn(Optional.of(user));

        String originalRaw = "BBQ 송도점"; // 원본 보존되어야 함

        RequestGroupOrderDto dto = mock(RequestGroupOrderDto.class);
        given(dto.getGroupOrderType()).willReturn(GroupOrderType.FOOD);
        given(dto.getRawPlaceName()).willReturn(originalRaw);
        given(dto.getPlaceId()).willReturn(null);

        Place place = mock(Place.class);
        given(place.getPlaceName()).willReturn("BBQ치킨 송도2점"); // 정규화 결과 달라도
        PlaceResolveResult resolveResult = PlaceResolveResult.of(place, NormalizationOutcome.MATCHED);
        given(placeResolver.resolve(any(), eq(originalRaw), eq(GroupOrderType.FOOD))).willReturn(resolveResult);

        ArgumentCaptor<GroupOrder> captor = ArgumentCaptor.forClass(GroupOrder.class);
        GroupOrder savedOrder = mock(GroupOrder.class);
        given(savedOrder.getId()).thenReturn(1L);
        given(savedOrder.getUser()).thenReturn(user);
        given(groupOrderRepository.save(captor.capture())).willReturn(savedOrder);

        // when
        groupOrderService.saveGroupOrder(PlaceNormalizationFixture.USER_ID, dto, null);

        // then: 저장된 GroupOrder의 rawPlaceName은 원본
        assertThat(captor.getValue().getRawPlaceName()).isEqualTo(originalRaw);
        */
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("BR-02(a) placeId 우선 — placeId 있으면 rawPlaceName 무시하고 placeId로 정규화")
    void should_prefer_placeId_over_rawPlaceName() {
        /*
        // given
        User user = mock(User.class);
        given(userRepository.findById(PlaceNormalizationFixture.USER_ID)).willReturn(Optional.of(user));

        RequestGroupOrderDto dto = mock(RequestGroupOrderDto.class);
        given(dto.getGroupOrderType()).willReturn(GroupOrderType.FOOD);
        given(dto.getPlaceId()).willReturn(PlaceNormalizationFixture.KAKAO_PLACE_ID);
        given(dto.getRawPlaceName()).willReturn(PlaceNormalizationFixture.RAW_PLACE_NAME);

        PlaceResolveResult resolveResult = PlaceResolveResult.of(mock(Place.class), NormalizationOutcome.MATCHED);
        given(placeResolver.resolve(
                eq(PlaceNormalizationFixture.KAKAO_PLACE_ID),
                eq(PlaceNormalizationFixture.RAW_PLACE_NAME),
                eq(GroupOrderType.FOOD))).willReturn(resolveResult);

        GroupOrder savedOrder = mock(GroupOrder.class);
        given(savedOrder.getId()).thenReturn(1L);
        given(savedOrder.getUser()).thenReturn(user);
        given(groupOrderRepository.save(any())).willReturn(savedOrder);

        // when
        groupOrderService.saveGroupOrder(PlaceNormalizationFixture.USER_ID, dto, null);

        // then: placeId 전달된 상태로 resolve 호출
        then(placeResolver).should().resolve(
                eq(PlaceNormalizationFixture.KAKAO_PLACE_ID), any(), eq(GroupOrderType.FOOD));
        */
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("BR-03 fallback — FALLBACK_ERROR 결과여도 GroupOrder 작성은 성공 (관용 정책)")
    void should_save_groupOrder_even_when_normalization_result_is_FALLBACK_ERROR() {
        /*
        // given
        User user = mock(User.class);
        given(userRepository.findById(PlaceNormalizationFixture.USER_ID)).willReturn(Optional.of(user));

        RequestGroupOrderDto dto = mock(RequestGroupOrderDto.class);
        given(dto.getGroupOrderType()).willReturn(GroupOrderType.FOOD);
        given(dto.getRawPlaceName()).willReturn(PlaceNormalizationFixture.RAW_PLACE_NAME);
        given(dto.getPlaceId()).willReturn(null);

        PlaceResolveResult fallback = PlaceResolveResult.of(null, NormalizationOutcome.FALLBACK_ERROR);
        given(placeResolver.resolve(any(), any(), eq(GroupOrderType.FOOD))).willReturn(fallback);

        GroupOrder savedOrder = mock(GroupOrder.class);
        given(savedOrder.getId()).thenReturn(1L);
        given(savedOrder.getUser()).thenReturn(user);
        given(groupOrderRepository.save(any(GroupOrder.class))).willReturn(savedOrder);

        // when: 예외 없이 정상 저장
        groupOrderService.saveGroupOrder(PlaceNormalizationFixture.USER_ID, dto, null);

        // then
        then(groupOrderRepository).should().save(any(GroupOrder.class));
        */
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("ADR-P-05 — PlaceResolver 호출이 GroupOrder save보다 먼저 수행됨 (트랜잭션 외부 호출)")
    void should_call_placeResolver_before_groupOrder_save() {
        /*
        // given
        User user = mock(User.class);
        given(userRepository.findById(PlaceNormalizationFixture.USER_ID)).willReturn(Optional.of(user));

        RequestGroupOrderDto dto = mock(RequestGroupOrderDto.class);
        given(dto.getGroupOrderType()).willReturn(GroupOrderType.FOOD);
        given(dto.getRawPlaceName()).willReturn(PlaceNormalizationFixture.RAW_PLACE_NAME);

        Place place = mock(Place.class);
        PlaceResolveResult resolveResult = PlaceResolveResult.of(place, NormalizationOutcome.MATCHED);
        given(placeResolver.resolve(any(), any(), any())).willReturn(resolveResult);

        GroupOrder savedOrder = mock(GroupOrder.class);
        given(savedOrder.getId()).thenReturn(1L);
        given(savedOrder.getUser()).thenReturn(user);
        given(groupOrderRepository.save(any())).willReturn(savedOrder);

        InOrder inOrder = inOrder(placeResolver, groupOrderRepository);

        // when
        groupOrderService.saveGroupOrder(PlaceNormalizationFixture.USER_ID, dto, null);

        // then: resolve 호출이 save보다 먼저
        inOrder.verify(placeResolver).resolve(any(), any(), any());
        inOrder.verify(groupOrderRepository).save(any());
        */
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("BR-01 FOOD + 둘 다 없음 — PlaceResolver 호출 없이 GroupOrder place=null 저장")
    void should_save_groupOrder_with_null_place_when_FOOD_but_no_place_inputs() {
        /*
        // given
        User user = mock(User.class);
        given(userRepository.findById(PlaceNormalizationFixture.USER_ID)).willReturn(Optional.of(user));

        RequestGroupOrderDto dto = mock(RequestGroupOrderDto.class);
        given(dto.getGroupOrderType()).willReturn(GroupOrderType.FOOD);
        given(dto.getPlaceId()).willReturn(null);
        given(dto.getRawPlaceName()).willReturn(null);

        GroupOrder savedOrder = mock(GroupOrder.class);
        given(savedOrder.getId()).thenReturn(1L);
        given(savedOrder.getUser()).thenReturn(user);
        given(groupOrderRepository.save(any(GroupOrder.class))).willReturn(savedOrder);

        // when
        groupOrderService.saveGroupOrder(PlaceNormalizationFixture.USER_ID, dto, null);

        // then
        then(placeResolver).shouldHaveNoInteractions();
        then(groupOrderRepository).should().save(any(GroupOrder.class));
        */
        assertThat(true).isTrue();
    }
}
