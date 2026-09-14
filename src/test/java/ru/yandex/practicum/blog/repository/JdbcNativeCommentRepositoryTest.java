package ru.yandex.practicum.blog.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.blog.AbstractTest;
import ru.yandex.practicum.blog.model.Comment;
import ru.yandex.practicum.blog.model.Post;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class JdbcNativeCommentRepositoryTest extends AbstractTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Test
    void save_shouldAddCommentToDatabase() {
        Long postId = savePost("Первый пост");

        Long id = commentRepository.save(new Comment(null, postId, "Первый комментарий"));

        Comment saved = commentRepository.findByIdAndPostId(id, postId);
        assertNotNull(saved);
        assertEquals("Первый комментарий", saved.getText());
        assertEquals(postId, saved.getPostId());
    }

    @Test
    void findByIdAndPostId_shouldReturnNull_whenCommentNotFound() {
        Long postId = savePost("Первый пост");

        assertNull(commentRepository.findByIdAndPostId(999L, postId));
    }

    @Test
    void findByIdAndPostId_shouldReturnNull_whenCommentBelongsToAnotherPost() {
        Long firstPostId = savePost("Первый пост");
        Long secondPostId = savePost("Второй пост");
        Long id = commentRepository.save(new Comment(null, firstPostId, "Первый комментарий"));

        assertNull(commentRepository.findByIdAndPostId(id, secondPostId));
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
    void updateByIdAndPostId_shouldChangeCommentText() {
        Long postId = savePost("Первый пост");
        Long id = commentRepository.save(new Comment(null, postId, "Первый комментарий"));

        commentRepository.updateByIdAndPostId(id, postId, "Второй комментарий");

        assertEquals("Второй комментарий", commentRepository.findByIdAndPostId(id, postId).getText());
    }

    @Test
    void updateByIdAndPostId_shouldNotChangeComment_whenCommentBelongsToAnotherPost() {
        Long firstPostId = savePost("Первый пост");
        Long secondPostId = savePost("Второй пост");
        Long id = commentRepository.save(new Comment(null, firstPostId, "Первый комментарий"));

        commentRepository.updateByIdAndPostId(id, secondPostId, "Второй комментарий");

        assertEquals("Первый комментарий", commentRepository.findByIdAndPostId(id, firstPostId).getText());
    }

    @Test
    void deleteByIdAndPostId_shouldRemoveComment() {
        Long postId = savePost("Первый пост");
        Long id = commentRepository.save(new Comment(null, postId, "Первый комментарий"));

        commentRepository.deleteByIdAndPostId(id, postId);

        assertNull(commentRepository.findByIdAndPostId(id, postId));
    }

    @Test
    void deleteByIdAndPostId_shouldNotRemoveComment_whenCommentBelongsToAnotherPost() {
        Long firstPostId = savePost("Первый пост");
        Long secondPostId = savePost("Второй пост");
        Long id = commentRepository.save(new Comment(null, firstPostId, "Первый комментарий"));

        commentRepository.deleteByIdAndPostId(id, secondPostId);

        assertNotNull(commentRepository.findByIdAndPostId(id, firstPostId));
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
