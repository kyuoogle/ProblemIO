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
}
