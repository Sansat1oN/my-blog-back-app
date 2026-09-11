package ru.yandex.practicum.blog.repository;

import ru.yandex.practicum.blog.model.Post;

import java.util.List;

public interface PostRepository {

    List<Post> findAll(String search, List<String> tags, int pageNumber, int pageSize);

    int count(String search, List<String> tags);

    Post findById(Long id);

    byte[] findImageById(Long id);

    List<String> findTagsByPostId(Long postId);

    int countCommentsByPostId(Long postId);

    Long save(Post post);

    void update(Long id, Post post);

    void updateImage(Long id, byte[] image);

    void deleteById(Long id);

    void addLike(Long id);
}
