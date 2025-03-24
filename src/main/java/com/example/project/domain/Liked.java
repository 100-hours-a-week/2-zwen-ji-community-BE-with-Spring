package com.example.project.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "liked")
@IdClass(Liked.LikedPK.class)
public class Liked {
    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    // 기본 생성자 (JPA 요구사항)
    public Liked() {}

    // 매개변수가 있는 생성자
    public Liked(User user, Post post) {
        this.user = user;
        this.post = post;
    }

    // Getter와 Setter
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    // equals와 hashCode 메서드
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Liked liked = (Liked) o;
        return Objects.equals(user, liked.user) &&
                Objects.equals(post, liked.post);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, post);
    }

    // 복합키 클래스
    public static class LikedPK implements Serializable {
        private Long user; // User 엔티티의 id와 매핑됨
        private Long post; // Post 엔티티의 id와 매핑됨

        // 기본 생성자 (JPA 요구사항)
        public LikedPK() {}

        // 매개변수가 있는 생성자
        public LikedPK(Long userId, Long postId) {
            this.user = userId;
            this.post = postId;
        }

        // Getter와 Setter
        public Long getUser() {
            return user;
        }

        public void setUser(Long user) {
            this.user = user;
        }

        public Long getPost() {
            return post;
        }

        public void setPost(Long post) {
            this.post = post;
        }

        // equals와 hashCode 메서드
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LikedPK likedPK = (LikedPK) o;
            return Objects.equals(user, likedPK.user) &&
                    Objects.equals(post, likedPK.post);
        }

        @Override
        public int hashCode() {
            return Objects.hash(user, post);
        }
    }
}