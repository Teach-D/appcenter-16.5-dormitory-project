package com.example.appcenter_project.enums.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum College {

    HUMANITIES("인문대학"),
    NATURAL_SCIENCES("자연과학대학"),
    SOCIAL_SCIENCES("사회과학대학"),
    GLOBAL_AFFAIRS_AND_ECONOMICS("글로벌정경대학"),
    ENGINEERING("공과대학"),
    INFORMATION_TECHNOLOGY("정보기술대학"),
    BUSINESS_ADMINISTRATION("경영대학"),
    ARTS_AND_PHYSICAL_EDUCATION("예술체육대학"),
    EDUCATION("사범대학"),
    URBAN_SCIENCES("도시과학대학"),
    LIFE_SCIENCES_AND_BIOTECHNOLOGY("생명과학기술대학"),
    INTERDISCIPLINARY_STUDIES("융합자유전공대학"),
    NORTHEAST_ASIAN_INTERNATIONAL_COMMERCE_AND_LOGISTICS("동북아국제통상물류학부"),
    LAW("법학부"),
    CONTRACT_DEPARTMENT("계약학과");

    private final String description;

    @JsonCreator
    public static College from(String value) {
        for (College type : College.values()) {
            if (type.getDescription().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid College: " + value);
    }

    @JsonValue
    public String toValue() {
        return this.description;
    }
}

