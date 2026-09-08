package ru.yandex.practicum.blog.repository;

import ru.yandex.practicum.blog.model.Comment;

import java.util.List;

public interface CommentRepository {

    List<Comment> findAllByPostId(Long postId);

    Comment findById(Long id);

    Long save(Comment comment);

    void update(Long id, String text);

    void deleteById(Long id);
}
