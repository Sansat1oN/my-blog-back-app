package ru.yandex.practicum.blog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.blog.repository.JdbcNativeCommentRepository;
import ru.yandex.practicum.blog.repository.JdbcNativePostRepository;
import ru.yandex.practicum.blog.repository.JdbcNativeTagRepository;

@DataJdbcTest
@Import({JdbcNativePostRepository.class, JdbcNativeCommentRepository.class, JdbcNativeTagRepository.class})
public abstract class AbstractRepositoryTest {

    @Autowired
    protected JdbcTemplate jdbcTemplate;
}
