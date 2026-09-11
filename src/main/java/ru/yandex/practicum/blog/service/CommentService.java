package ru.yandex.practicum.blog.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.blog.dto.CommentDto;
import ru.yandex.practicum.blog.exception.NotFoundException;
import ru.yandex.practicum.blog.model.Comment;
import ru.yandex.practicum.blog.repository.CommentRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
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

    private CommentDto toDto(Comment comment) {
        return new CommentDto(comment.getId(), comment.getText(), comment.getPostId());
    }
}
