package com.example.project.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

// PostListResponse.java
@Getter
@Setter
public class PostListResponse {
    private String message;
    private String status;
    private Data data;

    // 성공
    public PostListResponse(String message, List<PostDto> posts,
                            int currentPage, int totalPages,
                            long totalItems, int itemsPerPage,
                            boolean hasNextPage) {
        this.status = "success";
        this.message = message;
        this.data = new Data(posts,
                new Meta(currentPage, totalPages, totalItems,
                        itemsPerPage, hasNextPage));
    }

    // 중첩 Data 클래스
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Data {
        private List<PostDto> posts;
        private Meta meta;

        public Data(List<PostDto> posts, Meta meta) {
            this.posts = posts;
            this.meta = meta;
        }
    }

    // 중첩 Meta 클래스
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Meta {
        private int currentPage;
        private int totalPages;
        private long totalItems;
        private int itemsPerPage;
        private boolean hasNextPage;

        public Meta(int currentPage, int totalPages, long totalItems,
                    int itemsPerPage, boolean hasNextPage) {
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.totalItems = totalItems;
            this.itemsPerPage = itemsPerPage;
            this.hasNextPage = hasNextPage;
        }
    }

    // 실패
    public PostListResponse(String errorMessage) {
        this.data = null;
        this.message = errorMessage;
        this.status = "error";
    }
}