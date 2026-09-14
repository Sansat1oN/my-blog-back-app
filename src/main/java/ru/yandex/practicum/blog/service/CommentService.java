package ru.yandex.practicum.blog.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.blog.dto.CommentDto;
import ru.yandex.practicum.blog.exception.NotFoundException;
import ru.yandex.practicum.blog.model.Comment;
import ru.yandex.practicum.blog.repository.CommentRepository;
import ru.yandex.practicum.blog.repository.PostRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    public List<CommentDto> findAllByPostId(Long postId) {
        List<CommentDto> result = new ArrayList<>();
        for (Comment comment : commentRepository.findAllByPostId(postId)) {
            result.add(toDto(comment));
        }
        return result;
    }

    public CommentDto findById(Long postId, Long id) {
        Comment comment = commentRepository.findByIdAndPostId(id, postId);
        if (comment == null) {
            throw new NotFoundException("Комментарий не найден: " + id);
        }
        return toDto(comment);
    }

    @Transactional
    public CommentDto create(Long postId, CommentDto request) {
        checkPostExists(postId);

        Comment comment = new Comment(null, postId, request.getText());
        Long id = commentRepository.save(comment);

        return new CommentDto(id, request.getText(), postId);
    }

    @Transactional
    public CommentDto update(Long postId, Long id, CommentDto request) {
        findById(postId, id);

        commentRepository.updateByIdAndPostId(id, postId, request.getText());

        return findById(postId, id);
    }

    @Transactional
    public void delete(Long postId, Long id) {
        findById(postId, id);

        commentRepository.deleteByIdAndPostId(id, postId);
    }

    private void checkPostExists(Long postId) {
        if (postRepository.findById(postId) == null) {
            throw new NotFoundException("Пост не найден: " + postId);
        }
    }

    private CommentDto toDto(Comment comment) {
        return new CommentDto(comment.getId(), comment.getText(), comment.getPostId());
    }
}
