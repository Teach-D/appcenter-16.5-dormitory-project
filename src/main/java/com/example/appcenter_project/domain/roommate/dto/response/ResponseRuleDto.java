package com.example.appcenter_project.domain.roommate.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseRuleDto {
    private List<String> rules;
}