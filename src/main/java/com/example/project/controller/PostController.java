package com.example.project.controller;

import com.example.project.domain.Post;
import com.example.project.dto.request.PostCreateRequest;
import com.example.project.dto.request.PostUpdateRequest;
import com.example.project.dto.response.BaseResponse;
import com.example.project.dto.response.ImageUploadResponse;
import com.example.project.dto.response.PostDetailResponse;
import com.example.project.dto.response.PostDto;
import com.example.project.dto.response.PostListResponse;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.exception.UnauthorizedException;
import com.example.project.security.CustomUserDetails;
import com.example.project.service.PostService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/posts")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"})
public class PostController {
    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<PostListResponse> getPosts(
            @RequestParam(value="page") int page,
            @RequestParam(value="limit") int limit) {

        PostListResponse response = postService.getPostsByPage(page, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            // 인증되지 않은 사용자 처리
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new BaseResponse<>("로그인이 필요합니다.", null, "error"));
            }

            // CustomUserDetails로 캐스팅하여 ID 가져오기
            Long userId = ((CustomUserDetails) userDetails).getId();

            // 게시글 조회 서비스 호출 (userId를 전달)
            PostDetailResponse response = postService.getPostDetail(id, userId);

            return ResponseEntity.ok(response);

        } catch (AccessDeniedException e) {
            // 권한 없음 오류 (비공개 게시글 등)
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new BaseResponse<>("해당 게시글에 접근할 권한이 없습니다.", null, "error"));

        } catch (EntityNotFoundException e) {
            // 게시글을 찾을 수 없는 경우
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new BaseResponse<>("게시글을 찾을 수 없습니다.", null, "error"));

        } catch (Exception e) {
            // 그 외 모든 오류
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>("게시글 조회 중 오류가 발생했습니다: " + e.getMessage(), null, "error"));
        }
    }

    @PostMapping
    public ResponseEntity<BaseResponse<Long>> createPost(
            @RequestBody @Valid PostCreateRequest postRequest,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            // userDetails가 null이면 인증되지 않은 사용자
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new BaseResponse<>("로그인이 필요합니다.", null, "error"));
            }


            Long userId = ((CustomUserDetails) userDetails).getId();
            Long createdPostId = postService.createPost(postRequest, userId).getId();

            // 성공 응답 반환
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new BaseResponse<>("게시글이 성공적으로 생성되었습니다.", createdPostId, "success"));

        } catch (AccessDeniedException e) {
            // 권한 없음 오류
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new BaseResponse<>("게시글을 생성할 권한이 없습니다.", null, "error"));

        } catch (jakarta.validation.ValidationException e) {
            // 유효성 검사 오류 (Jakarta 패키지 사용)
            return ResponseEntity.badRequest()
                    .body(new BaseResponse<>(e.getMessage(), null, "error"));

        } catch (Exception e) {
            // 그 외 모든 오류
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>("게시글을 생성하는 중 오류가 발생했습니다: " + e.getMessage(), null, "error"));
        }
    }



    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<Long>> updatePost(
            @PathVariable Long id,
            @RequestBody @Valid PostUpdateRequest updateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            // 인증된 사용자 확인
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new BaseResponse<>("로그인이 필요합니다.", null, "error"));
            }

            Long userId = ((CustomUserDetails) userDetails).getId();

            // 게시글 소유권 확인 및 업데이트 수행
            Long updatedPostId = postService.updatePost(id, updateRequest,userId).getId();


            return ResponseEntity.ok()
                    .body(new BaseResponse<>("게시글이 성공적으로 수정되었습니다.", updatedPostId, "success"));

        } catch (ResourceNotFoundException e) {
            // 게시글을 찾을 수 없는 경우
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new BaseResponse<>(e.getMessage(), null, "error"));

        } catch (UnauthorizedException e) {
            // 권한 없음 오류
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new BaseResponse<>(e.getMessage(), null, "error"));

        } catch (AccessDeniedException e) {
            // 권한 없음 오류 (Spring Security 예외)
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new BaseResponse<>("게시글을 수정할 권한이 없습니다.", null, "error"));

        } catch (jakarta.validation.ValidationException e) {
            // 유효성 검사 오류 (Jakarta 패키지 사용)
            return ResponseEntity.badRequest()
                    .body(new BaseResponse<>(e.getMessage(), null, "error"));

        } catch (Exception e) {
            // 그 외 모든 오류
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>("게시글을 수정하는 중 오류가 발생했습니다: " + e.getMessage(), null, "error"));
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            // userDetails가 null이면 인증되지 않은 사용자
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new BaseResponse<>("로그인이 필요합니다.", null, "error"));
            }

            // 인증된 사용자 ID 가져오기
            Long userId = ((CustomUserDetails) userDetails).getId();

            // 게시글 소유권 확인 및 삭제 수행
            postService.deletePost(id, userId);

            // 성공 응답 생성
            return ResponseEntity.ok()
                    .body(new BaseResponse<>("게시글이 성공적으로 삭제되었습니다.", null, "success"));

        } catch (ResourceNotFoundException e) {
            // 게시글을 찾을 수 없는 경우
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new BaseResponse<>(e.getMessage(), null, "error"));

        } catch (AccessDeniedException | UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new BaseResponse<>(e.getMessage(), null, "error"));

        } catch (Exception e) {
            // 그 외 모든 오류
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>("게시글을 삭제하는 중 오류가 발생했습니다: " + e.getMessage(), null, "error"));
        }
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<ImageUploadResponse>> uploadTempImage(
            @RequestParam("image") MultipartFile imageFile,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            // 인증되지 않은 사용자 처리
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new BaseResponse<>("로그인이 필요합니다.", null, "error"));
            }

            // 이미지 파일 검증
            if (imageFile == null || imageFile.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new BaseResponse<>("이미지 파일이 제공되지 않았습니다.", null, "error"));
            }

            // 파일 타입 검증
            String contentType = imageFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(new BaseResponse<>("유효한 이미지 파일이 아닙니다.", null, "error"));
            }

            // 인증된 사용자 ID 가져오기
            Long userId = ((CustomUserDetails) userDetails).getId();

            // 이미지 저장 서비스 호출
            String imageUrl = postService.uploadTempImage(imageFile, userId);

            return ResponseEntity.ok()
                    .body(new BaseResponse<>("이미지가 성공적으로 업로드되었습니다.",
                            new ImageUploadResponse(imageUrl), "success"));

        } catch (Exception e) {
            // 그 외 모든 오류
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BaseResponse<>("이미지 업로드 중 오류가 발생했습니다: " + e.getMessage(), null, "error"));
        }
    }

}

