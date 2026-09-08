package ru.yandex.practicum.blog.model;

public class Post {

    private Long id;
    private String title;
    private String text;
    private int likesCount;

    public Post(Long id, String title, String text, int likesCount) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.likesCount = likesCount;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public int getLikesCount() {
        return likesCount;
    }
}
