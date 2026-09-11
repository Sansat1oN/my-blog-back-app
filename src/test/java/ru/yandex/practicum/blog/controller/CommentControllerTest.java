package ru.yandex.practicum.blog.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.yandex.practicum.blog.WebConfiguration;
import ru.yandex.practicum.blog.configuration.DataSourceConfiguration;
import ru.yandex.practicum.blog.dto.PostRequestDto;
import ru.yandex.practicum.blog.model.Comment;
import ru.yandex.practicum.blog.repository.CommentRepository;
import ru.yandex.practicum.blog.service.PostService;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig(classes = {DataSourceConfiguration.class, WebConfiguration.class})
@WebAppConfiguration
@TestPropertySource(locations = "classpath:test-application.properties")
class CommentControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PostService postService;

    @Autowired
    private CommentRepository commentRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        jdbcTemplate.execute("delete from post_tags");
        jdbcTemplate.execute("delete from comments");
        jdbcTemplate.execute("delete from posts");
        jdbcTemplate.execute("delete from tags");
    }

    @Test
    void getComments_shouldReturnCommentsOfPost() throws Exception {
        Long postId = savePost("Первый пост");
        saveComment(postId, "Первый комментарий");
        saveComment(postId, "Второй комментарий");

        mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].text").value("Первый комментарий"))
                .andExpect(jsonPath("$[0].postId").value(postId))
                .andExpect(jsonPath("$[1].text").value("Второй комментарий"));
    }

    @Test
    void getComments_shouldReturnEmptyList_whenPostHasNoComments() throws Exception {
        Long postId = savePost("Первый пост");

        mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getComment_shouldReturnComment() throws Exception {
        Long postId = savePost("Первый пост");
        Long id = saveComment(postId, "Первый комментарий");

        mockMvc.perform(get("/api/posts/{postId}/comments/{id}", postId, id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.text").value("Первый комментарий"))
                .andExpect(jsonPath("$.postId").value(postId));
    }

    @Test
    void getComment_shouldReturnNotFound_whenCommentNotFound() throws Exception {
        Long postId = savePost("Первый пост");

        mockMvc.perform(get("/api/posts/{postId}/comments/{id}", postId, 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void createComment_shouldAddCommentToDatabase() throws Exception {
        Long postId = savePost("Первый пост");
        String json = "{\"text\":\"Первый комментарий\"}";

        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Первый комментарий"))
                .andExpect(jsonPath("$.postId").value(postId));

        assertEquals(1, countComments("Первый комментарий"));
    }

    @Test
    void updateComment_shouldChangeCommentInDatabase() throws Exception {
        Long postId = savePost("Первый пост");
        Long id = saveComment(postId, "Первый комментарий");
        String json = "{\"id\":" + id + ",\"text\":\"Второй комментарий\",\"postId\":" + postId + "}";

        mockMvc.perform(put("/api/posts/{postId}/comments/{id}", postId, id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.text").value("Второй комментарий"));

        assertEquals(0, countComments("Первый комментарий"));
        assertEquals(1, countComments("Второй комментарий"));
    }

    @Test
    void deleteComment_shouldRemoveCommentFromDatabase() throws Exception {
        Long postId = savePost("Первый пост");
        Long id = saveComment(postId, "Первый комментарий");

        mockMvc.perform(delete("/api/posts/{postId}/comments/{id}", postId, id))
                .andExpect(status().isOk());

        assertEquals(0, countComments("Первый комментарий"));
    }

    @Test
    void getComment_shouldReturnNotFound_whenCommentBelongsToAnotherPost() throws Exception {
        Long firstPostId = savePost("Первый пост");
        Long secondPostId = savePost("Второй пост");
        Long id = saveComment(firstPostId, "Первый комментарий");

        mockMvc.perform(get("/api/posts/{postId}/comments/{id}", secondPostId, id))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateComment_shouldReturnNotFound_whenCommentBelongsToAnotherPost() throws Exception {
        Long firstPostId = savePost("Первый пост");
        Long secondPostId = savePost("Второй пост");
        Long id = saveComment(firstPostId, "Первый комментарий");
        String json = "{\"id\":" + id + ",\"text\":\"Второй комментарий\",\"postId\":" + secondPostId + "}";

        mockMvc.perform(put("/api/posts/{postId}/comments/{id}", secondPostId, id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());

        assertEquals(1, countComments("Первый комментарий"));
    }

    @Test
    void deleteComment_shouldReturnNotFound_whenCommentBelongsToAnotherPost() throws Exception {
        Long firstPostId = savePost("Первый пост");
        Long secondPostId = savePost("Второй пост");
        Long id = saveComment(firstPostId, "Первый комментарий");

        mockMvc.perform(delete("/api/posts/{postId}/comments/{id}", secondPostId, id))
                .andExpect(status().isNotFound());

        assertEquals(1, countComments("Первый комментарий"));
    }

    @Test
    void createComment_shouldReturnBadRequest_whenTextIsBlank() throws Exception {
        Long postId = savePost("Первый пост");
        String json = "{\"text\":\"\"}";

        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateComment_shouldReturnBadRequest_whenTextIsMissing() throws Exception {
        Long postId = savePost("Первый пост");
        Long id = saveComment(postId, "Первый комментарий");
        String json = "{\"id\":" + id + ",\"postId\":" + postId + "}";

        mockMvc.perform(put("/api/posts/{postId}/comments/{id}", postId, id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_shouldReturnBadRequest_whenJsonIsInvalid() throws Exception {
        Long postId = savePost("Первый пост");
        String json = "{\"text\":";

        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    private Long savePost(String title) {
        return postService.create(new PostRequestDto(null, title, "Текст первого поста", new ArrayList<>())).getId();
    }

    private Long saveComment(Long postId, String text) {
        return commentRepository.save(new Comment(null, postId, text));
    }

    private int countComments(String text) {
        return jdbcTemplate.queryForObject(
                "select count(*) from comments where text = ?", Integer.class, text);
    }
}
