package com.example.project.repository;

import com.example.project.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdOrderByCreatedAtDesc(Long id);
    // JpaRepository<Comment, Long>을 상속받으면 기본 CRUD 메서드가 자동 생성됩니다.
    // 필요하다면 추가 메서드를 여기에 선언할 수 있습니다.
}