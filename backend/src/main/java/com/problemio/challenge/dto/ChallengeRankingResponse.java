package com.problemio.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeRankingResponse {
    private Long challengeId;
    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private Integer ranking;
    private Double score;
    private Double playTime;
    private String challengeType; // TIME_ATTACK or SURVIVAL
    private LocalDateTime recordedAt;
}
