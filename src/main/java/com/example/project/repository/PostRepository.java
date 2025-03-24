package com.example.project.repository;

import com.example.project.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByIsDeletedFalse(PageRequest pageRequest);
    // JpaRepository<Post, Long>을 상속받으면 기본 CRUD 메서드가 자동 생성됩니다.
    // 필요하다면 추가 메서드를 여기에 선언할 수 있습니다.
}