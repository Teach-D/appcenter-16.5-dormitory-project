package com.example.appcenter_project.domain.roommate.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ResponseFilteredRoommatePostDto {
    private ResponseRoommatePostDto post;
    private List<String> matchedFilterFields;
}
