package com.problemio.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private String statusMessage;
}
