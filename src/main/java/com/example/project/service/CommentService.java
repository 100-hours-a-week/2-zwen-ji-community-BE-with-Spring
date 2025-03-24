package com.example.project.service;

import com.example.project.domain.Comment;
import com.example.project.domain.Post;
import com.example.project.domain.User;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.exception.UnauthorizedException;
import com.example.project.repository.CommentRepository;
import com.example.project.repository.PostRepository;
import com.example.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    /**
     * 댓글을 생성하는 메서드
     *
     * @param postId 댓글을 작성할 게시글 ID
     * @param content 댓글 내용
     * @param userId 댓글 작성자 ID
     * @return 생성된 댓글 엔티티
     * @throws ResourceNotFoundException 게시글이나 사용자가 존재하지 않는 경우
     */
    @Transactional
    public Comment createComment(Long postId, String content, Long userId) {
        // 게시글 존재 여부 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다. ID: " + postId));

        // 게시글이 삭제되었는지 확인
        if (post.isDeleted()) {
            throw new ResourceNotFoundException("삭제된 게시글에는 댓글을 작성할 수 없습니다.");
        }

        // 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

        // 댓글 엔티티 생성
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());

        // 댓글 저장
        Comment savedComment = commentRepository.save(comment);

        // 게시글의 댓글 수 증가
        post.setNumComments(post.getNumComments() + 1);
        postRepository.save(post);

        return savedComment;
    }

        /* 댓글을 물리적으로 삭제하는 메서드
     *
             * @param commentId 삭제할 댓글 ID
     * @param userId 삭제 요청자 ID
     * @throws ResourceNotFoundException 댓글이 존재하지 않는 경우
     * @throws UnauthorizedException 삭제 권한이 없는 경우
     */
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        // 댓글 존재 여부 확인
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글을 찾을 수 없습니다. ID: " + commentId));

        // 댓글 소유권 확인 (작성자만 삭제 가능)
        if (!comment.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("해당 댓글을 삭제할 권한이 없습니다.");
        }

        // 게시글 조회 (댓글 수 감소를 위해)
        Post post = comment.getPost();

        // 물리적 삭제 수행
        commentRepository.delete(comment);

        // 게시글의 댓글 수 감소
        post.setNumComments(post.getNumComments() - 1);
        postRepository.save(post);
    }

    @Transactional
    public Comment updateComment(Long commentId, String content, Long userId)
            throws ResourceNotFoundException, UnauthorizedException {
        // 댓글 존재 여부 확인
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글이 존재하지 않습니다."));

        // 댓글 작성자 확인
        if (!comment.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("댓글 작성자만 수정할 수 있습니다.");
        }

        // 댓글 내용 수정
        comment.setContent(content);
        comment.setUpdatedAt(LocalDateTime.now());
        // 수정된 댓글 저장
        return commentRepository.save(comment);
    }
}