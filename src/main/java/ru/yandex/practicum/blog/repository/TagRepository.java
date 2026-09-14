package ru.yandex.practicum.blog.repository;

public interface TagRepository {

    Long findIdByName(String name);

    Long save(String name);

    void linkTagToPost(Long postId, Long tagId);

    void unlinkTagsFromPost(Long postId);
}
