package io.spring.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.DateTimeCursor;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.ArticlesConnection;
import io.spring.graphql.types.Profile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArticleDatafetcherTest {
  @Mock private ArticleQueryService articleQueryService;
  @Mock private UserRepository userRepository;
  @InjectMocks private ArticleDatafetcher articleDatafetcher;

  @Mock private DgsDataFetchingEnvironment dgsDataFetchingEnvironment;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  private MockedStatic<SecurityUtil> securityUtilMock;
  private User currentUser;
  private ArticleData articleData;
  private CursorPager<ArticleData> articlePager;

  @BeforeEach
  void setUp() {
    securityUtilMock = Mockito.mockStatic(SecurityUtil.class);
    currentUser = new User("test@example.com", "testuser", "password", "bio", "image");
    DateTime now = DateTime.now();
    articleData =
        new ArticleData(
            "article-id",
            "test-slug",
            "Test Title",
            "Test Description",
            "Test Body",
            false,
            0,
            now,
            now,
            Arrays.asList("java", "spring"),
            new ProfileData("author-id", "author", "author bio", "author.png", false));
    articlePager =
        new CursorPager<>(
            Arrays.asList(articleData),
            Direction.NEXT,
            true);
  }

  @AfterEach
  void tearDown() {
    securityUtilMock.close();
  }

  @Test
  void getFeed_withFirst_returnsArticlesConnection() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(articleQueryService.findUserFeedWithCursor(
            eq(currentUser), any(CursorPageParameter.class)))
        .thenReturn(articlePager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dgsDataFetchingEnvironment);

    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getEdges()).hasSize(1);
    assertThat(result.getData().getPageInfo()).isNotNull();
  }

  @Test
  void getFeed_withLast_returnsArticlesConnection() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(articleQueryService.findUserFeedWithCursor(
            eq(currentUser), any(CursorPageParameter.class)))
        .thenReturn(articlePager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 10, null, dgsDataFetchingEnvironment);

    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getEdges()).hasSize(1);
  }

  @Test
  void getFeed_withAfterCursor_returnsArticlesConnection() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    String cursor = new DateTimeCursor(DateTime.now()).toString();
    when(articleQueryService.findUserFeedWithCursor(
            eq(currentUser), any(CursorPageParameter.class)))
        .thenReturn(articlePager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, cursor, null, null, dgsDataFetchingEnvironment);

    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getEdges()).hasSize(1);
  }

  @Test
  void getFeed_withBeforeCursor_returnsArticlesConnection() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    String cursor = new DateTimeCursor(DateTime.now()).toString();
    when(articleQueryService.findUserFeedWithCursor(
            eq(currentUser), any(CursorPageParameter.class)))
        .thenReturn(articlePager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 10, cursor, dgsDataFetchingEnvironment);

    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getEdges()).hasSize(1);
  }

  @Test
  void getFeed_noAuthentication_returnsArticlesConnection() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
    when(articleQueryService.findUserFeedWithCursor(isNull(), any(CursorPageParameter.class)))
        .thenReturn(articlePager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dgsDataFetchingEnvironment);

    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getEdges()).hasSize(1);
  }

  @Test
  void getFeed_neitherFirstNorLast_throwsIllegalArgumentException() {
    assertThatThrownBy(
            () -> articleDatafetcher.getFeed(null, null, null, null, dgsDataFetchingEnvironment))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void getFeed_emptyResults_returnsEmptyConnection() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    CursorPager<ArticleData> emptyPager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(
            eq(currentUser), any(CursorPageParameter.class)))
        .thenReturn(emptyPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dgsDataFetchingEnvironment);

    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getEdges()).isEmpty();
  }

  @Test
  void userFeed_withFirst_returnsArticlesConnection() {
    Profile profile = Profile.newBuilder().username("targetuser").build();
    User targetUser = new User("target@example.com", "targetuser", "password", "bio", "image");
    when(dgsDataFetchingEnvironment.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(targetUser));
    when(articleQueryService.findUserFeedWithCursor(eq(targetUser), any(CursorPageParameter.class)))
        .thenReturn(articlePager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(10, null, null, null, dgsDataFetchingEnvironment);

    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getEdges()).hasSize(1);
  }

  @Test
  void userFeed_userNotFound_throwsResourceNotFoundException() {
    Profile profile = Profile.newBuilder().username("nonexistent").build();
    when(dgsDataFetchingEnvironment.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                articleDatafetcher.userFeed(
                    10, null, null, null, dgsDataFetchingEnvironment))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void userFeed_neitherFirstNorLast_throwsIllegalArgumentException() {
    assertThatThrownBy(
            () ->
                articleDatafetcher.userFeed(
                    null, null, null, null, dgsDataFetchingEnvironment))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void userFavorites_withFirst_returnsArticlesConnection() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    Profile profile = Profile.newBuilder().username("targetuser").build();
    when(dgsDataFetchingEnvironment.getSource()).thenReturn(profile);
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), isNull(), eq("targetuser"), any(CursorPageParameter.class), eq(currentUser)))
        .thenReturn(articlePager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(10, null, null, null, dgsDataFetchingEnvironment);

    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getEdges()).hasSize(1);
  }

  @Test
  void userFavorites_withLast_returnsArticlesConnection() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    Profile profile = Profile.newBuilder().username("targetuser").build();
    when(dgsDataFetchingEnvironment.getSource()).thenReturn(profile);
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), isNull(), eq("targetuser"), any(CursorPageParameter.class), eq(currentUser)))
        .thenReturn(articlePager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(null, null, 10, null, dgsDataFetchingEnvironment);

    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getEdges()).hasSize(1);
  }

  @Test
  void userFavorites_noAuthentication_returnsArticlesConnection() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
    Profile profile = Profile.newBuilder().username("targetuser").build();
    when(dgsDataFetchingEnvironment.getSource()).thenReturn(profile);
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), isNull(), eq("targetuser"), any(CursorPageParameter.class), isNull()))
        .thenReturn(articlePager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(10, null, null, null, dgsDataFetchingEnvironment);

    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getEdges()).hasSize(1);
  }

  @Test
  void userArticles_withFirst_returnsArticlesConnection() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    Profile profile = Profile.newBuilder().username("targetuser").build();
    when(dgsDataFetchingEnvironment.getSource()).thenReturn(profile);
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), eq("targetuser"), isNull(), any(CursorPageParameter.class), eq(currentUser)))
        .thenReturn(articlePager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(10, null, null, null, dgsDataFetchingEnvironment);

    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getEdges()).hasSize(1);
  }

  @Test
  void userArticles_withLast_returnsArticlesConnection() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    Profile profile = Profile.newBuilder().username("targetuser").build();
    when(dgsDataFetchingEnvironment.getSource()).thenReturn(profile);
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), eq("targetuser"), isNull(), any(CursorPageParameter.class), eq(currentUser)))
        .thenReturn(articlePager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(null, null, 10, null, dgsDataFetchingEnvironment);

    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getEdges()).hasSize(1);
  }

  @Test
  void getArticles_withFirst_returnsArticlesConnection() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), isNull(), isNull(), any(CursorPageParameter.class), eq(currentUser)))
        .thenReturn(articlePager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(
            10, null, null, null, null, null, null, dgsDataFetchingEnvironment);

    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getEdges()).hasSize(1);
  }

  @Test
  void getArticles_withFilters_returnsArticlesConnection() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(articleQueryService.findRecentArticlesWithCursor(
            eq("java"), eq("author"), eq("favoriter"), any(CursorPageParameter.class), eq(currentUser)))
        .thenReturn(articlePager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(
            10, null, null, null, "author", "favoriter", "java", dgsDataFetchingEnvironment);

    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getEdges()).hasSize(1);
  }

  @Test
  void getArticles_neitherFirstNorLast_throwsIllegalArgumentException() {
    assertThatThrownBy(
            () ->
                articleDatafetcher.getArticles(
                    null, null, null, null, null, null, null, dgsDataFetchingEnvironment))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void getArticle_validArticle_returnsArticle() {
    Article article =
        new Article(
            "Test Title", "Test desc", "Test body", Arrays.asList("java"), currentUser.getId());
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(article);
    when(articleQueryService.findById(eq(article.getId()), eq(currentUser)))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.getArticle(dataFetchingEnvironment);

    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getSlug()).isEqualTo("test-slug");
  }

  @Test
  void getArticle_articleNotFound_throwsResourceNotFoundException() {
    Article article =
        new Article(
            "Test Title", "Test desc", "Test body", Arrays.asList("java"), currentUser.getId());
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(article);
    when(articleQueryService.findById(eq(article.getId()), eq(currentUser)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> articleDatafetcher.getArticle(dataFetchingEnvironment))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void getCommentArticle_validComment_returnsArticle() {
    CommentData commentData =
        new CommentData(
            "comment-id", "comment body", "article-id", DateTime.now(), null, null);
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(commentData);
    when(articleQueryService.findById(eq("article-id"), eq(currentUser)))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.getCommentArticle(dataFetchingEnvironment);

    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getSlug()).isEqualTo("test-slug");
  }

  @Test
  void getCommentArticle_articleNotFound_throwsResourceNotFoundException() {
    CommentData commentData =
        new CommentData(
            "comment-id", "comment body", "article-id", DateTime.now(), null, null);
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(commentData);
    when(articleQueryService.findById(eq("article-id"), eq(currentUser)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> articleDatafetcher.getCommentArticle(dataFetchingEnvironment))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void findArticleBySlug_validSlug_returnsArticle() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(articleQueryService.findBySlug(eq("test-slug"), eq(currentUser)))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.findArticleBySlug("test-slug");

    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getSlug()).isEqualTo("test-slug");
  }

  @Test
  void findArticleBySlug_articleNotFound_throwsResourceNotFoundException() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(articleQueryService.findBySlug(eq("nonexistent"), eq(currentUser)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> articleDatafetcher.findArticleBySlug("nonexistent"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void findArticleBySlug_noAuthentication_returnsArticle() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
    when(articleQueryService.findBySlug(eq("test-slug"), isNull()))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.findArticleBySlug("test-slug");

    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getSlug()).isEqualTo("test-slug");
  }
}
