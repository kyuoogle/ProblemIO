package com.problemio.global.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;


import java.io.IOException;

public class S3UrlSerializer extends JsonSerializer<String> {

    private static String s3Url;

    public static void setS3Url(String url) {
        // 끝에 slash가 없다면 추가 (url joining을 위해)
        if (url != null && !url.endsWith("/")) {
            S3UrlSerializer.s3Url = url + "/";
        } else {
            S3UrlSerializer.s3Url = url;
        }
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        // 이미 http로 시작하면 그대로 씀
        if (value.startsWith("http://") || value.startsWith("https://")) {
            gen.writeString(value);
            return;
        }

        // DB에 저장된 상대 경로(public/...) 앞에 도메인 붙이기
        // S3_URL이 설정 안되어있으면 그냥 값 그대로 (로컬 테스트 등)
        if (s3Url != null) {
            // value가 /로 시작하면 제거 (중복 슬래시 방지)
            String path = value.startsWith("/") ? value.substring(1) : value;
            gen.writeString(s3Url + path);
        } else {
            gen.writeString(value);
        }
    }
}
