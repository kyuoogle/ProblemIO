package com.problemio.global.controller;

import com.problemio.global.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    // 파일이 저장될 로컬 디렉토리 경로
    private static final String UPLOAD_DIR = "C:/upload";

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
            @RequestParam("file") MultipartFile file
    ) {

        // 업로드된 파일이 비어있는지 확인
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("FILE_UPLOAD_ERROR", "File is empty."));
        }

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
            // 저장할 디렉토리 생성 (존재하지 않으면)
            Path uploadPath = Paths.get(UPLOAD_DIR);
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
        String url = "/uploads/" + filename;

        // 성공 응답: 저장된 파일의 URL과 파일명 반환
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "url", url,
                "filename", filename
        )));
    }
}
