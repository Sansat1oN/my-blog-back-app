package ru.yandex.practicum.blog.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentDto {

    private Long id;

    @NotBlank(message = "Текст обязателен")
    private String text;

    private Long postId;

    public CommentDto() {
    }

    public CommentDto(Long id, String text, Long postId) {
        this.id = id;
        this.text = text;
        this.postId = postId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }
}
