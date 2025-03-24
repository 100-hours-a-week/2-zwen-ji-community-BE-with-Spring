package com.example.project.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 게시글 수정 요청 클래스
@Getter
@Setter
@NoArgsConstructor
public class PostUpdateRequest {
//    @NotBlank(message = "제목은 필수 입력 항목입니다.")
//    @Size(min = 2, max = 100, message = "제목은 2자 이상 100자 이하여야 합니다.")
    private String title;

//    @NotBlank(message = "내용은 필수 입력 항목입니다.")
//    @Size(min = 10, max = 10000, message = "내용은 10자 이상 10000자 이하여야 합니다.")
    private String content;

    private String imageUrl;
    private String message;
    private String status = "success";
}