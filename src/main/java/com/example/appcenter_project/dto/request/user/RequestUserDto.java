package com.example.appcenter_project.dto.request.user;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class RequestUserDto {

    private String name;
    private String dormType;
    private String college;
    private int penalty;

}
