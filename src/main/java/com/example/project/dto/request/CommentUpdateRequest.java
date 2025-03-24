package com.example.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 댓글 수정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentUpdateRequest {

    /**
     * 댓글 내용
     */
    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(min = 1, max = 500, message = "댓글 내용은 1~500자 사이여야 합니다.")
    private String content;
}