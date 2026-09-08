package ru.yandex.practicum.blog.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.blog.model.Post;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class JdbcNativePostRepository implements PostRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNativePostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Post> findAll(int pageNumber, int pageSize) {
        return jdbcTemplate.query(
                "select id, title, text, likes_count from posts order by id desc limit ? offset ?",
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getInt("likes_count")
                ),
                pageSize,
                (pageNumber - 1) * pageSize);
    }

    @Override
    public int count() {
        return jdbcTemplate.queryForObject("select count(*) from posts", Integer.class);
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

    @Override
    public Long save(Post post) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "insert into posts(title, text) values(?, ?)",
                    new String[]{"id"});
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getText());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }
}
