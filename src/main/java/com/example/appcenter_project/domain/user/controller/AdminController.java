package com.example.appcenter_project.domain.user.controller;

import com.example.appcenter_project.domain.user.dto.request.RequestAdminDto;
import com.example.appcenter_project.domain.user.dto.response.ResponseLoginDto;
import com.example.appcenter_project.domain.user.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admins")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/login")
    public ResponseLoginDto login(@RequestBody RequestAdminDto requestAdminDto) {
        return adminService.login(requestAdminDto);
    }
}
