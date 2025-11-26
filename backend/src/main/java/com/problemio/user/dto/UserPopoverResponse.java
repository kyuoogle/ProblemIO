package com.problemio.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPopoverResponse {

    private Long Id;            // 대상 유저 ID
    private String nickname;        // 닉네임
    private String profileImageUrl; // 프로필 이미지
    private String statusMessage;   // 상태 메시지

    private boolean isFollowing;    // 내가 이 유저를 팔로우 중인지
    private boolean isMe;           // 나 자신인지

    private int followerCount;      // 이 유저의 팔로워 수
    private int followingCount;     // 이 유저의 팔로잉 수
}
