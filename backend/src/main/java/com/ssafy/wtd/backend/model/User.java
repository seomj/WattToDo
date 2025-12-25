package com.ssafy.wtd.backend.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long userId;
    private String email;
    private String password;
    private String name;

    private String role;
    private String status;
    private Long vehicleId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
