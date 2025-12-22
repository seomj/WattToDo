package com.ssafy.wtd.backend.security;

import com.ssafy.wtd.backend.model.User;
import com.ssafy.wtd.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DbUserDetailsService implements UserDetailsService {

    private final UserRepository userMapper;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("존재하지 않는 사용자입니다.");
        }
        return new CustomUserDetails(user);
    }
}

