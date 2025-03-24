package com.example.project.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;

@Component
public class FileStorageInitializer implements CommandLineRunner {

    private final FileStorageConfig fileStorageConfig;
    private final Logger logger = LoggerFactory.getLogger(FileStorageInitializer.class);

    @Autowired
    public FileStorageInitializer(FileStorageConfig fileStorageConfig) {
        this.fileStorageConfig = fileStorageConfig;
    }

    @Override
    public void run(String... args) {
        try {
            // 프로필 이미지 디렉토리 생성
            Files.createDirectories(fileStorageConfig.getProfileDirPath());

            // 게시물 이미지 디렉토리 생성
            Files.createDirectories(fileStorageConfig.getPostDirPath());

            logger.info("파일 저장 디렉토리가 성공적으로 초기화되었습니다.");
            logger.info("프로필 경로: {}", fileStorageConfig.getProfileDirPath());
            logger.info("게시물 경로: {}", fileStorageConfig.getPostDirPath());
        } catch (IOException e) {
            logger.error("파일 저장 디렉토리 초기화 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("파일 저장 디렉토리를 생성할 수 없습니다.", e);
        }
    }
}