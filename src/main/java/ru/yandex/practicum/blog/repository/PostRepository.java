package ru.yandex.practicum.blog.repository;

import ru.yandex.practicum.blog.model.Post;

import java.util.List;

public interface PostRepository {

    List<Post> findAll(String search, int pageNumber, int pageSize);

    int count(String search);

    Post findById(Long id);

    List<String> findTagsByPostId(Long postId);

    int countCommentsByPostId(Long postId);

    Long save(Post post);
}
