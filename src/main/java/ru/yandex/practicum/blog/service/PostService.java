package ru.yandex.practicum.blog.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.blog.dto.PostDto;
import ru.yandex.practicum.blog.dto.PostRequestDto;
import ru.yandex.practicum.blog.dto.PostsResponseDto;
import ru.yandex.practicum.blog.exception.BadRequestException;
import ru.yandex.practicum.blog.exception.NotFoundException;
import ru.yandex.practicum.blog.model.Post;
import ru.yandex.practicum.blog.repository.CommentRepository;
import ru.yandex.practicum.blog.repository.PostRepository;
import ru.yandex.practicum.blog.repository.TagRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;

    public PostService(PostRepository postRepository,
                       TagRepository tagRepository,
                       CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.commentRepository = commentRepository;
    }

    public PostsResponseDto findAll(String search, int pageNumber, int pageSize) {
        if (pageNumber < 1) {
            throw new BadRequestException("Номер страницы должен быть больше нуля: " + pageNumber);
        }
        if (pageSize < 1) {
            throw new BadRequestException("Размер страницы должен быть больше нуля: " + pageSize);
        }

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

    public byte[] findImage(Long id) {
        checkPostExists(id);

        byte[] image = postRepository.findImageById(id);
        if (image == null) {
            return new byte[0];
        }
        return image;
    }

    public PostDto findById(Long id) {
        Post post = postRepository.findById(id);
        if (post == null) {
            throw new NotFoundException("Пост не найден: " + id);
        }
        return toDto(post);
    }

    @Transactional
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

    @Transactional
    public PostDto update(Long id, PostRequestDto request) {
        checkPostExists(id);

        Post post = new Post(id, request.getTitle(), request.getText(), 0);
        postRepository.update(id, post);

        List<String> tags = request.getTags();
        if (tags == null) {
            tags = new ArrayList<>();
        }
        tagRepository.unlinkTagsFromPost(id);
        saveTags(id, tags);

        return findById(id);
    }

    public void updateImage(Long id, MultipartFile image) {
        checkPostExists(id);

        try {
            postRepository.updateImage(id, image.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public int addLike(Long id) {
        checkPostExists(id);

        postRepository.addLike(id);

        Post post = postRepository.findById(id);
        return post.getLikesCount();
    }

    @Transactional
    public void delete(Long id) {
        checkPostExists(id);

        tagRepository.unlinkTagsFromPost(id);
        commentRepository.deleteByPostId(id);
        postRepository.deleteById(id);
    }

    private void checkPostExists(Long id) {
        if (postRepository.findById(id) == null) {
            throw new NotFoundException("Пост не найден: " + id);
        }
    }

    private String cutText(String text) {
        if (text.length() > 128) {
            return text.substring(0, 128) + "…";
        }
        return text;
    }

    private void saveTags(Long postId, List<String> tags) {
        List<String> savedNames = new ArrayList<>();
        for (String tag : tags) {
            String name = tag.trim().toLowerCase();
            if (savedNames.contains(name)) {
                continue;
            }
            savedNames.add(name);

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
