package com.problemio.global.service;

import com.problemio.global.exception.BusinessException;
import com.problemio.global.exception.ErrorCode;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    /**
     * 파일을 S3에 업로드하고 전체 URL을 반환합니다.
     * key는 내부적으로 생성하지 않고, 호출자가 지정한 경로(폴더+파일명)를 그대로 사용합니다.
     */
    public String upload(MultipartFile file, String s3Key) {
        try {
            PutObjectRequest putOb = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putOb, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // DB에는 상대 경로(Key)만 저장
            return s3Key;

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR); // 또는 적절한 파일 에러
        }
    }

    /**
     * 바이트 배열을 S3에 업로드하고 Key를 반환합니다.
     */
    public String uploadBytes(byte[] bytes, String s3Key, String contentType) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
            return s3Key;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 파일을 삭제합니다.
     * 파라미터로 전체 URL이 들어오더라도 Key를 추출하여 삭제합니다.
     */
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;

        try {
            // URL에서 Key 추출 로직
            // 예: https://bucket.s3.ap-northeast-2.amazonaws.com/public/upload/abc.jpg
            // -> public/upload/abc.jpg
            String key = extractKeyFromUrl(fileUrl);
            if (key != null) {
                s3Client.deleteObject(b -> b.bucket(bucketName).key(key));
            }
        } catch (Exception e) {
            // 삭제 실패는 로그만 남기고 흐름을 막지 않는 경우가 많음
            System.err.println("S3 Delete Error: " + e.getMessage());
        }
    }

    private String extractKeyFromUrl(String url) {
        // 간단한 파싱: ".amazonaws.com/" 뒷부분이 Key
        String target = ".amazonaws.com/";
        int idx = url.indexOf(target);
        if (idx != -1) {
            return url.substring(idx + target.length());
        }
        return null; // URL 형식이 아니면 null 처리 (또는 그대로 시도)
    }
}
