package com.example.appcenter_project.dto.response.groupOrder;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ResponseReadGroupOrderChat {

    private Long readGroupOrderChatId;
}
