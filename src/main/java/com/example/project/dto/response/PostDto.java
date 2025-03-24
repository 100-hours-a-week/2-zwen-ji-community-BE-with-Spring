package com.example.project.dto.response;

import lombok.Getter;
import lombok.Setter;

// PostDto.java
@Setter
@Getter
public class PostDto {
    private Long postId;
    private String title;
    private String authorNickname;
    private String authorProfileImageUrl;
    private int likesCount;
    private int viewsCount;
    private int commentsCount;
    private String createdAt;
}