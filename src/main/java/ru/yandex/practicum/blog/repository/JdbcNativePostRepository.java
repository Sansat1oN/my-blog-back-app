package ru.yandex.practicum.blog.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.blog.model.Post;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcNativePostRepository implements PostRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNativePostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Post> findAll(String search, List<String> tags, int pageNumber, int pageSize) {
        String sql = "select id, title, text, likes_count from posts where lower(title) like ?"
                + tagsCondition(tags)
                + " order by id desc limit ? offset ?";

        List<Object> params = searchParams(search, tags);
        params.add(pageSize);
        params.add((pageNumber - 1) * pageSize);

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getInt("likes_count")
                ),
                params.toArray());
    }

    @Override
    public int count(String search, List<String> tags) {
        String sql = "select count(*) from posts where lower(title) like ?" + tagsCondition(tags);

        List<Object> params = searchParams(search, tags);

        return jdbcTemplate.queryForObject(sql, Integer.class, params.toArray());
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
    public byte[] findImageById(Long id) {
        List<byte[]> images = jdbcTemplate.query(
                "select image from posts where id = ?",
                (rs, rowNum) -> rs.getBytes("image"),
                id);

        if (images.isEmpty()) {
            return null;
        }
        return images.get(0);
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

    @Override
    public void update(Long id, Post post) {
        jdbcTemplate.update(
                "update posts set title = ?, text = ? where id = ?",
                post.getTitle(),
                post.getText(),
                id);
    }

    @Override
    public void updateImage(Long id, byte[] image) {
        jdbcTemplate.update("update posts set image = ? where id = ?", image, id);
    }

    @Override
    public void addLike(Long id) {
        jdbcTemplate.update("update posts set likes_count = likes_count + 1 where id = ?", id);
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("delete from comments where post_id = ?", id);
        jdbcTemplate.update("delete from posts where id = ?", id);
    }

    private String tagsCondition(List<String> tags) {
        String condition = "";
        for (int i = 0; i < tags.size(); i++) {
            condition = condition
                    + " and posts.id in (select post_tags.post_id from post_tags"
                    + " join tags on tags.id = post_tags.tag_id"
                    + " where lower(tags.name) = ?)";
        }
        return condition;
    }

    private List<Object> searchParams(String search, List<String> tags) {
        List<Object> params = new ArrayList<>();
        params.add("%" + search.toLowerCase() + "%");
        for (String tag : tags) {
            params.add(tag.toLowerCase());
        }
        return params;
    }
}
