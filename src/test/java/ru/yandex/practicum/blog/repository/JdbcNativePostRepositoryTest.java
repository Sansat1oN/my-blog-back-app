package ru.yandex.practicum.blog.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.blog.AbstractTest;
import ru.yandex.practicum.blog.model.Post;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class JdbcNativePostRepositoryTest extends AbstractTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    @Test
    void save_shouldAddPostToDatabase() {
        Long id = postRepository.save(new Post(null, "Первый пост", "Текст первого поста", 0));

        Post saved = postRepository.findById(id);

        assertNotNull(saved);
        assertEquals("Первый пост", saved.getTitle());
        assertEquals("Текст первого поста", saved.getText());
        assertEquals(0, saved.getLikesCount());
    }

    @Test
    void findById_shouldReturnNull_whenPostNotFound() {
        assertNull(postRepository.findById(999L));
    }

    @Test
    void findAll_shouldFindPostsByPartOfTitleIgnoringCase() {
        postRepository.save(new Post(null, "Первый пост", "Текст первого поста", 0));
        postRepository.save(new Post(null, "Второй пост", "Текст второго поста", 0));

        List<Post> found = postRepository.findAll("ПЕРВЫЙ", new ArrayList<>(), 1, 10);

        assertEquals(1, found.size());
        assertEquals("Первый пост", found.get(0).getTitle());
    }

    @Test
    void findAll_shouldReturnAllPosts_whenSearchIsEmpty() {
        postRepository.save(new Post(null, "Первый пост", "Текст первого поста", 0));
        postRepository.save(new Post(null, "Второй пост", "Текст второго поста", 0));

        assertEquals(2, postRepository.findAll("", new ArrayList<>(), 1, 10).size());
    }

    @Test
    void findAll_shouldFindPostsThatHaveAllTags() {
        Long first = postRepository.save(new Post(null, "Первый пост", "Текст первого поста", 0));
        Long second = postRepository.save(new Post(null, "Второй пост", "Текст второго поста", 0));
        linkTag(first, "первый тег");
        linkTag(first, "второй тег");
        linkTag(second, "первый тег");

        List<String> tags = new ArrayList<>();
        tags.add("первый тег");
        tags.add("второй тег");

        List<Post> found = postRepository.findAll("", tags, 1, 10);

        assertEquals(1, found.size());
        assertEquals("Первый пост", found.get(0).getTitle());
    }

    @Test
    void findAll_shouldReturnPageOfPosts() {
        for (int i = 1; i <= 5; i++) {
            postRepository.save(new Post(null, "Пост " + i, "Текст поста " + i, 0));
        }

        List<Post> secondPage = postRepository.findAll("", new ArrayList<>(), 2, 2);

        assertEquals(2, secondPage.size());
        assertEquals("Пост 3", secondPage.get(0).getTitle());
        assertEquals("Пост 2", secondPage.get(1).getTitle());
    }

    @Test
    void count_shouldReturnNumberOfFoundPosts() {
        postRepository.save(new Post(null, "Первый пост", "Текст первого поста", 0));
        postRepository.save(new Post(null, "Второй пост", "Текст второго поста", 0));
        postRepository.save(new Post(null, "Третий пост", "Текст третьего поста", 0));

        assertEquals(3, postRepository.count("", new ArrayList<>()));
        assertEquals(1, postRepository.count("первый", new ArrayList<>()));
    }

    @Test
    void update_shouldChangeTitleAndText() {
        Long id = postRepository.save(new Post(null, "Первый пост", "Текст первого поста", 0));

        postRepository.update(id, new Post(id, "Второй пост", "Текст второго поста", 0));

        Post updated = postRepository.findById(id);
        assertEquals("Второй пост", updated.getTitle());
        assertEquals("Текст второго поста", updated.getText());
    }

    @Test
    void updateImage_shouldSaveBytesToDatabase() {
        Long id = postRepository.save(new Post(null, "Первый пост", "Текст первого поста", 0));
        byte[] image = new byte[]{1, 2, 3, 4};

        postRepository.updateImage(id, image);

        assertArrayEquals(image, postRepository.findImageById(id));
    }

    @Test
    void findImageById_shouldReturnNull_whenImageNotSaved() {
        Long id = postRepository.save(new Post(null, "Первый пост", "Текст первого поста", 0));

        assertNull(postRepository.findImageById(id));
    }

    @Test
    void addLike_shouldIncrementLikesCount() {
        Long id = postRepository.save(new Post(null, "Первый пост", "Текст первого поста", 0));

        postRepository.addLike(id);
        postRepository.addLike(id);

        assertEquals(2, postRepository.findById(id).getLikesCount());
    }

    @Test
    void deleteById_shouldRemovePost() {
        Long id = postRepository.save(new Post(null, "Первый пост", "Текст первого поста", 0));

        postRepository.deleteById(id);

        assertNull(postRepository.findById(id));
    }

    @Test
    void findTagsByPostId_shouldReturnTagsOfPost() {
        Long id = postRepository.save(new Post(null, "Первый пост", "Текст первого поста", 0));
        linkTag(id, "первый тег");
        linkTag(id, "второй тег");

        List<String> tags = postRepository.findTagsByPostId(id);

        assertEquals(2, tags.size());
        assertEquals("первый тег", tags.get(0));
        assertEquals("второй тег", tags.get(1));
    }

    @Test
    void countCommentsByPostId_shouldReturnNumberOfComments() {
        Long id = postRepository.save(new Post(null, "Первый пост", "Текст первого поста", 0));
        jdbcTemplate.update("insert into comments(post_id, text) values(?, ?)", id, "Первый комментарий");
        jdbcTemplate.update("insert into comments(post_id, text) values(?, ?)", id, "Второй комментарий");

        assertEquals(2, postRepository.countCommentsByPostId(id));
    }

    private void linkTag(Long postId, String name) {
        Long tagId = tagRepository.findIdByName(name);
        if (tagId == null) {
            tagId = tagRepository.save(name);
        }
        tagRepository.linkTagToPost(postId, tagId);
    }
}
