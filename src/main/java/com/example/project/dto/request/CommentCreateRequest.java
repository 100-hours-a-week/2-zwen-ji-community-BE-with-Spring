package com.example.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommentCreateRequest {

    @NotNull(message = "게시글 ID는 필수 입력 항목입니다.")
    private Long postId;

    @NotBlank(message = "댓글 내용은 필수 입력 항목입니다.")
    private String content;
}