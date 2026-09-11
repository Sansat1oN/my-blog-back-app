package ru.yandex.practicum.blog.repository;

import ru.yandex.practicum.blog.model.Comment;

import java.util.List;

public interface CommentRepository {

    List<Comment> findAllByPostId(Long postId);

    Comment findByIdAndPostId(Long id, Long postId);

    Long save(Comment comment);

    void updateByIdAndPostId(Long id, Long postId, String text);

    void deleteByIdAndPostId(Long id, Long postId);

    void deleteByPostId(Long postId);
}
