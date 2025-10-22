package com.example.appcenter_project.shared.util;

import com.example.appcenter_project.domain.roommate.enums.DormDay;
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
