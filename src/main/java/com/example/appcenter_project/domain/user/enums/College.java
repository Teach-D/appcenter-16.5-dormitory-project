package com.example.appcenter_project.domain.user.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum College {

    HUMANITIES("인문대"),
    NATURAL_SCIENCES("자연과학대"),
    SOCIAL_SCIENCES("사회과학대"),
    GLOBAL_AFFAIRS_AND_ECONOMICS("글로벌정경대"),
    ENGINEERING("공과대"),
    INFORMATION_TECHNOLOGY("정보기술대"),
    BUSINESS_ADMINISTRATION("경영대"),
    ARTS_AND_PHYSICAL_EDUCATION("예체대"),
    EDUCATION("사범대"),
    URBAN_SCIENCES("도시과학대"),
    LIFE_SCIENCES_AND_BIOTECHNOLOGY("생명과학기술대"),
    INTERDISCIPLINARY_STUDIES("융합자유전공대"),
    NORTHEAST_ASIAN_INTERNATIONAL_COMMERCE_AND_LOGISTICS("동북아국제통상물류학부"),
    LAW("법학부"),
    CONTRACT_DEPARTMENT("계약학과");

    private final String description;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static College from(String value) {
        for (College type : College.values()) {
            if (type.getDescription().equals(value) || type.name().equalsIgnoreCase(value)) {
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
