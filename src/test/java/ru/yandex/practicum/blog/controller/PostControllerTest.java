package ru.yandex.practicum.blog.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.yandex.practicum.blog.WebConfiguration;
import ru.yandex.practicum.blog.configuration.DataSourceConfiguration;
import ru.yandex.practicum.blog.dto.PostRequestDto;
import ru.yandex.practicum.blog.service.PostService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig(classes = {DataSourceConfiguration.class, WebConfiguration.class})
@WebAppConfiguration
@TestPropertySource(locations = "classpath:test-application.properties")
class PostControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PostService postService;

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
    void getPosts_shouldReturnAllPosts() throws Exception {
        savePost("Первый пост", "Текст первого поста", new ArrayList<>());
        savePost("Второй пост", "Текст второго поста", new ArrayList<>());

        mockMvc.perform(get("/api/posts")
                        .param("search", "")
                        .param("pageNumber", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.posts.length()").value(2))
                .andExpect(jsonPath("$.lastPage").value(1))
                .andExpect(jsonPath("$.hasPrev").value(false))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void getPosts_shouldFilterPostsByTitle() throws Exception {
        savePost("Первый пост", "Текст первого поста", new ArrayList<>());
        savePost("Второй пост", "Текст второго поста", new ArrayList<>());

        mockMvc.perform(get("/api/posts")
                        .param("search", "первый")
                        .param("pageNumber", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.length()").value(1))
                .andExpect(jsonPath("$.posts[0].title").value("Первый пост"));
    }

    @Test
    void getPosts_shouldFilterPostsByTag() throws Exception {
        List<String> tags = new ArrayList<>();
        tags.add("первый");
        savePost("Первый пост", "Текст первого поста", tags);
        savePost("Второй пост", "Текст второго поста", new ArrayList<>());

        mockMvc.perform(get("/api/posts")
                        .param("search", "#первый")
                        .param("pageNumber", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.length()").value(1))
                .andExpect(jsonPath("$.posts[0].title").value("Первый пост"));
    }

    @Test
    void getPosts_shouldReturnRequestedPage() throws Exception {
        savePost("Первый пост", "Текст первого поста", new ArrayList<>());
        savePost("Второй пост", "Текст второго поста", new ArrayList<>());
        savePost("Третий пост", "Текст третьего поста", new ArrayList<>());

        mockMvc.perform(get("/api/posts")
                        .param("search", "")
                        .param("pageNumber", "2")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.length()").value(1))
                .andExpect(jsonPath("$.lastPage").value(2))
                .andExpect(jsonPath("$.hasPrev").value(true))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void getPost_shouldReturnPost() throws Exception {
        List<String> tags = new ArrayList<>();
        tags.add("первый тег");
        Long id = savePost("Первый пост", "Текст первого поста", tags);

        mockMvc.perform(get("/api/posts/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Первый пост"))
                .andExpect(jsonPath("$.text").value("Текст первого поста"))
                .andExpect(jsonPath("$.tags.length()").value(1))
                .andExpect(jsonPath("$.likesCount").value(0))
                .andExpect(jsonPath("$.commentsCount").value(0));
    }

    @Test
    void getPost_shouldReturnNotFound_whenPostNotFound() throws Exception {
        mockMvc.perform(get("/api/posts/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPost_shouldAddPostToDatabase() throws Exception {
        String json = "{\"title\":\"Первый пост\","
                + "\"text\":\"Текст первого поста\","
                + "\"tags\":[\"первый тег\"]}";

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Первый пост"))
                .andExpect(jsonPath("$.text").value("Текст первого поста"))
                .andExpect(jsonPath("$.tags.length()").value(1));

        assertEquals(1, countPosts("Первый пост"));
    }

    @Test
    void updatePost_shouldChangePostInDatabase() throws Exception {
        Long id = savePost("Первый пост", "Текст первого поста", new ArrayList<>());
        String json = "{\"id\":" + id + ","
                + "\"title\":\"Второй пост\","
                + "\"text\":\"Текст второго поста\","
                + "\"tags\":[\"второй тег\"]}";

        mockMvc.perform(put("/api/posts/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Второй пост"))
                .andExpect(jsonPath("$.tags[0]").value("второй тег"));

        assertEquals(0, countPosts("Первый пост"));
        assertEquals(1, countPosts("Второй пост"));
    }

    @Test
    void deletePost_shouldRemovePostFromDatabase() throws Exception {
        Long id = savePost("Первый пост", "Текст первого поста", new ArrayList<>());

        mockMvc.perform(delete("/api/posts/{id}", id))
                .andExpect(status().isOk());

        assertEquals(0, countPosts("Первый пост"));
    }

    @Test
    void addLike_shouldReturnLikesCount() throws Exception {
        Long id = savePost("Первый пост", "Текст первого поста", new ArrayList<>());

        mockMvc.perform(post("/api/posts/{id}/likes", id))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        mockMvc.perform(post("/api/posts/{id}/likes", id))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }

    @Test
    void updateImage_shouldSaveImage() throws Exception {
        Long id = savePost("Первый пост", "Текст первого поста", new ArrayList<>());
        byte[] bytes = new byte[]{1, 2, 3};
        MockMultipartFile image = new MockMultipartFile(
                "image", "image.jpg", MediaType.IMAGE_JPEG_VALUE, bytes);

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/posts/{id}/image", id).file(image))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/{id}/image", id))
                .andExpect(status().isOk())
                .andExpect(content().bytes(bytes));
    }

    @Test
    void getImage_shouldReturnEmptyBody_whenPostHasNoImage() throws Exception {
        Long id = savePost("Первый пост", "Текст первого поста", new ArrayList<>());

        mockMvc.perform(get("/api/posts/{id}/image", id))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[0]));
    }

    @Test
    void getPosts_shouldReturnBadRequest_whenPageNumberIsZero() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("search", "")
                        .param("pageNumber", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPosts_shouldReturnBadRequest_whenPageNumberIsNegative() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("search", "")
                        .param("pageNumber", "-1")
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPosts_shouldReturnBadRequest_whenPageSizeIsZero() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("search", "")
                        .param("pageNumber", "1")
                        .param("pageSize", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPost_shouldReturnBadRequest_whenTitleIsBlank() throws Exception {
        String json = "{\"title\":\"\",\"text\":\"Текст первого поста\",\"tags\":[]}";

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPost_shouldReturnBadRequest_whenTextIsMissing() throws Exception {
        String json = "{\"title\":\"Первый пост\",\"tags\":[]}";

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPost_shouldReturnBadRequest_whenTagsAreMissing() throws Exception {
        String json = "{\"title\":\"Первый пост\",\"text\":\"Текст первого поста\"}";

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePost_shouldReturnBadRequest_whenTitleIsBlank() throws Exception {
        Long id = savePost("Первый пост", "Текст первого поста", new ArrayList<>());
        String json = "{\"title\":\"\",\"text\":\"Текст первого поста\",\"tags\":[]}";

        mockMvc.perform(put("/api/posts/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    private Long savePost(String title, String text, List<String> tags) {
        return postService.create(new PostRequestDto(null, title, text, tags)).getId();
    }

    private int countPosts(String title) {
        return jdbcTemplate.queryForObject(
                "select count(*) from posts where title = ?", Integer.class, title);
    }
}
