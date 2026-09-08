package ru.yandex.practicum.blog.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.blog.model.Post;

import java.util.List;

@Repository
public class JdbcNativePostRepository implements PostRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNativePostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Post findById(Long id) {
        List<Post> posts = jdbcTemplate.query(
                "select id, title, text, likes_count from posts where id = ?",
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getInt("likes_count")
                ),
                id);

        if (posts.isEmpty()) {
            return null;
        }
        return posts.get(0);
    }

    @Override
    public List<String> findTagsByPostId(Long postId) {
        return jdbcTemplate.query(
                "select tags.name from tags join post_tags on post_tags.tag_id = tags.id where post_tags.post_id = ?",
                (rs, rowNum) -> rs.getString("name"),
                postId);
    }

    @Override
    public int countCommentsByPostId(Long postId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from comments where post_id = ?",
                Integer.class,
                postId);
    }
}
