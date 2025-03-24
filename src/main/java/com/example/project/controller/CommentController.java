package com.example.project.controller;

import com.example.project.domain.Comment;
import com.example.project.dto.request.CommentCreateRequest;
import com.example.project.dto.request.CommentUpdateRequest;
import com.example.project.dto.response.BaseResponse;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.exception.UnauthorizedException;
import com.example.project.security.CustomUserDetails;
import com.example.project.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"})
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 댓글 등록 API
     *
     * @param commentRequest 댓글 생성 요청 데이터
     * @param userDetails 인증된 사용자 정보
     * @return 댓글 등록 결과
     */
    @PostMapping
    public ResponseEntity<BaseResponse<Void>> createComment(
            @RequestBody @Valid CommentCreateRequest commentRequest,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            // 인증 확인
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new BaseResponse<>("접근 권한이 없습니다.", null, "AUTH_FAILED"));
            }

            // 현재 인증된 사용자 ID 가져오기
            Long userId = ((CustomUserDetails) userDetails).getId();

            // 댓글 생성 서비스 호출
            commentService.createComment(commentRequest.getPostId(), commentRequest.getContent(), userId);

            // 성공 응답 반환
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new BaseResponse<>("댓글이 성공적으로 등록되었습니다", null, "success"));

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
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            // 인증 확인
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new BaseResponse<>("접근 권한이 없습니다.", null, "AUTH_FAILED"));
            }

            // 현재 인증된 사용자 ID 가져오기
            Long userId = ((CustomUserDetails) userDetails).getId();

            // 댓글 삭제 서비스 호출
            commentService.deleteComment(id, userId);

            // 성공 응답 반환
            return ResponseEntity.ok()
                    .body(new BaseResponse<>("댓글이 성공적으로 삭제되었습니다", null, "success"));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new BaseResponse<>(e.getMessage(), null, "not_found"));

        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new BaseResponse<>(e.getMessage(), null, "forbidden"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>("internal_server_error", null, "error"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> updateComment(
            @PathVariable Long id,
            @RequestBody @Valid CommentUpdateRequest commentRequest,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            // 인증 확인
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new BaseResponse<>("접근 권한이 없습니다.", null, "AUTH_FAILED"));
            }

            // 현재 인증된 사용자 ID 가져오기
            Long userId = ((CustomUserDetails) userDetails).getId();

            // 댓글 수정 서비스 호출
            commentService.updateComment(id, commentRequest.getContent(), userId);

            // 성공 응답 반환
            return ResponseEntity.ok()
                    .body(new BaseResponse<>("댓글이 성공적으로 수정되었습니다", null, "success"));

        } catch (ResourceNotFoundException e) {
            // 댓글이 존재하지 않는 경우
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new BaseResponse<>(e.getMessage(), null, "not_found"));

        } catch (UnauthorizedException e) {
            // 댓글 작성자가 아닌 경우
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new BaseResponse<>(e.getMessage(), null, "forbidden"));

        } catch (AccessDeniedException e) {
            // 접근 권한이 없는 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new BaseResponse<>("접근 권한이 없습니다.", null, "AUTH_FAILED"));

        } catch (Exception e) {
            // 서버 내부 오류
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>("서버 오류가 발생했습니다", null, "error"));
        }
    }
}