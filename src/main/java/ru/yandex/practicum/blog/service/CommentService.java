package ru.yandex.practicum.blog.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.blog.dto.CommentDto;
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

    public CommentDto findById(Long id) {
        Comment comment = commentRepository.findById(id);
        if (comment == null) {
            return null;
        }
        return toDto(comment);
    }

    public CommentDto create(Long postId, CommentDto request) {
        Comment comment = new Comment(null, postId, request.getText());
        Long id = commentRepository.save(comment);

        return new CommentDto(id, request.getText(), postId);
    }

    public CommentDto update(Long id, CommentDto request) {
        commentRepository.update(id, request.getText());

        return findById(id);
    }

    public void delete(Long id) {
        commentRepository.deleteById(id);
    }

    private CommentDto toDto(Comment comment) {
        return new CommentDto(comment.getId(), comment.getText(), comment.getPostId());
    }
}
