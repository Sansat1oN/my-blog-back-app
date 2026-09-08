package ru.yandex.practicum.blog.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.blog.dto.PostDto;
import ru.yandex.practicum.blog.dto.PostRequestDto;
import ru.yandex.practicum.blog.model.Post;
import ru.yandex.practicum.blog.repository.PostRepository;
import ru.yandex.practicum.blog.repository.TagRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;

    public PostService(PostRepository postRepository, TagRepository tagRepository) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
    }

    public PostDto findById(Long id) {
        Post post = postRepository.findById(id);
        if (post == null) {
            return null;
        }
        return toDto(post);
    }

    public PostDto create(PostRequestDto request) {
        Post post = new Post(null, request.getTitle(), request.getText(), 0);
        Long id = postRepository.save(post);

        List<String> tags = request.getTags();
        if (tags == null) {
            tags = new ArrayList<>();
        }
        saveTags(id, tags);

        return new PostDto(id, request.getTitle(), request.getText(), tags, 0, 0);
    }

    private void saveTags(Long postId, List<String> tags) {
        for (String name : tags) {
            Long tagId = tagRepository.findIdByName(name);
            if (tagId == null) {
                tagId = tagRepository.save(name);
            }
            tagRepository.linkTagToPost(postId, tagId);
        }
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
