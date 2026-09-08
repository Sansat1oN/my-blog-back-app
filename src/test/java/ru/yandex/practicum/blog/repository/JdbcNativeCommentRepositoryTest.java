package ru.yandex.practicum.blog.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import ru.yandex.practicum.blog.WebConfiguration;
import ru.yandex.practicum.blog.configuration.DataSourceConfiguration;
import ru.yandex.practicum.blog.model.Comment;
import ru.yandex.practicum.blog.model.Post;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringJUnitConfig(classes = {DataSourceConfiguration.class, WebConfiguration.class})
@WebAppConfiguration
@TestPropertySource(locations = "classpath:test-application.properties")
class JdbcNativeCommentRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("delete from post_tags");
        jdbcTemplate.execute("delete from comments");
        jdbcTemplate.execute("delete from posts");
        jdbcTemplate.execute("delete from tags");
    }

    @Test
    void save_shouldAddCommentToDatabase() {
        Long postId = savePost("Первый пост");

        Long id = commentRepository.save(new Comment(null, postId, "Первый комментарий"));

        Comment saved = commentRepository.findById(id);
        assertNotNull(saved);
        assertEquals("Первый комментарий", saved.getText());
        assertEquals(postId, saved.getPostId());
    }

    @Test
    void findById_shouldReturnNull_whenCommentNotFound() {
        assertNull(commentRepository.findById(999L));
    }

    @Test
    void findAllByPostId_shouldReturnCommentsOfPost() {
        Long firstPostId = savePost("Первый пост");
        Long secondPostId = savePost("Второй пост");
        commentRepository.save(new Comment(null, firstPostId, "Первый комментарий"));
        commentRepository.save(new Comment(null, firstPostId, "Второй комментарий"));
        commentRepository.save(new Comment(null, secondPostId, "Третий комментарий"));

        List<Comment> comments = commentRepository.findAllByPostId(firstPostId);

        assertEquals(2, comments.size());
        assertEquals("Первый комментарий", comments.get(0).getText());
        assertEquals("Второй комментарий", comments.get(1).getText());
    }

    @Test
    void findAllByPostId_shouldReturnEmptyList_whenPostHasNoComments() {
        Long postId = savePost("Первый пост");

        assertEquals(0, commentRepository.findAllByPostId(postId).size());
    }

    @Test
    void update_shouldChangeCommentText() {
        Long postId = savePost("Первый пост");
        Long id = commentRepository.save(new Comment(null, postId, "Первый комментарий"));

        commentRepository.update(id, "Второй комментарий");

        assertEquals("Второй комментарий", commentRepository.findById(id).getText());
    }

    @Test
    void deleteById_shouldRemoveComment() {
        Long postId = savePost("Первый пост");
        Long id = commentRepository.save(new Comment(null, postId, "Первый комментарий"));

        commentRepository.deleteById(id);

        assertNull(commentRepository.findById(id));
    }

    @Test
    void deleteByPostId_shouldRemoveAllCommentsOfPost() {
        Long firstPostId = savePost("Первый пост");
        Long secondPostId = savePost("Второй пост");
        commentRepository.save(new Comment(null, firstPostId, "Первый комментарий"));
        commentRepository.save(new Comment(null, firstPostId, "Второй комментарий"));
        commentRepository.save(new Comment(null, secondPostId, "Третий комментарий"));

        commentRepository.deleteByPostId(firstPostId);

        assertEquals(0, commentRepository.findAllByPostId(firstPostId).size());
        assertEquals(1, commentRepository.findAllByPostId(secondPostId).size());
    }

    private Long savePost(String title) {
        return postRepository.save(new Post(null, title, "Текст поста", 0));
    }
}
