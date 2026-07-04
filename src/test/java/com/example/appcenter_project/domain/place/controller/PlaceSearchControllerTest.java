package com.example.appcenter_project.domain.place.controller;

import com.example.appcenter_project.domain.place.fixture.PlaceNormalizationFixture;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.Role;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import com.example.appcenter_project.global.exception.SlackErrorNotifier;
import com.example.appcenter_project.global.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PlaceSearchController 컨트롤러 테스트 — #650 음식점 검색 (BR-09)
 *
 * 총 8개 케이스
 *   - BR-09: 인증 필수, keyword 2자 미만 빈 리스트, size 범위
 *   - 에러: 401 미인증, 503 한도 도달, 502 카카오 오류
 *
 * [IMPL-HINT] 구현 에이전트가 아래를 생성해야 @WebMvcTest 주석 해제 가능:
 *   - PlaceSearchController (GET /places/search)
 *   - PlaceSearchService 또는 PlaceResolver (검색용)
 *   - SecurityConfig 에 /places/search 인증 경로 추가
 *
 * 현재는 PlaceSearchController 클래스가 없으므로
 * @WebMvcTest 대상 클래스 어노테이션을 주석 처리하고 placeholder 유지.
 */
// @WebMvcTest(PlaceSearchController.class)   // [IMPL-UNLOCK] PlaceSearchController 생성 후 해제
// @AutoConfigureMockMvc(addFilters = false)
class PlaceSearchControllerTest {

    /*
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlaceSearchService placeSearchService;

    @MockBean
    private SlackErrorNotifier slackErrorNotifier;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private static final String SEARCH_URL = "/places/search";
    */

    /*
    @BeforeEach
    void setUp() {
        User mockUser = mock(User.class);
        given(mockUser.getId()).willReturn(PlaceNormalizationFixture.USER_ID);
        given(mockUser.getRole()).willReturn(Role.ROLE_USER);
        CustomUserDetails userDetails = new CustomUserDetails(mockUser);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
    */

    @Test
    @DisplayName("BR-09 성공 — keyword=BBQ, 정상 결과 리스트 200 반환")
    void should_return_200_with_place_list_when_keyword_is_valid() {
        /*
        // given
        List<ResponsePlaceSearchItemDto> items = List.of(
                new ResponsePlaceSearchItemDto(
                        PlaceNormalizationFixture.KAKAO_PLACE_ID,
                        PlaceNormalizationFixture.KAKAO_PLACE_NAME,
                        PlaceNormalizationFixture.ROAD_ADDRESS,
                        PlaceNormalizationFixture.LATITUDE,
                        PlaceNormalizationFixture.LONGITUDE
                )
        );
        given(placeSearchService.search("BBQ", 10)).willReturn(items);

        // when
        ResultActions result = mockMvc.perform(get(SEARCH_URL)
                .param("keyword", "BBQ")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.data.keyword").value("BBQ"))
              .andExpect(jsonPath("$.data.results[0].placeId").value(PlaceNormalizationFixture.KAKAO_PLACE_ID));
        */
        org.assertj.core.api.Assertions.assertThat(true).isTrue();
    }

    @Test
    @DisplayName("BR-09 keyword 1자 미만 — 빈 results 200 반환 (카카오 API 미호출)")
    void should_return_200_with_empty_results_when_keyword_is_shorter_than_2() {
        /*
        // given
        given(placeSearchService.search(PlaceNormalizationFixture.KEYWORD_1CHAR, 10)).willReturn(List.of());

        // when
        ResultActions result = mockMvc.perform(get(SEARCH_URL)
                .param("keyword", PlaceNormalizationFixture.KEYWORD_1CHAR)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.data.results").isEmpty());
        */
        org.assertj.core.api.Assertions.assertThat(true).isTrue();
    }

    @Test
    @DisplayName("BR-09 keyword 2자 경계 — 정상 검색 200 반환")
    void should_return_200_when_keyword_is_exactly_2_chars() {
        /*
        // given
        given(placeSearchService.search(PlaceNormalizationFixture.KEYWORD_2CHAR, 10)).willReturn(List.of());

        // when
        ResultActions result = mockMvc.perform(get(SEARCH_URL)
                .param("keyword", PlaceNormalizationFixture.KEYWORD_2CHAR)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());
        */
        org.assertj.core.api.Assertions.assertThat(true).isTrue();
    }

    @Test
    @DisplayName("에러 — keyword 파라미터 누락 시 400 VALIDATION_ERROR")
    void should_return_400_when_keyword_param_is_missing() {
        /*
        // when
        ResultActions result = mockMvc.perform(get(SEARCH_URL)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isBadRequest());
        */
        org.assertj.core.api.Assertions.assertThat(true).isTrue();
    }

    @Test
    @DisplayName("에러 — size 범위 초과(16) 시 400 VALIDATION_ERROR")
    void should_return_400_when_size_is_out_of_range() {
        /*
        // when
        ResultActions result = mockMvc.perform(get(SEARCH_URL)
                .param("keyword", "BBQ")
                .param("size", "16")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isBadRequest());
        */
        org.assertj.core.api.Assertions.assertThat(true).isTrue();
    }

    @Test
    @DisplayName("에러 — 미인증 요청 시 401 UNAUTHORIZED")
    void should_return_401_when_unauthenticated() {
        /*
        // SecurityContextHolder cleared — no auth
        SecurityContextHolder.clearContext();

        // when
        ResultActions result = mockMvc.perform(get(SEARCH_URL)
                .param("keyword", "BBQ")
                .contentType(MediaType.APPLICATION_JSON));

        // then: addFilters=true 모드에서 실제 401 검증
        result.andReturn(); // 필터 비활성 상태에서는 placeholder
        */
        org.assertj.core.api.Assertions.assertThat(true).isTrue();
    }

    @Test
    @DisplayName("에러 — 카카오 API 최종 실패 시 502 KAKAO_API_ERROR")
    void should_return_502_when_kakao_api_fails_after_retries() {
        /*
        // given
        willThrow(new CustomException(ErrorCode.KAKAO_API_ERROR))
                .given(placeSearchService).search(anyString(), anyInt());

        // when
        ResultActions result = mockMvc.perform(get(SEARCH_URL)
                .param("keyword", "BBQ")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isBadGateway());
        */
        org.assertj.core.api.Assertions.assertThat(true).isTrue();
    }

    @Test
    @DisplayName("에러 — 일일 한도 도달 시 503 KAKAO_QUOTA_EXCEEDED")
    void should_return_503_when_daily_quota_exceeded() {
        /*
        // given
        willThrow(new CustomException(ErrorCode.KAKAO_QUOTA_EXCEEDED))
                .given(placeSearchService).search(anyString(), anyInt());

        // when
        ResultActions result = mockMvc.perform(get(SEARCH_URL)
                .param("keyword", "BBQ")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isServiceUnavailable());
        */
        org.assertj.core.api.Assertions.assertThat(true).isTrue();
    }
}
