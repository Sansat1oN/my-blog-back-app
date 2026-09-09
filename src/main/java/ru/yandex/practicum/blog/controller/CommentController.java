package ru.yandex.practicum.blog.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.blog.dto.CommentDto;
import ru.yandex.practicum.blog.service.CommentService;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/posts")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/{postId}/comments")
    public List<CommentDto> getComments(@PathVariable(name = "postId") Long postId) {
        return commentService.findAllByPostId(postId);
    }

    @GetMapping("/{postId}/comments/{id}")
    public CommentDto getComment(@PathVariable(name = "id") Long id) {
        return commentService.findById(id);
    }

    @PostMapping("/{postId}/comments")
    public CommentDto createComment(@PathVariable(name = "postId") Long postId,
                                    @RequestBody CommentDto request) {
        return commentService.create(postId, request);
    }

    @PutMapping("/{postId}/comments/{id}")
    public CommentDto updateComment(@PathVariable(name = "id") Long id,
                                    @RequestBody CommentDto request) {
        return commentService.update(id, request);
    }

    @DeleteMapping("/{postId}/comments/{id}")
    public void deleteComment(@PathVariable(name = "id") Long id) {
        commentService.delete(id);
    }
}
