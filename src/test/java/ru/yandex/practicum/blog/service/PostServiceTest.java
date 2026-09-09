package ru.yandex.practicum.blog.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import ru.yandex.practicum.blog.WebConfiguration;
import ru.yandex.practicum.blog.configuration.DataSourceConfiguration;
import ru.yandex.practicum.blog.dto.PostDto;
import ru.yandex.practicum.blog.dto.PostRequestDto;
import ru.yandex.practicum.blog.dto.PostsResponseDto;
import ru.yandex.practicum.blog.model.Comment;
import ru.yandex.practicum.blog.repository.CommentRepository;
import ru.yandex.practicum.blog.repository.PostRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(classes = {DataSourceConfiguration.class, WebConfiguration.class})
@WebAppConfiguration
@TestPropertySource(locations = "classpath:test-application.properties")
class PostServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("delete from post_tags");
        jdbcTemplate.execute("delete from comments");
        jdbcTemplate.execute("delete from posts");
        jdbcTemplate.execute("delete from tags");
    }

    @Test
    void findAll_shouldCutLongTextTo128Symbols() {
        String text = longText();
        savePost("Первый пост", text, new ArrayList<>());

        PostsResponseDto response = postService.findAll("", 1, 10);

        PostDto post = response.getPosts().get(0);
        assertEquals(129, post.getText().length());
        assertEquals(text.substring(0, 128) + "…", post.getText());
    }

    @Test
    void findAll_shouldNotCutShortText() {
        savePost("Первый пост", "Текст первого поста", new ArrayList<>());

        PostsResponseDto response = postService.findAll("", 1, 10);

        assertEquals("Текст первого поста", response.getPosts().get(0).getText());
    }

    @Test
    void findAll_shouldReturnAllPosts_whenSearchIsEmpty() {
        savePost("Первый пост", "Текст первого поста", new ArrayList<>());
        savePost("Второй пост", "Текст второго поста", new ArrayList<>());

        PostsResponseDto response = postService.findAll("", 1, 10);

        assertEquals(2, response.getPosts().size());
    }

    @Test
    void findAll_shouldFindPostsByPartOfTitle() {
        savePost("Первый пост", "Текст первого поста", new ArrayList<>());
        savePost("Второй пост", "Текст второго поста", new ArrayList<>());

        PostsResponseDto response = postService.findAll("первый", 1, 10);

        assertEquals(1, response.getPosts().size());
        assertEquals("Первый пост", response.getPosts().get(0).getTitle());
    }

    @Test
    void findAll_shouldFindPostsByTag() {
        List<String> firstTags = new ArrayList<>();
        firstTags.add("первый");
        savePost("Первый пост", "Текст первого поста", firstTags);

        List<String> secondTags = new ArrayList<>();
        secondTags.add("второй");
        savePost("Второй пост", "Текст второго поста", secondTags);

        PostsResponseDto response = postService.findAll("#первый", 1, 10);

        assertEquals(1, response.getPosts().size());
        assertEquals("Первый пост", response.getPosts().get(0).getTitle());
    }

    @Test
    void findAll_shouldFindPostsByTitleAndTag() {
        List<String> tags = new ArrayList<>();
        tags.add("первый");
        savePost("Первый пост", "Текст первого поста", tags);
        savePost("Первый пост без тега", "Текст второго поста", new ArrayList<>());

        PostsResponseDto response = postService.findAll("Первый #первый", 1, 10);

        assertEquals(1, response.getPosts().size());
        assertEquals("Первый пост", response.getPosts().get(0).getTitle());
    }

    @Test
    void findAll_shouldFindPostsWithAllTags_whenSearchHasTwoTags() {
        List<String> firstTags = new ArrayList<>();
        firstTags.add("первый");
        firstTags.add("второй");
        savePost("Первый пост", "Текст первого поста", firstTags);

        List<String> secondTags = new ArrayList<>();
        secondTags.add("первый");
        savePost("Второй пост", "Текст второго поста", secondTags);

        PostsResponseDto response = postService.findAll("#первый #второй", 1, 10);

        assertEquals(1, response.getPosts().size());
        assertEquals("Первый пост", response.getPosts().get(0).getTitle());
    }

    @Test
    void findAll_shouldCalculatePages_whenFirstPageRequested() {
        savePosts(5);

        PostsResponseDto response = postService.findAll("", 1, 2);

        assertEquals(2, response.getPosts().size());
        assertEquals(3, response.getLastPage());
        assertFalse(response.isHasPrev());
        assertTrue(response.isHasNext());
    }

    @Test
    void findAll_shouldCalculatePages_whenLastPageRequested() {
        savePosts(5);

        PostsResponseDto response = postService.findAll("", 3, 2);

        assertEquals(1, response.getPosts().size());
        assertEquals(3, response.getLastPage());
        assertTrue(response.isHasPrev());
        assertFalse(response.isHasNext());
    }

    @Test
    void findAll_shouldReturnOnePage_whenPostsNotFound() {
        PostsResponseDto response = postService.findAll("", 1, 10);

        assertEquals(0, response.getPosts().size());
        assertEquals(1, response.getLastPage());
        assertFalse(response.isHasPrev());
        assertFalse(response.isHasNext());
    }

    @Test
    void findById_shouldReturnNull_whenPostNotFound() {
        assertNull(postService.findById(999L));
    }

    @Test
    void create_shouldSavePostWithTags() {
        List<String> tags = new ArrayList<>();
        tags.add("первый тег");
        tags.add("второй тег");

        Long id = savePost("Первый пост", "Текст первого поста", tags);

        PostDto saved = postService.findById(id);
        assertEquals("Первый пост", saved.getTitle());
        assertEquals("Текст первого поста", saved.getText());
        assertEquals(2, saved.getTags().size());
        assertEquals(0, saved.getLikesCount());
        assertEquals(0, saved.getCommentsCount());
    }

    @Test
    void update_shouldRewriteTitleTextAndTags() {
        List<String> tags = new ArrayList<>();
        tags.add("первый тег");
        Long id = savePost("Первый пост", "Текст первого поста", tags);

        List<String> newTags = new ArrayList<>();
        newTags.add("второй тег");
        postService.update(id, new PostRequestDto(id, "Второй пост", "Текст второго поста", newTags));

        PostDto updated = postService.findById(id);
        assertEquals("Второй пост", updated.getTitle());
        assertEquals("Текст второго поста", updated.getText());
        assertEquals(1, updated.getTags().size());
        assertEquals("второй тег", updated.getTags().get(0));
    }

    @Test
    void addLike_shouldReturnIncreasedLikesCount() {
        Long id = savePost("Первый пост", "Текст первого поста", new ArrayList<>());

        assertEquals(1, postService.addLike(id));
        assertEquals(2, postService.addLike(id));
    }

    @Test
    void delete_shouldRemovePostWithComments() {
        List<String> tags = new ArrayList<>();
        tags.add("первый тег");
        Long id = savePost("Первый пост", "Текст первого поста", tags);
        commentRepository.save(new Comment(null, id, "Первый комментарий"));

        postService.delete(id);

        assertNull(postRepository.findById(id));
        assertEquals(0, commentRepository.findAllByPostId(id).size());
    }

    @Test
    void findImage_shouldReturnEmptyArray_whenPostHasNoImage() {
        Long id = savePost("Первый пост", "Текст первого поста", new ArrayList<>());

        assertEquals(0, postService.findImage(id).length);
    }

    private Long savePost(String title, String text, List<String> tags) {
        return postService.create(new PostRequestDto(null, title, text, tags)).getId();
    }

    private void savePosts(int count) {
        for (int i = 1; i <= count; i++) {
            savePost("Пост " + i, "Текст поста " + i, new ArrayList<>());
        }
    }

    private String longText() {
        String text = "";
        for (int i = 0; i < 200; i++) {
            text = text + "а";
        }
        return text;
    }
}
