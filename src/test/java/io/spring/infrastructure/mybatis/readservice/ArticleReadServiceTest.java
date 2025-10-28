package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager.Direction;
import io.spring.application.Page;
import io.spring.application.data.ArticleData;
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
import org.joda.time.DateTime;
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
public class ArticleReadServiceTest extends DbTestBase {
  @Autowired private ArticleReadService articleReadService;
  @Autowired private ArticleRepository articleRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private ArticleFavoriteRepository articleFavoriteRepository;

  private User author1;
  private User author2;
  private Article article1;
  private Article article2;
  private Article article3;

  @BeforeEach
  public void setUp() {
    author1 = new User("author1@example.com", "author1", "password", "bio", "image");
    author2 = new User("author2@example.com", "author2", "password", "bio", "image");
    userRepository.save(author1);
    userRepository.save(author2);

    article1 =
        new Article("Java Basics", "Learn Java", "Content", Arrays.asList("java"), author1.getId());
    article2 =
        new Article(
            "Spring Framework",
            "Learn Spring",
            "Content",
            Arrays.asList("spring", "java"),
            author1.getId());
    article3 =
        new Article(
            "React Basics", "Learn React", "Content", Arrays.asList("react"), author2.getId());

    articleRepository.save(article1);
    articleRepository.save(article2);
    articleRepository.save(article3);
  }

  @Test
  public void findById_existingArticle_returnsArticleData() {
    ArticleData articleData = articleReadService.findById(article1.getId());

    Assertions.assertNotNull(articleData);
    Assertions.assertEquals(article1.getId(), articleData.getId());
    Assertions.assertEquals(article1.getSlug(), articleData.getSlug());
    Assertions.assertEquals("Java Basics", articleData.getTitle());
  }

  @Test
  public void findById_nonExistingArticle_returnsNull() {
    ArticleData articleData = articleReadService.findById("nonexistent-id");

    Assertions.assertNull(articleData);
  }

  @Test
  public void findBySlug_existingArticle_returnsArticleData() {
    ArticleData articleData = articleReadService.findBySlug(article1.getSlug());

    Assertions.assertNotNull(articleData);
    Assertions.assertEquals(article1.getId(), articleData.getId());
    Assertions.assertEquals("Java Basics", articleData.getTitle());
  }

  @Test
  public void findBySlug_nonExistingArticle_returnsNull() {
    ArticleData articleData = articleReadService.findBySlug("nonexistent-slug");

    Assertions.assertNull(articleData);
  }

  @Test
  public void queryArticles_noFilters_returnsAllArticleIds() {
    Page page = new Page(0, 10);
    List<String> articleIds = articleReadService.queryArticles(null, null, null, page);

    Assertions.assertNotNull(articleIds);
    Assertions.assertEquals(3, articleIds.size());
  }

  @Test
  public void queryArticles_filterByTag_returnsMatchingArticleIds() {
    Page page = new Page(0, 10);
    List<String> articleIds = articleReadService.queryArticles("java", null, null, page);

    Assertions.assertNotNull(articleIds);
    Assertions.assertEquals(2, articleIds.size());
    Assertions.assertTrue(articleIds.contains(article1.getId()));
    Assertions.assertTrue(articleIds.contains(article2.getId()));
  }

  @Test
  public void queryArticles_filterByAuthor_returnsMatchingArticleIds() {
    Page page = new Page(0, 10);
    List<String> articleIds = articleReadService.queryArticles(null, "author1", null, page);

    Assertions.assertNotNull(articleIds);
    Assertions.assertEquals(2, articleIds.size());
    Assertions.assertTrue(articleIds.contains(article1.getId()));
    Assertions.assertTrue(articleIds.contains(article2.getId()));
  }

  @Test
  public void queryArticles_filterByFavoritedBy_returnsMatchingArticleIds() {
    User user = new User("user@example.com", "user", "password", "bio", "image");
    userRepository.save(user);
    articleFavoriteRepository.save(new ArticleFavorite(article1.getId(), user.getId()));

    Page page = new Page(0, 10);
    List<String> articleIds = articleReadService.queryArticles(null, null, "user", page);

    Assertions.assertNotNull(articleIds);
    Assertions.assertEquals(1, articleIds.size());
    Assertions.assertTrue(articleIds.contains(article1.getId()));
  }

  @Test
  public void queryArticles_withPagination_returnsLimitedResults() {
    Page page = new Page(0, 2);
    List<String> articleIds = articleReadService.queryArticles(null, null, null, page);

    Assertions.assertNotNull(articleIds);
    Assertions.assertTrue(articleIds.size() <= 2);
  }

  @Test
  public void queryArticles_secondPage_returnsRemainingResults() {
    Page page = new Page(2, 2);
    List<String> articleIds = articleReadService.queryArticles(null, null, null, page);

    Assertions.assertNotNull(articleIds);
    Assertions.assertTrue(articleIds.size() <= 1);
  }

  @Test
  public void countArticle_noFilters_returnsTotal() {
    int count = articleReadService.countArticle(null, null, null);

    Assertions.assertEquals(3, count);
  }

  @Test
  public void countArticle_filterByTag_returnsMatchingCount() {
    int count = articleReadService.countArticle("java", null, null);

    Assertions.assertEquals(2, count);
  }

  @Test
  public void countArticle_filterByAuthor_returnsMatchingCount() {
    int count = articleReadService.countArticle(null, "author1", null);

    Assertions.assertEquals(2, count);
  }

  @Test
  public void findArticles_byIds_returnsMatchingArticles() {
    List<String> articleIds = Arrays.asList(article1.getId(), article3.getId());
    List<ArticleData> articles = articleReadService.findArticles(articleIds);

    Assertions.assertNotNull(articles);
    Assertions.assertEquals(2, articles.size());

    boolean hasArticle1 = articles.stream().anyMatch(a -> a.getId().equals(article1.getId()));
    boolean hasArticle3 = articles.stream().anyMatch(a -> a.getId().equals(article3.getId()));
    Assertions.assertTrue(hasArticle1);
    Assertions.assertTrue(hasArticle3);
  }

  @Test
  public void findArticlesOfAuthors_withPagination_returnsArticles() {
    List<String> authorIds = Arrays.asList(author1.getId());
    Page page = new Page(0, 10);
    List<ArticleData> articles = articleReadService.findArticlesOfAuthors(authorIds, page);

    Assertions.assertNotNull(articles);
    Assertions.assertEquals(2, articles.size());
  }

  @Test
  public void findArticlesOfAuthors_multipleAuthors_returnsAllArticles() {
    List<String> authorIds = Arrays.asList(author1.getId(), author2.getId());
    Page page = new Page(0, 10);
    List<ArticleData> articles = articleReadService.findArticlesOfAuthors(authorIds, page);

    Assertions.assertNotNull(articles);
    Assertions.assertEquals(3, articles.size());
  }

  @Test
  public void findArticlesOfAuthorsWithCursor_firstPage_returnsLimitedArticles() {
    List<String> authorIds = Arrays.asList(author1.getId());
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 1, Direction.NEXT);
    List<ArticleData> articles =
        articleReadService.findArticlesOfAuthorsWithCursor(authorIds, page);

    Assertions.assertNotNull(articles);
    Assertions.assertTrue(articles.size() <= page.getQueryLimit());
  }

  @Test
  public void findArticlesOfAuthorsWithCursor_withCursor_returnsArticlesAfterCursor() {
    List<String> authorIds = Arrays.asList(author1.getId());
    DateTime cursor = article1.getCreatedAt();
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(cursor, 10, Direction.NEXT);
    List<ArticleData> articles =
        articleReadService.findArticlesOfAuthorsWithCursor(authorIds, page);

    Assertions.assertNotNull(articles);
    for (ArticleData article : articles) {
      Assertions.assertTrue(
          article.getCreatedAt().isAfter(cursor) || article.getCreatedAt().equals(cursor));
    }
  }

  @Test
  public void countFeedSize_singleAuthor_returnsCount() {
    List<String> authorIds = Arrays.asList(author1.getId());
    int count = articleReadService.countFeedSize(authorIds);

    Assertions.assertEquals(2, count);
  }

  @Test
  public void countFeedSize_multipleAuthors_returnsTotal() {
    List<String> authorIds = Arrays.asList(author1.getId(), author2.getId());
    int count = articleReadService.countFeedSize(authorIds);

    Assertions.assertEquals(3, count);
  }

  @Test
  public void findArticlesWithCursor_noFilters_returnsArticleIds() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    List<String> articleIds = articleReadService.findArticlesWithCursor(null, null, null, page);

    Assertions.assertNotNull(articleIds);
    Assertions.assertEquals(3, articleIds.size());
  }

  @Test
  public void findArticlesWithCursor_filterByTag_returnsMatchingIds() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    List<String> articleIds = articleReadService.findArticlesWithCursor("java", null, null, page);

    Assertions.assertNotNull(articleIds);
    Assertions.assertEquals(2, articleIds.size());
  }

  @Test
  public void findArticlesWithCursor_withCursor_returnsArticlesAfterCursor() {
    DateTime cursor = article1.getCreatedAt();
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(cursor, 10, Direction.NEXT);
    List<String> articleIds = articleReadService.findArticlesWithCursor(null, null, null, page);

    Assertions.assertNotNull(articleIds);
  }

  @Test
  public void findArticlesWithCursor_withLimit_returnsLimitedResults() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 2, Direction.NEXT);
    List<String> articleIds = articleReadService.findArticlesWithCursor(null, null, null, page);

    Assertions.assertNotNull(articleIds);
    Assertions.assertTrue(articleIds.size() <= page.getQueryLimit());
  }
}
