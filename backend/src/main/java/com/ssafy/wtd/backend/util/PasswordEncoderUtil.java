package com.ssafy.wtd.backend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderUtil {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "1234";

        String encoded = encoder.encode(rawPassword);
        System.out.println("BCrypt password = " + encoded);
    }
}
