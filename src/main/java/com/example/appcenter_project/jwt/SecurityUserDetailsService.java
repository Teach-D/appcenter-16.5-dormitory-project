package com.example.appcenter_project.jwt;

import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String studentNumber) throws UsernameNotFoundException {
        User user = userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new UsernameNotFoundException("학번 [" + studentNumber + "] 에 해당하는 사용자를 찾을 수 없습니다."));

        return new SecurityUser(user);
    }
}