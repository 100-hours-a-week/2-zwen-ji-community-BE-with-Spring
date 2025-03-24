package com.example.project.service;

import com.example.project.domain.Comment;
import com.example.project.domain.Post;
import com.example.project.domain.User;
import com.example.project.dto.request.PostCreateRequest;
import com.example.project.dto.request.PostUpdateRequest;
import com.example.project.dto.response.PostDto;
import com.example.project.dto.response.PostDetailResponse;
import com.example.project.dto.response.PostListResponse;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.exception.UnauthorizedException;
import com.example.project.repository.CommentRepository;
import com.example.project.repository.LikedRepository;
import com.example.project.repository.PostRepository;
import com.example.project.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LikedRepository likedRepository;

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository, CommentRepository commentRepository, LikedRepository likedRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.likedRepository = likedRepository;
    }
    private static final String STATIC_UPLOAD_PATH = "resources/static.upload";

    // 웹에서 접근 가능한 URL 경로
    private static final String UPLOAD_URL_PATH = "/static.upload";

    // 서버의 기본 URL (개발 환경과 프로덕션 환경에서 다를 수 있음)
    @Value("${app.base.url:}")

    private String baseUrl; // 기본값은 빈 문자열로 설정 (상대 경로 사용)
    // 모든 게시글 조회
    @Transactional(readOnly = true)
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    // 페이징을 사용한 게시글 조회
    @Transactional(readOnly = true)
    public Page<Post> getPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    // ID로 게시글 조회
    @Transactional(readOnly = true)
    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }


    // 게시글 생성
    @Transactional
    public Post createPost(PostCreateRequest postRequest, Long userId) {
        // 1. 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AccessDeniedException("해당 사용자를 찾을 수 없습니다."));

        // 2. 새 게시글 엔티티 생성
        Post post = new Post();
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setImageUrl(postRequest.getImageUrl());
        post.setUser(user);
        post.setLikesCount(0);
        post.setViewsCount(0);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());


        // 3. 게시글 저장 및 반환
        return postRepository.save(post);
    }

    // 게시글 업데이트
    @Transactional
    public Post updatePost(Long id, @Valid PostUpdateRequest postDetails, Long userId) {
        // 게시글 조회
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다. ID: " + id));

        // 게시글 소유자 확인
        if (!existingPost.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("이 게시글을 수정할 권한이 없습니다.");
        }

        // 게시글 정보 업데이트
        existingPost.setTitle(postDetails.getTitle());
        existingPost.setContent(postDetails.getContent());
        existingPost.setImageUrl(postDetails.getImageUrl());
        existingPost.setUpdatedAt(LocalDateTime.now());

        // 변경된 게시글 저장 및 반환
        return postRepository.save(existingPost);
    }

    /**
     * 게시글을 논리적으로 삭제하는 메서드
     * @param postId 삭제할 게시글 ID
     * @param userId 삭제 요청자 ID
     * @throws ResourceNotFoundException 게시글이 존재하지 않을 경우
     * @throws UnauthorizedException 삭제 권한이 없는 경우
     */
    public void deletePost(Long postId, Long userId) {
        // 게시글 존재 여부 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다. ID: " + postId));

        // 게시글 소유권 확인
        if (!post.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("해당 게시글을 삭제할 권한이 없습니다.");
        }

        // 논리적 삭제 처리: isDeleted 필드를 true로 설정
        post.setDeleted(true);

        // 변경사항 저장
        postRepository.save(post);
    }


    // 게시글 삭제 (논리적 삭제)
    @Transactional
    public void softDeletePost(Long id) {
        postRepository.findById(id).ifPresent(post -> {
            post.setDeleted(true);
            postRepository.save(post);
        });
    }

    // 조회수 증가
    @Transactional
    public void incrementViewCount(Long id) {
        postRepository.findById(id).ifPresent(post -> {
            post.setViewsCount(post.getViewsCount() + 1);
            postRepository.save(post);
        });
    }

    @Transactional(readOnly = true)
    public PostListResponse getPostsByPage(int page, int limit) {
        // JPA의 페이지는 0부터 시작하므로 page - 1을 사용
        PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 삭제되지 않은 게시글만 조회하도록 수정
        Page<Post> postPage = postRepository.findByIsDeletedFalse(pageRequest);

        List<PostDto> posts = new ArrayList<>();
        for (Post post : postPage.getContent()) {
            PostDto postDto = convertToDto(post);
            posts.add(postDto);
        }

        // 기존의 PostListResponse 생성자를 유지
        return new PostListResponse(
                "게시글 목록 조회 성공",
                posts,
                page,
                postPage.getTotalPages(),
                postPage.getTotalElements(),
                limit,
                postPage.hasNext()
        );
    }


    private PostDto convertToDto(Post post) {
        PostDto dto = new PostDto();
        dto.setPostId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setAuthorNickname(post.getUser().getNickname());
        dto.setAuthorProfileImageUrl(post.getUser().getProfileImageUrl());
        dto.setLikesCount(post.getLikes().size());
        dto.setViewsCount(post.getViewsCount());
        dto.setCommentsCount(post.getComments().size());

        // ISO 8601 형식으로 날짜 포맷팅
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dto.setCreatedAt(post.getCreatedAt().format(formatter));

        return dto;
    }

    public void incrementViews() {

    }
    /**
     * 게시물 상세 정보를 조회합니다.
     *
     * @param id 게시물 ID
     * @return 게시물 상세 정보 응답 객체
     * @throws ResourceNotFoundException 게시물이 존재하지 않을 경우 발생
     */
    public PostDetailResponse getPostDetail(Long id, Long userId) {
        // 1. 게시물 엔티티 조회
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("게시물을 찾을 수 없습니다. ID: " + id));

        // 2. 조회수 증가 (필요한 경우)
        post.incrementViews();
        postRepository.save(post);

        // 3. 작성자 정보 조회
        User author = userRepository.findById(post.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다. ID: " + post.getUser().getId()));

        // 4. 댓글 정보 조회
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(id);

        // 5. Response DTO 생성
        PostDetailResponse response = new PostDetailResponse();
        response.setMessage("게시물 조회 성공");



        // 6. Data 객체 생성 및 설정
        PostDetailResponse.Data data = new PostDetailResponse.Data();
        data.setPostId(post.getId());
        data.setTitle(post.getTitle());
        data.setContent(post.getContent());
        data.setImageUrl(post.getImageUrl());
        data.setAuthorUserId(author.getId());
        data.setAuthorNickname(author.getNickname());
        data.setAuthorProfileImageUrl(author.getProfileImageUrl());
        data.setLikedByCurrentUser(IsLikedByCurrentUser(id,userId));
        data.setLikesCount(post.getLikesCount());
        data.setViewsCount(post.getViewsCount());
        data.setCommentsCount(comments.size());
        data.setCreatedAt(post.getCreatedAt().toString());
        data.setUpdatedAt(post.getUpdatedAt() != null ? post.getUpdatedAt().toString() : null);



       // 7. 댓글 목록 생성 및 설정
        List<PostDetailResponse.Data.Comment> commentDtos = new ArrayList<>();

        for (Comment comment : comments) {
            User commentAuthor = userRepository.findById(comment.getUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("댓글 작성자를 찾을 수 없습니다. ID: " + comment.getUser().getId()));

            PostDetailResponse.Data.Comment commentDto = new PostDetailResponse.Data.Comment();
            commentDto.setCommentId(comment.getId());
            commentDto.setContent(comment.getContent());
            commentDto.setAuthorUserId(commentAuthor.getId());
            commentDto.setAuthorNickname(commentAuthor.getNickname());
            commentDto.setAuthorProfileImageUrl(commentAuthor.getProfileImageUrl());
            commentDto.setCreatedAt(comment.getCreatedAt().toString());
            commentDto.setUpdatedAt(comment.getUpdatedAt() != null ? comment.getUpdatedAt().toString() : null);

            commentDtos.add(commentDto);
        }
        commentDtos.sort(Comparator.comparing(PostDetailResponse.Data.Comment::getCommentId));
        // 댓글 목록을 직접 data 객체에 설정
        data.setComments(commentDtos);

        // 8. Response에 Data 설정
        response.setData(data);

        return response;
    }

    public boolean IsLikedByCurrentUser(Long id, Long userId) {
        return likedRepository.existsByPostIdAndUserId(id, userId);
    }

    public String uploadPostImage(Long postId, MultipartFile imageFile, Long userId)
            throws ResourceNotFoundException, UnauthorizedException {

        // 게시글 존재 여부 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + postId));

        // 권한 확인: 게시글 작성자만 이미지 업로드 가능
        if (!post.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("이 게시글에 이미지를 업로드할 권한이 없습니다.");
        }

        // 이미지 저장 처리
        String imageUrl = saveImage(imageFile, "posts/" + postId);

        // 게시글 이미지 URL 업데이트
        post.setImageUrl(imageUrl);
        postRepository.save(post);

        return imageUrl;
    }

    public String uploadTempImage(MultipartFile imageFile, Long userId) {
        // 이미지 저장 처리 (임시 폴더에 저장)
        return saveImage(imageFile, "posts/temp/" + userId);
    }

    /**
     * 이미지 파일을 저장하고 URL을 반환합니다.
     *
     * @param imageFile 저장할 이미지 파일
     * @param subPath 이미지를 저장할 하위 경로 (예: "posts/123")
     * @return 저장된 이미지의 URL
     */
    private String saveImage(MultipartFile imageFile, String subPath) {
        try {
            // 원본 파일명에서 확장자 추출
            String originalFilename = imageFile.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 고유한 파일명 생성 (UUID + 원래 확장자)
            String newFilename = UUID.randomUUID().toString() + extension;

            // 저장 경로 생성 및 디렉토리 확인
            String projectRoot = System.getProperty("user.dir");
            String uploadPath = projectRoot + "/src/main/resources/static.upload/" + subPath;
            File directory = new File(uploadPath);
            if (!directory.exists()) {
                directory.mkdirs(); // 디렉토리가 없으면 생성
            }

            // 파일 저장
            File destinationFile = new File(uploadPath + File.separator + newFilename);
            imageFile.transferTo(destinationFile);

            // 이미지 URL 반환 (웹에서 접근 가능한 경로)
            return "/static.upload/" + subPath + "/" + newFilename;

        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}