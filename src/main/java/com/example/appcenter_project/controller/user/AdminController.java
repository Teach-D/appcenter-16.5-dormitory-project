package com.example.appcenter_project.controller.user;

import com.example.appcenter_project.dto.request.user.RequestAdminDto;
import com.example.appcenter_project.dto.response.user.ResponseLoginDto;
import com.example.appcenter_project.service.user.AdminService;
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
