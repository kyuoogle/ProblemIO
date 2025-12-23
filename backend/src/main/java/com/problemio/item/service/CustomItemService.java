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
import org.springframework.beans.factory.annotation.Autowired;
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
                    .itemType(ItemType.valueOf(request.getItemType()))
                    .name(request.getName())
                    .description(request.getDescription())
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

        customItemMapper.saveUserItem(userId, itemId, LocalDateTime.now());
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
                Object parsedConfig = item.getConfig();
                if (parsedConfig instanceof String) {
                    parsedConfig = objectMapper.readValue((String) parsedConfig, Object.class);
                }
                
                return CustomItemResponse.builder()
                        .id(item.getId())
                        .itemType(item.getItemType())
                        .name(item.getName())
                        .description(item.getDescription())
                        .config(parsedConfig)
                        .isDefault(item.isDefault())
                        .createdAt(item.getCreatedAt())
                        .isOwned(item.isOwned())
                        .build();
            } catch (Exception e) {
                log.error("Error parsing item config for item " + item.getId(), e);
                return null; 
            }
        })
        .filter(java.util.Objects::nonNull)
        .collect(Collectors.toList());
    }

    @Transactional
    public void updateItem(Long itemId, CustomItemRequest request) {
        CustomItem item = customItemMapper.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        
        try {
            String configJson = objectMapper.writeValueAsString(request.getConfig());
            
             CustomItem updatedItem = CustomItem.builder()
                    .id(item.getId())
                    .itemType(ItemType.valueOf(request.getItemType())) 
                    .name(request.getName())
                    .description(request.getDescription())
                    .config(configJson)
                    .isDefault(request.isDefault())
                    .createdAt(item.getCreatedAt()) 
                    .build();

            customItemMapper.update(updatedItem);           
        } catch (JsonProcessingException e) {
             throw new RuntimeException("Invalid config JSON");
        }
    }

    public List<com.problemio.user.dto.UserResponse> getAssignedUsers(Long itemId) {
        return customItemMapper.findAssignedUsers(itemId).stream()
                .map(u -> com.problemio.user.dto.UserResponse.builder()
                        .id(u.getId())
                        .nickname(u.getNickname())
                        .email(u.getEmail())
                        .profileImageUrl(u.getProfileImageUrl())
                        .build())
                .collect(Collectors.toList());
    }

    @Autowired
    private com.problemio.global.service.S3Service s3Service;

    @Transactional
    public void deleteItem(Long itemId) {
        // 1. Fetch item to get image path from config
        CustomItem item = customItemMapper.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        
        try {
            // Parse config to find image path
            if (item.getConfig() != null) {
                // Config is stored as String JSON
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> configMap = objectMapper.readValue(item.getConfig(), java.util.Map.class);
                
                if (configMap.containsKey("image")) {
                    String imagePath = (String) configMap.get("image");
                    // Check if it's an S3 path (starts with public/)
                    if (imagePath != null && (imagePath.startsWith("public/") || imagePath.startsWith("/public/"))) {
                         // Remove leading slash if present for S3 key consistency
                         if (imagePath.startsWith("/")) imagePath = imagePath.substring(1);
                         
                         s3Service.delete(imagePath);
                    }
                }
            }
        } catch (Exception e) {
             // Log but don't fail the transaction just because S3 delete failed (or decide policy)
             log.warn("Failed to delete S3 image for item " + itemId, e);
        }

        // FK 제약조건으로 인해 할당된 유저 정보 먼저 삭제
        customItemMapper.deleteUserItemsByItemId(itemId);
        customItemMapper.deleteItem(itemId);
    }

    @Transactional
    public void revokeUserItem(Long itemId, Long userId) {
        customItemMapper.deleteUserItem(userId, itemId);
    }
}
