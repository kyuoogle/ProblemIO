package com.problemio.item.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomItem {
    private Long id;
    private ItemType itemType;
    private String name;
    private String description;
    private String config; // Using String for JSON content in Domain, Mapper will handle it
    private boolean isDefault;
    private LocalDateTime createdAt;
}
