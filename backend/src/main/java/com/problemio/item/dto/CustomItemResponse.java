package com.problemio.item.dto;

import com.problemio.item.domain.ItemType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CustomItemResponse {
    private Long id;
    private ItemType itemType;
    private String name;
    private String description;
    private Object config; // Parsed JSON object
    private boolean isDefault;
    private LocalDateTime createdAt;
    private boolean isOwned; // For user view, to check ownership
}
