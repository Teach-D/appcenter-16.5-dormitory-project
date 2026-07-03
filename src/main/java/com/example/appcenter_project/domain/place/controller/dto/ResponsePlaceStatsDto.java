package com.example.appcenter_project.domain.place.controller.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ResponsePlaceStatsDto {

    private final Summary summary;
    private final List<DailyStats> daily;

    private ResponsePlaceStatsDto(Summary summary, List<DailyStats> daily) {
        this.summary = summary;
        this.daily = daily;
    }

    public static ResponsePlaceStatsDto of(Summary summary, List<DailyStats> daily) {
        return new ResponsePlaceStatsDto(summary, daily);
    }

    @Getter
    public static class Summary {
        private final long distinctRawPlaceNameCount;
        private final long distinctPlaceIdCount;
        private final Double deduplicationRatio;
        private final long newOrdersLastNDays;
        private final long newOrdersNullPlaceLastNDays;
        private final Double fallbackRatio;
        private final long cacheHitTotal;
        private final long cacheMissTotal;
        private final Double cacheHitRatio;
        private final long quotaUsageToday;
        private final long quotaLimitDaily;
        private final double quotaUsageRatio;

        public Summary(long distinctRawPlaceNameCount, long distinctPlaceIdCount,
                       long newOrdersLastNDays, long newOrdersNullPlaceLastNDays,
                       long cacheHitTotal, long cacheMissTotal,
                       long quotaUsageToday, long quotaLimitDaily) {
            this.distinctRawPlaceNameCount = distinctRawPlaceNameCount;
            this.distinctPlaceIdCount = distinctPlaceIdCount;
            this.deduplicationRatio = distinctRawPlaceNameCount == 0 ? null
                    : 1.0 - ((double) distinctPlaceIdCount / distinctRawPlaceNameCount);
            this.newOrdersLastNDays = newOrdersLastNDays;
            this.newOrdersNullPlaceLastNDays = newOrdersNullPlaceLastNDays;
            this.fallbackRatio = newOrdersLastNDays == 0 ? null
                    : (double) newOrdersNullPlaceLastNDays / newOrdersLastNDays;
            this.cacheHitTotal = cacheHitTotal;
            this.cacheMissTotal = cacheMissTotal;
            long total = cacheHitTotal + cacheMissTotal;
            this.cacheHitRatio = total == 0 ? null : (double) cacheHitTotal / total;
            this.quotaUsageToday = quotaUsageToday;
            this.quotaLimitDaily = quotaLimitDaily;
            this.quotaUsageRatio = quotaLimitDaily == 0 ? 0 : (double) quotaUsageToday / quotaLimitDaily;
        }
    }

    @Getter
    public static class DailyStats {
        private final String date;
        private final long kakaoCalls;
        private final long cacheHits;
        private final long cacheMisses;
        private final long fallbacks;

        public DailyStats(String date, long kakaoCalls, long cacheHits, long cacheMisses, long fallbacks) {
            this.date = date;
            this.kakaoCalls = kakaoCalls;
            this.cacheHits = cacheHits;
            this.cacheMisses = cacheMisses;
            this.fallbacks = fallbacks;
        }
    }
}
