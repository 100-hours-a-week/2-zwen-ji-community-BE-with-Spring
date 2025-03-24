package com.example.project.controller;

import com.example.project.domain.User;
import com.example.project.dto.request.LoginRequest;
import com.example.project.dto.response.ApiResponse;
import com.example.project.repository.UserRepository;
import com.example.project.security.CustomUserDetails;
import com.example.project.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"})
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // 인증 수행
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );


            SecurityContextHolder.getContext().setAuthentication(authentication);
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // JWT 토큰 생성
            String jwt = tokenProvider.createToken(userDetails.getUsername(), userDetails.getId());

            String profileImageUrl = userDetails.getProfileImageUrl();
            if (profileImageUrl == null || profileImageUrl.trim().isEmpty()) {
                profileImageUrl = "/static.upload/default/default-profile.png";
            }
            // 응답 데이터 생성 (토큰 포함)
            Map<String, Object> data = new HashMap<>();
            data.put("userId", userDetails.getId());
            data.put("profileImageUrl", profileImageUrl);
            data.put("token", jwt);

            // 응답 반환
            return ResponseEntity.ok(new ApiResponse("로그인 성공", data, "success"));
        } catch (BadCredentialsException e) {
            // 인증 실패 시 오류 응답
            return ResponseEntity.status(401)
                    .body(new ApiResponse("이메일 또는 비밀번호가 올바르지 않습니다.", null, "error"));
        } catch (DisabledException e) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse("이메일 또는 비밀번호가 올바르지 않습니다.", null, "error"));
        } catch (Exception e) {
            // 기타 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("로그인 중 오류가 발생했습니다: " + e.getMessage(), null, "error"));
        }
    }
}