package com.example.appcenter_project.utils;

import com.example.appcenter_project.enums.roommate.DormDay;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DormDayUtil {
    public static List<DormDay> sortDormDays(Set<DormDay> dormDays) {
        return dormDays.stream()
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .collect(Collectors.toList());
    }
}
