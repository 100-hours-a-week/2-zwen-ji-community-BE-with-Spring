package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailResponse {
    private String message;
    private Data data;
    private String status = "success";

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private Long postId;
        private String title;
        private String content;
        private String imageUrl;
        private Long authorUserId;
        private String authorNickname;
        private String authorProfileImageUrl;
        private boolean isLikedByCurrentUser;
        private int likesCount;
        private int viewsCount;
        private int commentsCount;
        private String createdAt;
        private String updatedAt;
        private List<Comment> comments;


        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Comment {
            private Long commentId;
            private String content;
            private Long authorUserId;
            private String authorNickname;
            private String authorProfileImageUrl;
            private String createdAt;
            private String updatedAt;
        }

    }
}

