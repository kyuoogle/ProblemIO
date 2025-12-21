package com.problemio.ai.service;

import com.problemio.ai.cache.CandidateCache;
import com.problemio.ai.dto.AiThumbnailCandidateResponse;
import com.problemio.ai.dto.AiThumbnailConfirmResponse;
import com.problemio.global.exception.BusinessException;
import com.problemio.global.exception.ErrorCode;
import com.problemio.global.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiQuizThumbnailService {

    private final CandidateCache candidateCache;
    private final GmsGeminiClient gmsGeminiClient;
    private final S3Service s3Service;

    @Value("${spring.cloud.aws.s3.url:}")
    private String s3BaseUrl;

    public AiThumbnailCandidateResponse generateCandidates(String title, String description) {
        String safeTitle = title == null ? "" : title.trim();
        String safeDescription = description == null ? "" : description.trim();

        if (safeTitle.length() < 5 || safeDescription.length() < 10) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<String> styleHints = List.of(
                "minimal flat illustration, bold shapes, soft gradient background",
                "playful 3D clay style, warm colors, simple lighting"
        );

        List<AiThumbnailCandidateResponse.Candidate> candidates = styleHints.stream()
                .map(style -> createCandidate(safeTitle, safeDescription, style))
                .toList();

        return AiThumbnailCandidateResponse.builder()
                .candidates(candidates)
                .build();
    }

    public AiThumbnailConfirmResponse confirmCandidate(String candidateId, Long userId) {
        if (candidateId == null || candidateId.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        byte[] bytes = candidateCache.get(candidateId);
        if (bytes == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String key = "quiz-thumbnails/" + userId + "/" + candidateId + ".png";
        String s3Key = s3Service.uploadBytes(bytes, key, "image/png");
        candidateCache.evict(candidateId);

        String fullUrl = buildFullUrl(s3Key);
        return AiThumbnailConfirmResponse.builder()
                .thumbnailUrl(fullUrl)
                .build();
    }

    private AiThumbnailCandidateResponse.Candidate createCandidate(
            String title,
            String description,
            String styleHint
    ) {
        byte[] bytes = gmsGeminiClient.generatePngBytes(title, description, styleHint);
        String candidateId = UUID.randomUUID().toString();
        candidateCache.put(candidateId, bytes);

        String previewDataUrl = "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        return AiThumbnailCandidateResponse.Candidate.builder()
                .candidateId(candidateId)
                .previewDataUrl(previewDataUrl)
                .build();
    }

    private String buildFullUrl(String s3Key) {
        if (s3BaseUrl == null || s3BaseUrl.isBlank()) {
            return s3Key;
        }
        String base = s3BaseUrl.endsWith("/") ? s3BaseUrl : s3BaseUrl + "/";
        String key = s3Key.startsWith("/") ? s3Key.substring(1) : s3Key;
        return base + key;
    }
}
