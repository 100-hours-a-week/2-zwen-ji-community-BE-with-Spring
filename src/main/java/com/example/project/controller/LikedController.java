package com.example.project.controller;

import com.example.project.dto.response.BaseResponse;
import com.example.project.service.LikedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.project.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import com.example.project.security.CustomUserDetails;

@RestController
@RequestMapping("/likes")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"})
public class LikedController {

    private final LikedService likedService;

    @Autowired
    public LikedController(LikedService likeService) {
        this.likedService = likeService;
    }

    /**
     * 게시글에 좋아요 추가 API
     *
     * @param postId      좋아요를 추가할 게시글 ID
     * @param userDetails 현재 인증된 사용자 정보
     * @return 좋아요 추가 결과
     */
    @PostMapping("/{postId}")
    public ResponseEntity<BaseResponse<Void>> addLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            // 인증 확인
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new BaseResponse<>("접근 권한이 없습니다.", null, "AUTH_FAILED"));
            }

            // 현재 인증된 사용자 ID 가져오기
            Long userId = ((CustomUserDetails) userDetails).getId();

            // 좋아요 추가 서비스 호출
            likedService.addLike(postId, userId);

            // 성공 응답 반환
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new BaseResponse<>("좋아요가 성공적으로 추가되었습니다.", null, "success"));

        } catch (ResourceNotFoundException e) {
            // 게시글이 존재하지 않는 경우
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new BaseResponse<>(e.getMessage(), null, "invalid_request"));

        } catch (AccessDeniedException e) {
            // 접근 권한이 없는 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new BaseResponse<>("접근 권한이 없습니다.", null, "AUTH_FAILED"));

        } catch (Exception e) {
            // 서버 내부 오류
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>("internal_server_error", null, "error"));
        }
    }
    /**
     * 게시글 좋아요 취소 API
     *
     * @param postId 좋아요를 취소할 게시글 ID
     * @param userDetails 현재 인증된 사용자 정보
     * @return 좋아요 취소 결과
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<BaseResponse<Void>> removeLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            // 인증 확인
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new BaseResponse<>("접근 권한이 없습니다.", null, "AUTH_FAILED"));
            }

            // 현재 인증된 사용자 ID 가져오기
            Long userId = ((CustomUserDetails) userDetails).getId();

            // 좋아요 취소 서비스 호출
            likedService.removeLike(postId, userId);

            // 성공 응답 반환
            return ResponseEntity.ok()
                    .body(new BaseResponse<>("좋아요가 성공적으로 취소되었습니다.", null, "success"));

        } catch (ResourceNotFoundException e) {
            // 게시글이나 좋아요가 존재하지 않는 경우
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new BaseResponse<>(e.getMessage(), null, "invalid_request"));

        } catch (AccessDeniedException e) {
            // 접근 권한이 없는 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new BaseResponse<>("접근 권한이 없습니다.", null, "AUTH_FAILED"));

        } catch (Exception e) {
            // 서버 내부 오류
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>("internal_server_error", null, "error"));
        }
    }
}