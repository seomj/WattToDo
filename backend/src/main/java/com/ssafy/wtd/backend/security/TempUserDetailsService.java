package com.ssafy.wtd.backend.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

//@Service
// 실행을 위한 임시 클래스
public class TempUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        // ⚠️ 임시 사용자 (DB 없이 실행용)
        if (!email.equals("test@example.com")) {
            throw new UsernameNotFoundException("User not found");
        }

        return User.builder()
                .username("test@example.com")
//                .password("{noop}password") // 임시라 암호화 생략
                .password("$2a$10$5Dm2Sy50TMLwILSU4KMTNed8SGynZ73IBrCpBS9kF8pqxmswj3s2O")
                .roles("USER")               // ROLE_USER
                .build();
    }
}
