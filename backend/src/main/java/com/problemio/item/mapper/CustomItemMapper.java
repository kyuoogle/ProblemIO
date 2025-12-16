package com.problemio.item.mapper;

import com.problemio.item.domain.CustomItem;
import com.problemio.item.dto.CustomItemResponse;
import com.problemio.item.domain.ItemType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CustomItemMapper {
    void save(CustomItem customItem);
    
    Optional<CustomItem> findById(Long id);
    
    List<CustomItem> findAll();
    
    // Find items owned by user (including defaults)
    List<CustomItemResponse> findItemsByUserId(@Param("userId") Long userId, @Param("type") ItemType type);
    
    // Check ownership
    boolean existsUserItem(@Param("userId") Long userId, @Param("itemId") Long itemId);
    
    // Assign item
    void saveUserItem(@Param("userId") Long userId, @Param("itemId") Long itemId);
}
