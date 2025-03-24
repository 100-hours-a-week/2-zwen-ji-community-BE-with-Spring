package com.example.project.service;

import com.example.project.domain.Liked;
import com.example.project.domain.Post;
import com.example.project.domain.User;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.repository.LikedRepository;
import com.example.project.repository.PostRepository;
import com.example.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LikedService {
    private final LikedRepository likedRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Autowired
    public LikedService(LikedRepository likedRepository, PostRepository postRepository, UserRepository userRepository) {
        this.likedRepository = likedRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    /**
     * 게시글에 좋아요를 추가하는 메서드
     *
     * @param postId 좋아요를 추가할 게시글 ID
     * @param userId 좋아요를 추가하는 사용자 ID
     * @throws ResourceNotFoundException 게시글이나 사용자가 존재하지 않는 경우
     */
    @Transactional
    public void addLike(Long postId, Long userId) {
        // 게시글 존재 여부 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다. ID: " + postId));

        // 게시글이 삭제되었는지 확인
        if (post.isDeleted()) {
            throw new ResourceNotFoundException("삭제된 게시글에는 좋아요를 추가할 수 없습니다.");
        }

        // 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

        // 이미 좋아요를 눌렀는지 확인
        Optional<Liked> existingLike = likedRepository.findByPostIdAndUserId(postId, userId);
        if (existingLike.isPresent()) {
            // 이미 좋아요를 누른 경우 예외를 던지지 않고 그냥 반환
            return;
        }

        // 좋아요 엔티티 생성
        Liked liked = new Liked();
        liked.setPost(post);
        liked.setUser(user);

        // 좋아요 저장
        likedRepository.save(liked);

        // 게시글의 좋아요 수 증가
        post.setLikesCount(post.getLikesCount() + 1);
        postRepository.save(post);
    }

    /**
     * 게시글 좋아요를 취소하는 메서드
     *
     * @param postId 좋아요를 취소할 게시글 ID
     * @param userId 좋아요를 취소하는 사용자 ID
     * @throws ResourceNotFoundException 게시글이나 좋아요가 존재하지 않는 경우
     */
    @Transactional
    public void removeLike(Long postId, Long userId) {
        // 게시글 존재 여부 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다. ID: " + postId));

        // 좋아요 존재 여부 확인
        Liked liked = likedRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 게시글에 좋아요를 누른 기록이 없습니다."));

        // 좋아요 삭제
        likedRepository.delete(liked);

        // 게시글의 좋아요 수 감소
        int currentLikes = post.getLikesCount();
        post.setLikesCount(Math.max(0, currentLikes - 1)); // 음수가 되지 않도록 방지
        postRepository.save(post);
    }

    /**
     * 사용자가 특정 게시글에 좋아요를 눌렀는지 확인하는 메서드
     *
     * @param postId 확인할 게시글 ID
     * @param userId 확인할 사용자 ID
     * @return 좋아요 여부
     */
    public boolean hasUserLikedPost(Long postId, Long userId) {
        return likedRepository.findByPostIdAndUserId(postId, userId).isPresent();
    }
}