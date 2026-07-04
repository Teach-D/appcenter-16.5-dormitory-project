package com.example.appcenter_project.domain.place.fixture;

/**
 * Place 정규화 기능(#650) 테스트용 상수 및 헬퍼
 *
 * [IMPL-HINT] 구현 에이전트가 아래 클래스를 생성해야 주석 해제 가능:
 *   - com.example.appcenter_project.domain.place.entity.Place
 *   - com.example.appcenter_project.domain.place.service.PlaceResolver
 *   - com.example.appcenter_project.domain.place.service.PlaceCacheService
 *   - com.example.appcenter_project.domain.place.service.QuotaGuard
 *   - com.example.appcenter_project.domain.place.service.PlaceMigrationService
 *   - com.example.appcenter_project.domain.place.enums.NormalizationOutcome
 *   - com.example.appcenter_project.domain.place.client.KakaoLocalClient
 *   - com.example.appcenter_project.domain.place.client.dto.KakaoPlaceDocument
 *   - com.example.appcenter_project.domain.place.repository.PlaceRepository
 *   - com.example.appcenter_project.domain.place.controller.PlaceSearchController
 *   - com.example.appcenter_project.domain.place.controller.AdminPlaceController
 *   - com.example.appcenter_project.domain.place.controller.dto.ResponsePlaceSearchItemDto
 *   - com.example.appcenter_project.global.exception.ErrorCode 에 신규 코드 5종 추가:
 *       KAKAO_API_ERROR(BAD_GATEWAY, 24001, ...)
 *       KAKAO_QUOTA_EXCEEDED(SERVICE_UNAVAILABLE, 24002, ...)
 *       KEYWORD_TOO_SHORT(BAD_REQUEST, 24003, ...)
 *       INVALID_PLACE_FOR_TYPE(BAD_REQUEST, 24004, ...)
 *       PLACE_ID_INVALID(BAD_REQUEST, 24005, ...)
 */
public class PlaceNormalizationFixture {

    public static final Long USER_ID = 1L;
    public static final Long ADMIN_USER_ID = 2L;

    public static final String KAKAO_PLACE_ID = "12345678";
    public static final String KAKAO_PLACE_NAME = "BBQ 송도점";
    public static final String RAW_PLACE_NAME = "BBQ 송도점";
    public static final String RAW_PLACE_NAME_VARIANT = "비비큐 송도";
    public static final String NORMALIZED_KEY = "bbq 송도점";
    public static final String NORMALIZED_KEY_VARIANT = "비비큐 송도";
    public static final String ROAD_ADDRESS = "인천 연수구 컨벤시아대로 165";
    public static final double LATITUDE = 37.3911;
    public static final double LONGITUDE = 126.6391;

    public static final String NOT_FOUND_SENTINEL = "__NOT_FOUND__";
    public static final String CACHE_KEY_PREFIX = "kakao:place:";
    public static final String USAGE_KEY_PREFIX = "kakao:usage:";

    public static final String KEYWORD_1CHAR = "B";
    public static final String KEYWORD_2CHAR = "BB";
    public static final String KEYWORD_LONG = "BBQ 송도점 치킨";

    public static final int QUOTA_LIMIT = 250_000;
    public static final int DEFAULT_SEARCH_SIZE = 10;
}
