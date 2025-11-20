package com.problemio.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email; // username
    private String nickname;
    private String profileImageUrl;
    private String statusMessage;
    private Boolean isDeleted;
}