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

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }
}
