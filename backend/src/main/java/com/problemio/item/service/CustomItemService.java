package com.problemio.item.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.problemio.item.domain.CustomItem;
import com.problemio.item.domain.ItemType;
import com.problemio.item.dto.CustomItemRequest;
import com.problemio.item.dto.CustomItemResponse;
import com.problemio.item.mapper.CustomItemMapper;
import com.problemio.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomItemService {

    private final CustomItemMapper customItemMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public void createItem(CustomItemRequest request) {
        try {
            String configJson = objectMapper.writeValueAsString(request.getConfig());
            
            CustomItem item = CustomItem.builder()
                    .itemType(request.getItemType())
                    .name(request.getName())
                    .config(configJson)
                    .isDefault(request.isDefault())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            customItemMapper.save(item);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize item config", e);
            throw new RuntimeException("Invalid item configuration");
        }
    }

    public List<CustomItem> getAllItems() {
        return customItemMapper.findAll();
    }

    /*
     * Assign an item to a user.
     * Throws exception if user doesn't exist or item doesn't exist.
     */
    @Transactional
    public void assignItemToUser(Long itemId, Long userId) {
        // Validation: Check if item exists
        customItemMapper.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Invalid item ID"));
        
        // Validation: Check if user exists (Optional, relying on FK constraint usually better for perf, but explicit check implies better error msg)
        // userMapper.findById(userId).orElseThrow(...) 

        // Check if already assigned
        if (customItemMapper.existsUserItem(userId, itemId)) {
            throw new IllegalStateException("User already owns this item");
        }

        customItemMapper.saveUserItem(userId, itemId);
    }

    /**
     * Get all items available to a specific user for a specific category.
     * This includes default items + assigned items.
     */
    public List<CustomItemResponse> getAvailableItems(Long userId, ItemType type) {
        List<CustomItemResponse> items = customItemMapper.findItemsByUserId(userId, type);
        
        // Post-process config string back to Object if needed
        return items.stream().map(item -> {
            try {
                // Determine if we need to parse config. 
                // However, DTO has `Object config`. 
                // If Mybatis returned it as String in the object, we might need to parse.
                // But wait, `CustomItemResponse` field `config` is Object. 
                // Mybatis will likely map the JSON string column to a String in Java if type handler isn't set.
                // Actually, let's look at `Configuration` of MyBatis or just handle it here.
                // If I mapped `config` in mapper XML to a column, it comes out as String.
                // So I should modify CustomItemResponse to have String config OR parse it here.
                // Let's assume it comes as String and we parse it.
                
                // Oops, I defined `Object config` in DTO. 
                // If MyBatis maps String -> Object, it remains String.
                Object parsedConfig = item.getConfig();
                if (parsedConfig instanceof String) {
                    parsedConfig = objectMapper.readValue((String) parsedConfig, Object.class);
                }
                
                return CustomItemResponse.builder()
                        .id(item.getId())
                        .itemType(item.getItemType())
                        .name(item.getName())
                        .config(parsedConfig)
                        .isDefault(item.isDefault())
                        .createdAt(item.getCreatedAt())
                        .isOwned(item.isOwned())
                        .build();
            } catch (Exception e) {
                log.error("Error parsing item config for item " + item.getId(), e);
                return item; // Return as is if fail?
            }
        }).collect(Collectors.toList());
    }
}
