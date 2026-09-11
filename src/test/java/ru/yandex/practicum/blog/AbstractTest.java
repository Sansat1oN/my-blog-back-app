package ru.yandex.practicum.blog;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import ru.yandex.practicum.blog.configuration.DataSourceConfiguration;


@SpringJUnitConfig(classes = {DataSourceConfiguration.class, WebConfiguration.class})
@WebAppConfiguration
@TestPropertySource(locations = "classpath:test-application.properties")
public abstract class AbstractTest {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    protected void cleanDatabase() {
        jdbcTemplate.execute("delete from post_tags");
        jdbcTemplate.execute("delete from comments");
        jdbcTemplate.execute("delete from posts");
        jdbcTemplate.execute("delete from tags");
    }
}
