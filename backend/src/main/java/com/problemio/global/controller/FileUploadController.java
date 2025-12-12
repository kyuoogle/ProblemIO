package com.problemio.global.controller;

import com.problemio.global.common.ApiResponse;
import com.problemio.global.auth.CustomUserDetails;
import com.problemio.global.exception.BusinessException;
import com.problemio.global.exception.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.apache.tika.Tika;

import java.util.Arrays;
import java.util.List;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    // 파일이 저장될 로컬 디렉토리 경로 (정적 리소스 매핑과 동일하게 유지)
    private static final String UPLOAD_DIR = "C:/public/upload";
    private static final String THUMBNAIL_DIR = "Thumbnail";
    private static final String QUESTIONS_DIR = "Questions";

    private final Tika tika = new Tika();
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp");

    /*
     * 단일 파일 업로드 요청을 처리하는 API
     * 1. 파일 비었는지 체크
     * 2. 원본 확장자 추출
     * 3. UUID 기반 새로운 파일명 생성
     * 4. 로컬 디렉토리에 파일 저장
     * 5. 저장된 파일 URL 반환
     */

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, String>>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", required = false) String category,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        // 로그인 사용자만 업로드 가능
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }

        // 업로드된 파일이 비어있는지 확인
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("FILE_UPLOAD_ERROR", "File is empty."));
        }

        // 파일 유효성 검사 (확장자 + MIME Type)
        validateFile(file);

        // 원본 파일명에서 확장자 포함 정제
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // 확장자 추출 (.png, .jpg 등)
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > -1) {
            extension = originalFilename.substring(dotIndex);
        }

        // 충돌 방지를 위해 UUID로 새 파일명 생성
        String filename = UUID.randomUUID() + extension;

        try {
            String subDir = resolveSubDirectory(category);
            // 저장할 디렉토리 생성 (존재하지 않으면)
            Path uploadPath = Paths.get(UPLOAD_DIR, subDir);
            Files.createDirectories(uploadPath);

            // 실제 저장 대상 경로
            Path target = uploadPath.resolve(filename);

            // 파일을 로컬 디렉토리로 복사
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            // 저장 중 오류 발생 시 응답 처리
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("FILE_UPLOAD_ERROR", "Failed to save file."));
        }

        // 프론트에서 접근 가능한 형태의 URL 반환
        String subPath = resolveSubDirectory(category);
        String prefix = subPath.isEmpty() ? "" : subPath + "/";
        String url = "/uploads/" + prefix + filename;

        // 성공 응답: 저장된 파일의 URL과 파일명 반환
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "url", url,
                "filename", filename
        )));
    }

    /**
     * 업로드 카테고리에 따라 하위 폴더를 결정한다.
     * 허용된 값 이외에는 루트에 저장한다.
     */
    private String resolveSubDirectory(String category) {
        if (category == null || category.isBlank()) {
            return "";
        }

        String normalized = category.trim().toLowerCase();
        return switch (normalized) {
            case "thumbnail", "thumbnails" -> THUMBNAIL_DIR;
            case "question", "questions" -> QUESTIONS_DIR;
            default -> "";
        };
    }

    private void validateFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }

        // 1. 확장자 화이트리스트 검사
        String lowerCaseName = originalFilename.toLowerCase();
        boolean validExtension = ALLOWED_EXTENSIONS.stream().anyMatch(lowerCaseName::endsWith);
        if (!validExtension) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }

        // 2. MIME Type 검사 (Apache Tika)
        try {
            String mimeType = tika.detect(file.getInputStream());
            if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
                throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
