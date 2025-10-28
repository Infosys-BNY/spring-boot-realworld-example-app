package io.spring.infrastructure.mybatis.readservice;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({MyBatisArticleRepository.class, MyBatisUserRepository.class})
public class TagReadServiceTest extends DbTestBase {
  @Autowired private TagReadService tagReadService;
  @Autowired private ArticleRepository articleRepository;
  @Autowired private UserRepository userRepository;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("test@example.com", "testuser", "password", "bio", "image");
    userRepository.save(user);
  }

  @Test
  public void all_noTags_returnsEmptyList() {
    List<String> tags = tagReadService.all();
    Assertions.assertNotNull(tags);
    Assertions.assertTrue(tags.isEmpty());
  }

  @Test
  public void all_withTags_returnsAllUniqueTags() {
    Article article1 =
        new Article("Title 1", "desc", "body", Arrays.asList("java", "spring"), user.getId());
    Article article2 =
        new Article("Title 2", "desc", "body", Arrays.asList("spring", "boot"), user.getId());
    articleRepository.save(article1);
    articleRepository.save(article2);

    List<String> tags = tagReadService.all();

    Assertions.assertNotNull(tags);
    Assertions.assertEquals(3, tags.size());
    Assertions.assertTrue(tags.contains("java"));
    Assertions.assertTrue(tags.contains("spring"));
    Assertions.assertTrue(tags.contains("boot"));
  }

  @Test
  public void all_withDuplicateTags_returnsUniqueTagsOnly() {
    Article article1 = new Article("Title 1", "desc", "body", Arrays.asList("java"), user.getId());
    Article article2 = new Article("Title 2", "desc", "body", Arrays.asList("java"), user.getId());
    articleRepository.save(article1);
    articleRepository.save(article2);

    List<String> tags = tagReadService.all();

    Assertions.assertNotNull(tags);
    Assertions.assertEquals(1, tags.size());
    Assertions.assertEquals("java", tags.get(0));
  }
}
