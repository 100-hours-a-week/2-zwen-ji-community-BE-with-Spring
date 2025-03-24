package com.example.project.service;

import com.example.project.config.FileStorageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final FileStorageConfig fileStorageConfig;
    private final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Autowired
    public FileStorageService(FileStorageConfig fileStorageConfig) {
        this.fileStorageConfig = fileStorageConfig;
    }

    /**
     * 프로필 이미지 저장
     * @param file 업로드된 파일
     * @return 저장된 이미지 URI
     */
    public String storeProfileImage(MultipartFile file) {
        return storeFile(file, fileStorageConfig.getProfileDirPath(), FileType.PROFILE);
    }

    /**
     * 게시물 이미지 저장
     * @param file 업로드된 파일
     * @return 저장된 이미지 URI
     */
    public String storePostImage(MultipartFile file) {
        return storeFile(file, fileStorageConfig.getPostDirPath(), FileType.POST);
    }

    /**
     * URL에서 프로필 이미지 다운로드 및 저장
     * @param imageUrl 이미지 URL
     * @return 저장된 이미지 URI
     */
    public String storeProfileImageFromUrl(String imageUrl) {
        return storeFileFromUrl(imageUrl, fileStorageConfig.getProfileDirPath(), FileType.PROFILE);
    }

    /**
     * URL에서 게시물 이미지 다운로드 및 저장
     * @param imageUrl 이미지 URL
     * @return 저장된 이미지 URI
     */
    public String storePostImageFromUrl(String imageUrl) {
        return storeFileFromUrl(imageUrl, fileStorageConfig.getPostDirPath(), FileType.POST);
    }

    /**
     * 파일 삭제
     * @param fileUri 파일 URI
     * @return 삭제 성공 여부
     */
    public boolean deleteFile(String fileUri) {
        try {
            // URI에서 파일명 추출
            String filename = fileUri.substring(fileUri.lastIndexOf("/") + 1);

            // 파일 경로 결정
            Path filePath;
            if (fileUri.contains("/profiles/")) {
                filePath = fileStorageConfig.getProfileDirPath().resolve(filename);
            } else if (fileUri.contains("/posts/")) {
                filePath = fileStorageConfig.getPostDirPath().resolve(filename);
            } else {
                logger.warn("지원되지 않는 파일 경로: {}", fileUri);
                return false;
            }

            // 파일 삭제
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            logger.error("파일 삭제 중 오류 발생: {}", fileUri, e);
            return false;
        }
    }

    /**
     * 파일 저장 (내부 메서드)
     */
    private String storeFile(MultipartFile file, Path targetDir, FileType fileType) {
        try {
            // 파일 유효성 검사
            validateFile(file);

            // 원본 파일명에서 확장자 추출
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);

            // 고유한 파일명 생성
            String filename = UUID.randomUUID().toString() + extension;

            // 파일 저장
            Path targetLocation = targetDir.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            logger.info("파일 저장 완료: {}", targetLocation);

            // 파일 타입에 따라 적절한 URI 반환
            return getResourceUri(filename, fileType);
        } catch (IOException e) {
            logger.error("파일 저장 중 오류 발생", e);
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다", e);
        }
    }

    /**
     * URL에서 파일 다운로드 및 저장 (내부 메서드)
     */
    private String storeFileFromUrl(String imageUrl, Path targetDir, FileType fileType) {
        try {
            // URL 유효성 검사
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                throw new IllegalArgumentException("이미지 URL이 비어있습니다");
            }

            // URL에서 파일명 및 확장자 추출
            String originalFilename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            String extension = getFileExtension(originalFilename);

            // 고유한 파일명 생성
            String filename = UUID.randomUUID().toString() + extension;

            // URL에서 파일 다운로드
            URL url = new URL(imageUrl);
            Path targetLocation = targetDir.resolve(filename);

            try (InputStream in = url.openStream()) {
                Files.copy(in, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            logger.info("URL에서 파일 다운로드 완료: {}", targetLocation);

            // 파일 타입에 따라 적절한 URI 반환
            return getResourceUri(filename, fileType);
        } catch (IOException e) {
            logger.error("URL에서 파일 다운로드 중 오류 발생: {}", imageUrl, e);
            throw new RuntimeException("URL에서 이미지를 다운로드하는 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 파일 유효성 검사
     */
    private void validateFile(MultipartFile file) {
        // 파일이 비어있는지 확인
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        // 파일 크기 확인 (최대 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 5MB를 초과할 수 없습니다");
        }

        // 파일 형식 확인
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다");
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return ".jpg"; // 기본 확장자
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 파일 타입에 따른 리소스 URI 반환
     */
    private String getResourceUri(String filename, FileType fileType) {
        if (fileType == FileType.PROFILE) {
            return fileStorageConfig.getProfileResourceUri(filename);
        } else {
            return fileStorageConfig.getPostResourceUri(filename);
        }
    }

    /**
     * 파일 타입 열거형
     */
    public enum FileType {
        PROFILE,
        POST
    }
}