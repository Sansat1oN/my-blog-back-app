package ru.yandex.practicum.blog.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.blog.dto.PostDto;
import ru.yandex.practicum.blog.dto.PostRequestDto;
import ru.yandex.practicum.blog.dto.PostsResponseDto;
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

    public PostsResponseDto findAll(String search, int pageNumber, int pageSize) {
        String query = "";
        if (search != null) {
            query = search.trim();
        }

        List<String> words = new ArrayList<>();
        List<String> tags = new ArrayList<>();
        for (String word : query.split(" ")) {
            if (word.isEmpty()) {
                continue;
            }
            if (word.startsWith("#")) {
                tags.add(word.substring(1));
            } else {
                words.add(word);
            }
        }

        String titleSearch = String.join(" ", words);

        List<Post> posts = postRepository.findAll(titleSearch, tags, pageNumber, pageSize);

        List<PostDto> result = new ArrayList<>();
        for (Post post : posts) {
            PostDto postDto = toDto(post);
            postDto.setText(cutText(postDto.getText()));
            result.add(postDto);
        }

        int total = postRepository.count(titleSearch, tags);

        int lastPage = total / pageSize;
        if (total % pageSize > 0) {
            lastPage = lastPage + 1;
        }
        if (lastPage == 0) {
            lastPage = 1;
        }

        boolean hasPrev = pageNumber > 1;
        boolean hasNext = pageNumber < lastPage;

        return new PostsResponseDto(result, hasPrev, hasNext, lastPage);
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

    private String cutText(String text) {
        if (text.length() > 128) {
            return text.substring(0, 128) + "…";
        }
        return text;
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
