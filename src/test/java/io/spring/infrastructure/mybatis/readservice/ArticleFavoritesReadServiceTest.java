package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.ArticleFavoriteCount;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleFavoriteRepository;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({
  MyBatisArticleRepository.class,
  MyBatisUserRepository.class,
  MyBatisArticleFavoriteRepository.class
})
public class ArticleFavoritesReadServiceTest extends DbTestBase {
  @Autowired private ArticleFavoritesReadService articleFavoritesReadService;
  @Autowired private ArticleRepository articleRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private ArticleFavoriteRepository articleFavoriteRepository;

  private User user1;
  private User user2;
  private Article article1;
  private Article article2;

  @BeforeEach
  public void setUp() {
    user1 = new User("user1@example.com", "user1", "password", "bio", "image");
    user2 = new User("user2@example.com", "user2", "password", "bio", "image");
    userRepository.save(user1);
    userRepository.save(user2);

    article1 = new Article("Article 1", "desc", "body", Arrays.asList("java"), user1.getId());
    article2 = new Article("Article 2", "desc", "body", Arrays.asList("spring"), user1.getId());
    articleRepository.save(article1);
    articleRepository.save(article2);
  }

  @Test
  public void isUserFavorite_userFavoritedArticle_returnsTrue() {
    ArticleFavorite favorite = new ArticleFavorite(article1.getId(), user1.getId());
    articleFavoriteRepository.save(favorite);

    boolean isFavorite =
        articleFavoritesReadService.isUserFavorite(user1.getId(), article1.getId());

    Assertions.assertTrue(isFavorite);
  }

  @Test
  public void isUserFavorite_userNotFavoritedArticle_returnsFalse() {
    boolean isFavorite =
        articleFavoritesReadService.isUserFavorite(user1.getId(), article1.getId());

    Assertions.assertFalse(isFavorite);
  }

  @Test
  public void articleFavoriteCount_noFavorites_returnsZero() {
    int count = articleFavoritesReadService.articleFavoriteCount(article1.getId());

    Assertions.assertEquals(0, count);
  }

  @Test
  public void articleFavoriteCount_multipleFavorites_returnsCorrectCount() {
    articleFavoriteRepository.save(new ArticleFavorite(article1.getId(), user1.getId()));
    articleFavoriteRepository.save(new ArticleFavorite(article1.getId(), user2.getId()));

    int count = articleFavoritesReadService.articleFavoriteCount(article1.getId());

    Assertions.assertEquals(2, count);
  }

  @Test
  public void articlesFavoriteCount_multipleArticles_returnsCorrectCounts() {
    articleFavoriteRepository.save(new ArticleFavorite(article1.getId(), user1.getId()));
    articleFavoriteRepository.save(new ArticleFavorite(article1.getId(), user2.getId()));
    articleFavoriteRepository.save(new ArticleFavorite(article2.getId(), user1.getId()));

    List<String> articleIds = Arrays.asList(article1.getId(), article2.getId());
    List<ArticleFavoriteCount> counts =
        articleFavoritesReadService.articlesFavoriteCount(articleIds);

    Assertions.assertNotNull(counts);
    Assertions.assertEquals(2, counts.size());

    ArticleFavoriteCount count1 =
        counts.stream().filter(c -> c.getId().equals(article1.getId())).findFirst().orElse(null);
    Assertions.assertNotNull(count1);
    Assertions.assertEquals(2, count1.getCount());

    ArticleFavoriteCount count2 =
        counts.stream().filter(c -> c.getId().equals(article2.getId())).findFirst().orElse(null);
    Assertions.assertNotNull(count2);
    Assertions.assertEquals(1, count2.getCount());
  }

  @Test
  public void userFavorites_currentUserFavoritedArticles_returnsArticleIds() {
    articleFavoriteRepository.save(new ArticleFavorite(article1.getId(), user1.getId()));
    articleFavoriteRepository.save(new ArticleFavorite(article2.getId(), user2.getId()));

    List<String> articleIds = Arrays.asList(article1.getId(), article2.getId());
    Set<String> favorites = articleFavoritesReadService.userFavorites(articleIds, user1);

    Assertions.assertNotNull(favorites);
    Assertions.assertEquals(1, favorites.size());
    Assertions.assertTrue(favorites.contains(article1.getId()));
    Assertions.assertFalse(favorites.contains(article2.getId()));
  }

  @Test
  public void userFavorites_noFavorites_returnsEmptySet() {
    List<String> articleIds = Arrays.asList(article1.getId(), article2.getId());
    Set<String> favorites = articleFavoritesReadService.userFavorites(articleIds, user1);

    Assertions.assertNotNull(favorites);
    Assertions.assertTrue(favorites.isEmpty());
  }
}
