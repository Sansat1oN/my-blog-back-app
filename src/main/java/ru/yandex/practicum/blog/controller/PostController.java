package ru.yandex.practicum.blog.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
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
    public PostDto getPost(@PathVariable(name = "id") Long id) {
        return postService.findById(id);
    }

    @PostMapping("/{id}")
    public PostDto getPostByPostMethod(@PathVariable(name = "id") Long id) {
        return getPost(id);
    }

    @PostMapping
    public PostDto createPost(@RequestBody PostRequestDto request) {
        return postService.create(request);
    }

    @PostMapping("/{id}/likes")
    public int addLike(@PathVariable(name = "id") Long id) {
        return postService.addLike(id);
    }

    @PutMapping("/{id}")
    public PostDto updatePost(@PathVariable(name = "id") Long id, @RequestBody PostRequestDto request) {
        return postService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable(name = "id") Long id) {
        postService.delete(id);
    }

    @PutMapping("/{id}/image")
    public void updateImage(@PathVariable(name = "id") Long id,
                            @RequestParam(name = "image") MultipartFile image) {
        postService.updateImage(id, image);
    }

    @GetMapping(value = "/{id}/image", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getImage(@PathVariable(name = "id") Long id) {
        return postService.findImage(id);
    }
}
