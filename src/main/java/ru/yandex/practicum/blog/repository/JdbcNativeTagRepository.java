package ru.yandex.practicum.blog.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class JdbcNativeTagRepository implements TagRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNativeTagRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long findIdByName(String name) {
        List<Long> ids = jdbcTemplate.query(
                "select id from tags where name = ?",
                (rs, rowNum) -> rs.getLong("id"),
                name);

        if (ids.isEmpty()) {
            return null;
        }
        return ids.get(0);
    }

    @Override
    public Long save(String name) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "insert into tags(name) values(?)",
                    new String[]{"id"});
            ps.setString(1, name);
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public void linkTagToPost(Long postId, Long tagId) {
        jdbcTemplate.update("insert into post_tags(post_id, tag_id) values(?, ?)", postId, tagId);
    }

    @Override
    public void unlinkTagsFromPost(Long postId) {
        jdbcTemplate.update("delete from post_tags where post_id = ?", postId);
    }
}
