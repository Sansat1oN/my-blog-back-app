package ru.yandex.practicum.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class PostRequestDto {

    private Long id;

    @NotBlank(message = "Заголовок обязателен")
    private String title;

    @NotBlank(message = "Текст обязателен")
    private String text;

    @NotNull(message = "Список тегов обязателен")
    private List<String> tags;

    public PostRequestDto() {
    }

    public PostRequestDto(Long id, String title, String text, List<String> tags) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.tags = tags;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
