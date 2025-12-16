package com.problemio.admin.controller;

import com.problemio.admin.service.AdminService;
import com.problemio.challenge.dto.ChallengeCreateRequest;
import com.problemio.global.auth.CustomUserDetails;
import com.problemio.quiz.domain.Quiz;
import com.problemio.quiz.dto.QuizResponse;
import com.problemio.global.dto.PageResponse;
import com.problemio.user.dto.UserResponse;
import com.problemio.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserMapper userMapper; // To fetch author details for response if needed

    // Check Admin Role helper (Optional, assuming Spring Security handles it via separate config or PreAuthorize)
    // But since we didn't add @PreAuthorize, we might want to check here or rely on SecurityContext
    // We already added User.role field, but SecurityConfig might need update to secure /api/admin/**
    
    @GetMapping("/quizzes")
    public PageResponse<QuizResponse> getAdminQuizzes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword
    ) {
        List<Quiz> quizzes = adminService.findAdminQuizzes(page, size, keyword);
        int totalElements = adminService.countAdminQuizzes(keyword);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        // Convert to QuizResponse
        // Note: This matches QuizService logic slightly but without complex like/follow checks for efficiency or reuse logic
        // For Admin view, basic info + isHidden is key.
        
        List<QuizResponse> content = quizzes.stream().map(quiz -> {
            // Need author info?
             UserResponse author = userMapper.findById(quiz.getUserId())
                     .map(u -> UserResponse.builder()
                             .id(u.getId())
                             .nickname(u.getNickname())
                             .email(u.getEmail())
                             .profileImageUrl(u.getProfileImageUrl())
                             .build())
                     .orElse(null);

            return QuizResponse.builder()
                    .id(quiz.getId())
                    .userId(quiz.getUserId())
                    .title(quiz.getTitle())
                    .description(quiz.getDescription())
                    .thumbnailUrl(quiz.getThumbnailUrl())
                    .isPublic(quiz.isPublic())
                    .isHidden(quiz.isHidden())
                    .likeCount(quiz.getLikeCount())
                    .playCount(quiz.getPlayCount())
                    .author(author)
                    .build();
        }).collect(Collectors.toList());

        return PageResponse.<QuizResponse>builder()
                .content(content)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .size(size)
                .page(page)
                .build();
    }

    @PatchMapping("/quizzes/{id}/hide")
    public ResponseEntity<Void> toggleQuizVisibility(@PathVariable Long id) {
        adminService.toggleQuizVisibility(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/challenges")
    public ResponseEntity<Void> createChallenge(@Valid @RequestBody ChallengeCreateRequest request) {
        adminService.createChallenge(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // --- Custom Item Management ---

    @Autowired
    private com.problemio.item.service.CustomItemService customItemService;

    @PostMapping("/items")
    public ResponseEntity<Void> createCustomItem(@RequestBody com.problemio.item.dto.CustomItemRequest request) {
        customItemService.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/items")
    public ResponseEntity<List<com.problemio.item.domain.CustomItem>> getAllCustomItems() {
        return ResponseEntity.ok(customItemService.getAllItems());
    }

    @PostMapping("/items/{itemId}/assign/{userId}")
    public ResponseEntity<Void> assignItemToUser(@PathVariable Long itemId, @PathVariable Long userId) {
        customItemService.assignItemToUser(itemId, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<Void> updateCustomItem(@PathVariable Long itemId, @RequestBody com.problemio.item.dto.CustomItemRequest request) {
        customItemService.updateItem(itemId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/items/{itemId}/users")
    public ResponseEntity<List<com.problemio.user.dto.UserResponse>> getAssignedUsers(@PathVariable Long itemId) {
        return ResponseEntity.ok(customItemService.getAssignedUsers(itemId));
    }

    @DeleteMapping("/items/{itemId}/users/{userId}")
    public ResponseEntity<Void> revokeUserItem(@PathVariable Long itemId, @PathVariable Long userId) {
        customItemService.revokeUserItem(itemId, userId);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/items/upload")
    public ResponseEntity<String> uploadItemImage(@RequestParam("file") org.springframework.web.multipart.MultipartFile file, @RequestParam("type") String type) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        try {
            // Determine directory based on type
            String subDir = "theme";
            if ("POPOVER".equalsIgnoreCase(type)) subDir = "popover";
            else if ("AVATAR".equalsIgnoreCase(type)) subDir = "avatar";
            
            // Use backend managed directory as mapped in WebMvcConfig
            // Mapped to C:/public/{subDir}/
            Path uploadDir = Paths.get("C:/public/" + subDir);
            
            // Log for debugging
            System.out.println("Uploading to: " + uploadDir.toAbsolutePath());
            
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Save file
            String originalFilename = org.springframework.util.StringUtils.cleanPath(file.getOriginalFilename());
            
            // Generate filename: Timestamp + Original Name (to ensure uniqueness but keep readability)
            // Example: 1700000000000_myimage.jpg
            String filename = System.currentTimeMillis() + "_" + originalFilename;
            
            // Security check: ensure no directory traversal
            if (filename.contains("..")) {
                 throw new RuntimeException("Invalid filename: " + filename);
            }

            Path filePath = uploadDir.resolve(filename);
            
            Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            // Return relative path for use in config (e.g., /theme/123123_bg.jpg)
            return ResponseEntity.ok("/" + subDir + "/" + filename);

        } catch (java.io.IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to upload file: " + e.getMessage());
        }
    }
}
