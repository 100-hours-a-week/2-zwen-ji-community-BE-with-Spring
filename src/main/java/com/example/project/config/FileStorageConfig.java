package com.example.project.config;

import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfig {

    // 기본 업로드 경로 (애플리케이션 내부 리소스 디렉토리 기준)
    private final String baseUploadPath = "src/main/resources/static.upload/";

    // 프로필 이미지 저장 경로
    private final String profileDir = "profiles/";

    // 게시물 이미지 저장 경로
    private final String postDir = "posts/";

    // 웹에서 접근 가능한 경로 (URL 기준)
    private final String resourceUri = "/static.upload/";

    // 프로필 이미지 경로 가져오기
    public Path getProfileDirPath() {
        return Paths.get(baseUploadPath + profileDir).toAbsolutePath().normalize();
    }

    // 게시물 이미지 경로 가져오기
    public Path getPostDirPath() {
        return Paths.get(baseUploadPath + postDir).toAbsolutePath().normalize();
    }

    // 프로필 이미지 URI 생성
    public String getProfileResourceUri(String filename) {
        return resourceUri + profileDir + filename;
    }

    // 게시물 이미지 URI 생성
    public String getPostResourceUri(String filename) {
        return resourceUri + postDir + filename;
    }
}