package com.problemio.item.dto;

import com.problemio.item.domain.ItemType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CustomItemRequest {
    private ItemType itemType;
    private String name;
    private Object config; // JSON object from frontend
    private boolean isDefault;
}
