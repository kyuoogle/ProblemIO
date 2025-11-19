package com.problemio.follow.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Follow {

    private Long followerId;
    private Long followingId;
    private LocalDateTime createdAt;
}
