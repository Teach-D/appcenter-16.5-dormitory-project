package com.example.appcenter_project.domain.weather.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GridConverterTest {

    @Test
    @DisplayName("nx 반환 — BR-662 인천대학교 위경도(37.3749, 126.6368) 변환 시 nx=54")
    void should_return_nx_54_when_inu_lat_lon() {
        double lat = 37.3749;
        double lon = 126.6368;

        int[] grid = GridConverter.toGrid(lat, lon);

        assertThat(grid[0]).isEqualTo(54);
    }

    @Test
    @DisplayName("ny 반환 — BR-662 인천대학교 위경도(37.3749, 126.6368) 변환 시 ny=124")
    void should_return_ny_124_when_inu_lat_lon() {
        double lat = 37.3749;
        double lon = 126.6368;

        int[] grid = GridConverter.toGrid(lat, lon);

        assertThat(grid[1]).isEqualTo(124);
    }

    @Test
    @DisplayName("배열 길이 2 반환 — toGrid 결과는 [nx, ny] 2원소 배열")
    void should_return_array_of_length_2() {
        double lat = 37.3749;
        double lon = 126.6368;

        int[] grid = GridConverter.toGrid(lat, lon);

        assertThat(grid).hasSize(2);
    }
}
