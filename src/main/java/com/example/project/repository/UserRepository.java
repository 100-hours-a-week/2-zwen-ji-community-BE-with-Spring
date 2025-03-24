package com.example.project.repository;

import com.example.project.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<Object> findByNickname(String nickname);
    // JpaRepository<User, Long>을 상속받으면 기본 CRUD 메서드가 자동 생성됩니다.
    // 필요하다면 추가 메서드를 여기에 선언할 수 있습니다.
}