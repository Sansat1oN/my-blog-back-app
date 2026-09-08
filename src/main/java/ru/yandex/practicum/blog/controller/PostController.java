package ru.yandex.practicum.blog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.blog.dto.PostDto;
import ru.yandex.practicum.blog.dto.PostRequestDto;
import ru.yandex.practicum.blog.dto.PostsResponseDto;
import ru.yandex.practicum.blog.service.PostService;

@CrossOrigin
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public PostsResponseDto getPosts(
            @RequestParam(name = "search") String search,
            @RequestParam(name = "pageNumber") int pageNumber,
            @RequestParam(name = "pageSize") int pageSize) {
        return postService.findAll(search, pageNumber, pageSize);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getPost(@PathVariable(name = "id") Long id) {
        PostDto post = postService.findById(id);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(post);
    }

    @PostMapping("/{id}")
    public ResponseEntity<PostDto> getPostByPostMethod(@PathVariable(name = "id") Long id) {
        return getPost(id);
    }

    @PostMapping
    public PostDto createPost(@RequestBody PostRequestDto request) {
        return postService.create(request);
    }
}
