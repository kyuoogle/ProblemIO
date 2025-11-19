package com.problemio.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSummaryDto {

    private Long id;
    private String nickname;
    private String profileImageUrl;
}
