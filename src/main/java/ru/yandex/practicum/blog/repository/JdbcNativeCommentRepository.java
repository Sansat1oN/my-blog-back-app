package ru.yandex.practicum.blog.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.blog.model.Comment;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class JdbcNativeCommentRepository implements CommentRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNativeCommentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Comment> findAllByPostId(Long postId) {
        return jdbcTemplate.query(
                "select id, post_id, text from comments where post_id = ? order by id",
                (rs, rowNum) -> new Comment(
                        rs.getLong("id"),
                        rs.getLong("post_id"),
                        rs.getString("text")
                ),
                postId);
    }

    @Override
    public Comment findById(Long id) {
        List<Comment> comments = jdbcTemplate.query(
                "select id, post_id, text from comments where id = ?",
                (rs, rowNum) -> new Comment(
                        rs.getLong("id"),
                        rs.getLong("post_id"),
                        rs.getString("text")
                ),
                id);

        if (comments.isEmpty()) {
            return null;
        }
        return comments.get(0);
    }

    @Override
    public Long save(Comment comment) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "insert into comments(post_id, text) values(?, ?)",
                    new String[]{"id"});
            ps.setLong(1, comment.getPostId());
            ps.setString(2, comment.getText());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public void update(Long id, String text) {
        jdbcTemplate.update("update comments set text = ? where id = ?", text, id);
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("delete from comments where id = ?", id);
    }
}
