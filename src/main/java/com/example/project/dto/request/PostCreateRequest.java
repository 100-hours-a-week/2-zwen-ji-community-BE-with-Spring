package com.example.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostCreateRequest {

    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(min = 2, max = 100, message = "제목은 2자 이상 100자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "내용은 필수 입력 항목입니다.")
    @Size(min = 10, max = 10000, message = "내용은 10자 이상 10000자 이하여야 합니다.")
    private String content;

    // 선택적 필드 (이미지 URL)
    private String imageUrl;

    // 기타 필요한 필드들 (태그, 카테고리 등)
}