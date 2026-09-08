package ru.yandex.practicum.blog.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.blog.dto.PostDto;
import ru.yandex.practicum.blog.model.Post;
import ru.yandex.practicum.blog.repository.PostRepository;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public PostDto findById(Long id) {
        Post post = postRepository.findById(id);
        if (post == null) {
            return null;
        }
        return toDto(post);
    }

    private PostDto toDto(Post post) {
        List<String> tags = postRepository.findTagsByPostId(post.getId());
        int commentsCount = postRepository.countCommentsByPostId(post.getId());

        return new PostDto(
                post.getId(),
                post.getTitle(),
                post.getText(),
                tags,
                post.getLikesCount(),
                commentsCount);
    }
}
