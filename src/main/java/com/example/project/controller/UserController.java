package com.example.project.controller;

import com.example.project.domain.User;
import com.example.project.dto.request.SignupRequest;
import com.example.project.dto.response.ApiResponse;
import com.example.project.dto.response.BaseResponse;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.repository.UserRepository;
import com.example.project.service.FileStorageService;
import com.example.project.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/users")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"})
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public UserController(UserService userService, UserRepository userRepository, FileStorageService fileStorageService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    // 모든 사용자 조회
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // 특정 ID 사용자 조회
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



    // 회원 가입 (새로운 사용자 생성)
    @PostMapping
    public ResponseEntity<ApiResponse> registerUser(
            @RequestPart("userData") SignupRequest signupRequest,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        try {
            // 새 사용자 객체 생성
            User user = new User();
            user.setEmail(signupRequest.getEmail());

            // 비밀번호 인코딩
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            user.setPassword(encoder.encode(signupRequest.getPassword()));

            user.setNickname(signupRequest.getNickname());

            // 프로필 이미지가 제공된 경우 저장
            if (profileImage != null && !profileImage.isEmpty()) {
                try {
                    // 이미지 저장 및 URL 획득
                    String profileImageUrl = fileStorageService.storeProfileImage(profileImage);

                    // 사용자 객체에 이미지 URL 설정
                    user.setProfileImageUrl(profileImageUrl);
                } catch (Exception e) {
                    // 이미지 저장 중 오류 발생 시 로깅만 하고 계속 진행
                    // (사용자는 나중에 프로필 이미지를 업데이트할 수 있음)
                }
            } else {
                // 기본 프로필 이미지 설정 (선택 사항)
                user.setProfileImageUrl("/static.upload/default/default-profile.png");
            }

            // 사용자 저장
            User savedUser = userService.createUser(user);

            // 사용자 ID를 응답에 포함
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("userId", savedUser.getId());
            responseData.put("profileImageUrl", savedUser.getProfileImageUrl());

            // 응답 반환
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse("회원가입이 성공적으로 완료되었습니다.", responseData, "success"));

        } catch (Exception e) {
            // 오류 처리
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("회원가입 중 오류가 발생했습니다: " + e.getMessage(), null, "error"));
        }
    }


    @CrossOrigin(
            origins = {"http://localhost:5500", "http://127.0.0.1:5500"},
            methods = {RequestMethod.PATCH, RequestMethod.OPTIONS},
            allowedHeaders = {"Authorization", "Content-Type", "X-Requested-With"},
            allowCredentials = "true"
    )
    @PatchMapping("/{id}/nickname")
    public ResponseEntity<BaseResponse<User>> updateNickname(
            @PathVariable Long id,
            @RequestBody Map<String, String> updateData,
            @AuthenticationPrincipal UserDetails userDetails) {

        String newNickname = updateData.get("nickname");

        // 인증된 사용자가 자신의 프로필만 변경할 수 있도록 검증
        User currentUser = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        if (!userDetails.getUsername().equals(currentUser.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new BaseResponse<>("다른 사용자의 프로필을 변경할 권한이 없습니다", null, "error"));
        }

        try {
            // 닉네임이 제공되었는지 확인
            if (newNickname == null || newNickname.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new BaseResponse<>("변경할 닉네임이 제공되지 않았습니다", null, "error"));
            }

            // 현재 사용자의 닉네임과 동일한 경우 업데이트 건너뛰기
            if (newNickname.equals(currentUser.getNickname())) {
                return ResponseEntity.ok(new BaseResponse<>(
                        "닉네임이 이미 동일합니다", currentUser, "success"));
            }

            // 닉네임 중복 검사
            Optional<Object> existingUser = userRepository.findByNickname(newNickname);
            if (existingUser.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(new BaseResponse<>("이미 사용 중인 닉네임입니다", null, "error"));
            }

            // 닉네임 변경 처리
            User updatedUser = userService.changeNickname(id, newNickname);

            return ResponseEntity.ok(new BaseResponse<>(
                    "닉네임이 성공적으로 변경되었습니다", updatedUser, "success"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>("닉네임 변경 중 오류가 발생했습니다: " + e.getMessage(), null, "error"));
        }
    }



    @PatchMapping("/{id}/password")
    public ResponseEntity<BaseResponse<User>> updatePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> passwordData,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 인증된 사용자가 자신의 비밀번호만 변경할 수 있도록 검증
        User currentUser = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        if (!userDetails.getUsername().equals(currentUser.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new BaseResponse<>("다른 사용자의 비밀번호를 변경할 권한이 없습니다", null, "error"));
        }

        try {
            // 새 비밀번호 확인
            String newPassword = passwordData.get("newPassword");

            if (newPassword == null) {
                return ResponseEntity.badRequest()
                        .body(new BaseResponse<>("새 비밀번호가 필요합니다", null, "error"));
            }

            // 비밀번호 유효성 검사 (옵션)
            if (newPassword.length() < 8) {
                return ResponseEntity.badRequest()
                        .body(new BaseResponse<>("비밀번호는 8자 이상이어야 합니다", null, "error"));
            }

            // 서비스 메서드 호출 (현재 비밀번호 검증 없이)
            User updatedUser = userService.updatePassword(id, newPassword);

            return ResponseEntity.ok(new BaseResponse<>("비밀번호가 성공적으로 변경되었습니다", updatedUser, "success"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>("비밀번호 변경 중 오류가 발생했습니다: " + e.getMessage(), null, "error"));
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            // 1. 요청된 사용자 ID가 유효한지 확인
            User userToDelete = userService.getUserById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + id));

            // 2. 현재 인증된 사용자가 삭제하려는 사용자와 동일한지 확인
            if (!userDetails.getUsername().equals(userToDelete.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new BaseResponse<>("다른 사용자의 계정을 삭제할 권한이 없습니다", null, "error"));
            }

            // 3. 소프트 삭제 수행
            userService.softDeleteUser(id);

            // 4. 성공 응답 반환
            return ResponseEntity.ok()
                    .body(new BaseResponse<>("회원 탈퇴가 정상적으로 처리되었습니다", null, "success"));

        } catch (ResourceNotFoundException e) {
            // 사용자를 찾을 수 없는 경우
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new BaseResponse<>(e.getMessage(), null, "error"));
        } catch (Exception e) {
            // 기타 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>("회원 탈퇴 처리 중 오류가 발생했습니다: " + e.getMessage(), null, "error"));
        }
    }

    @PostMapping("/{id}/profile-image")
    public ResponseEntity<BaseResponse<User>> uploadProfileImage(
            @PathVariable Long id,
            @RequestPart("image") MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 현재 인증된 사용자가 요청한 사용자 ID와 일치하는지 확인
        User currentUser = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        if (!userDetails.getUsername().equals(currentUser.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new BaseResponse<>("다른 사용자의 프로필을 변경할 권한이 없습니다", null, "error"));
        }

        try {
            // 이미지가 제공되었는지 확인
            if (image == null || image.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new BaseResponse<>("업로드할 이미지가 제공되지 않았습니다", null, "error"));
            }

            // 이미지 유효성 검사 (옵션)
            if (!image.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(new BaseResponse<>("유효한 이미지 파일만 업로드할 수 있습니다", null, "error"));
            }

            // 기존 이미지가 있는 경우 삭제
            String currentImageUrl = currentUser.getProfileImageUrl();
            if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                // 파일 시스템에서 기존 이미지 삭제
                fileStorageService.deleteFile(currentImageUrl);
            }

            // 새 이미지 저장
            String newImageUrl = fileStorageService.storeProfileImage(image);

            // 사용자 프로필 업데이트
            User updatedUser = userService.updateProfileImage(id, newImageUrl);

            // 성공 응답 반환
            return ResponseEntity.ok(new BaseResponse<>(
                    "프로필 이미지가 성공적으로 업데이트되었습니다", updatedUser, "success"));

        } catch (Exception e) {
            // 오류 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>("프로필 이미지 업로드 중 오류가 발생했습니다: " + e.getMessage(), null, "error"));
        }
    }

}
