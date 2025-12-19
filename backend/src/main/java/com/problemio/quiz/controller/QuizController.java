package com.problemio.quiz.controller;

import com.problemio.global.auth.CustomUserDetails;
import com.problemio.global.common.ApiResponse;
import com.problemio.global.exception.BusinessException;
import com.problemio.global.exception.ErrorCode;
import com.problemio.quiz.dto.QuizCreateRequest;
import com.problemio.quiz.dto.QuestionResponse;
import com.problemio.quiz.dto.QuizResponse;
import com.problemio.quiz.dto.QuizSummaryDto;
import com.problemio.quiz.dto.QuizUpdateRequest;
import com.problemio.quiz.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    // 퀴즈 관련 비즈니스 로직을 담당하는 서비스 빈 주입
    private final QuizService quizService;

    /**
     * 퀴즈 목록 조회 API
     * - 페이징(page, size)
     * - 정렬 기준(sort: latest, popular 등)
     * - 검색 키워드(keyword)
     * 를 기준으로 퀴즈 목록 및 메타 정보(Map<String, Object>)를 반환한다.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getQuizzes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(quizService.getQuizzes(page, size, sort, keyword))
        );
    }

    /**
     * 퀴즈 생성 API
     * - 요청 바디의 QuizCreateRequest를 검증(@Valid) 후
     * - 로그인 유저 ID를 추출해서 퀴즈를 생성한다.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<QuizResponse>> createQuiz(
            @RequestBody @Valid QuizCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = requireLogin(userDetails);
        return ResponseEntity.ok(
                ApiResponse.success(quizService.createQuiz(userId, request))
        );
    }

    /**
     * 퀴즈 전체 수정 API (PUT)
     * - quizId에 해당하는 퀴즈를 요청 바디의 내용으로 전체 갱신한다.
     * - 로그인 유저가 해당 퀴즈의 작성자인지 서비스에서 검증한다고 가정.
     */
    @PutMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponse>> updateQuiz(
            @PathVariable Long quizId,
            @RequestBody @Valid QuizUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = requireLogin(userDetails);
        return ResponseEntity.ok(
                ApiResponse.success(quizService.updateQuiz(userId, quizId, request))
        );
    }

    /**
     * 퀴즈 부분 수정 API (PATCH)
     * - QuizUpdateRequest를 부분적으로 받아서 일부 필드만 수정할 때 사용.
     * - @Valid를 붙이지 않고, null 허용 후 서비스 단에서 부분 업데이트 처리 가능.
     */
    @PatchMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponse>> updateQuizPatch(
            @PathVariable Long quizId,
            @RequestBody QuizUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = requireLogin(userDetails);
        return ResponseEntity.ok(
                ApiResponse.success(quizService.updateQuiz(userId, quizId, request))
        );
    }

    /**
     * 퀴즈 삭제 API
     * - quizId에 해당하는 퀴즈를 삭제한다.
     * - 로그인 유저가 작성자인지 여부는 서비스에서 검증.
     */
    @DeleteMapping("/{quizId}")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = requireLogin(userDetails);
        quizService.deleteQuiz(userId, quizId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 퀴즈 단건 조회 API
     * - quizId로 특정 퀴즈 상세 정보를 조회한다.
     * - viewerId(조회자 ID)를 넘겨서,
     *   - 좋아요 여부
     *   - 접근 권한 등
     *   을 서비스에서 판단할 수 있도록 한다.
     * - 비로그인 사용자의 경우 viewerId는 null.
     */
    @GetMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponse>> getQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long viewerId = userDetails != null ? userDetails.getUser().getId() : null;
        return ResponseEntity.ok(
                ApiResponse.success(quizService.getQuiz(quizId, viewerId))
        );
    }

    /**
     * 공개 퀴즈 목록 조회 API
     * - 비로그인 사용자도 볼 수 있는 공개(public) 퀴즈 리스트를 조회한다.
     * - 목록 화면용으로 QuizSummaryDto 리스트를 반환.
     */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<QuizSummaryDto>>> getPublicQuizzes() {
        return ResponseEntity.ok(
                ApiResponse.success(quizService.getPublicQuizzes())
        );
    }

    /**
     * 나의 퀴즈 목록 조회 API
     * - 현재 로그인된 사용자가 생성한 퀴즈들의 요약 정보를 조회한다.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<QuizSummaryDto>>> getMyQuizzes(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(quizService.getUserQuizzes(requireLogin(userDetails)))
        );
    }

    /**
     * 퀴즈 좋아요 등록 API
     * - 현재 로그인한 사용자가 특정 퀴즈에 좋아요를 누른다.
     * - 응답으로 liked: true 플래그를 내려준다.
     */
    @PostMapping("/{quizId}/like")
    public ResponseEntity<ApiResponse<Map<String, Object>>> likeQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        quizService.likeQuiz(requireLogin(userDetails), quizId);
        return ResponseEntity.ok(
                ApiResponse.success(Map.of("liked", true))
        );
    }

    /**
     * 퀴즈 좋아요 취소 API
     * - 현재 로그인한 사용자가 특정 퀴즈에 눌렀던 좋아요를 취소한다.
     * - 응답으로 liked: false 플래그를 내려준다.
     */
    @DeleteMapping("/{quizId}/like")
    public ResponseEntity<ApiResponse<Map<String, Object>>> unlikeQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        quizService.unlikeQuiz(requireLogin(userDetails), quizId);
        return ResponseEntity.ok(
                ApiResponse.success(Map.of("liked", false))
        );
    }

    /**
     * 퀴즈 시작용 문제 조회 (무작위 순서 + 제한 개수)
     */
    @GetMapping("/{quizId}/questions")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuizQuestions(
            @PathVariable Long quizId,
            @RequestParam(required = false) Integer limit,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long viewerId = userDetails != null ? userDetails.getUser().getId() : null;
        return ResponseEntity.ok(
                ApiResponse.success(quizService.getQuizQuestions(quizId, viewerId, limit))
        );
    }

    private Long requireLogin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }
        return userDetails.getUser().getId();
    }
}
