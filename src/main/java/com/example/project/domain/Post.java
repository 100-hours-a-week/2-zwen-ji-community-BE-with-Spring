package com.example.project.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Setter
@Getter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Column(nullable = false, length = 30)
    private String title;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(length = 255)
    private String imageUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private int likesCount = 0;

    @Column(nullable = false)
    private int viewsCount = 0;

    @Column(nullable = false)
    private int numComments = 0;

    @Column(nullable = false)
    private boolean isDeleted = false;

    // 양방향 매핑 (필요한 경우)
    @OneToMany(mappedBy = "post")
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<Liked> likes = new ArrayList<>();

    // 기본 생성자
    public Post() {}

    // 관계 접근자
    public List<Comment> getComments() {
        return comments;
    }

    public List<Liked> getLikes() {
        return likes;
    }

    public void incrementViews() {
        viewsCount++;
    }
}