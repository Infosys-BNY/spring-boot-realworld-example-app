package io.spring.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import io.spring.application.CommentQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.CommentsConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
class CommentDatafetcherTest {
  @Mock private CommentQueryService commentQueryService;
  @InjectMocks private CommentDatafetcher commentDatafetcher;

  @Mock private DgsDataFetchingEnvironment dgsDataFetchingEnvironment;

  private MockedStatic<SecurityUtil> securityUtilMock;
  private User currentUser;
  private CommentData commentData;
  private ArticleData articleData;
  private CursorPager<CommentData> commentPager;

  @BeforeEach
  void setUp() {
    securityUtilMock = Mockito.mockStatic(SecurityUtil.class);
    currentUser = new User("test@example.com", "testuser", "password", "bio", "image");
    DateTime now = DateTime.now();
    ProfileData authorProfile = new ProfileData("author-id", "author", "bio", "image", false);
    commentData =
        new CommentData(
            "comment-id", "Test comment body", "article-id", now, now, authorProfile);
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
            Arrays.asList("java"),
            authorProfile);
    commentPager = new CursorPager<>(Arrays.asList(commentData), Direction.NEXT, true);
  }

  @AfterEach
  void tearDown() {
    securityUtilMock.close();
  }

  @Test
  void getComment_validCommentData_returnsComment() {
    when(dgsDataFetchingEnvironment.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dgsDataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getId()).isEqualTo("comment-id");
    assertThat(result.getData().getBody()).isEqualTo("Test comment body");
    assertThat(result.getLocalContext()).isInstanceOf(Map.class);
    @SuppressWarnings("unchecked")
    Map<String, Object> localContext = (Map<String, Object>) result.getLocalContext();
    assertThat(localContext).containsKey("comment-id");
    assertThat(localContext.get("comment-id")).isEqualTo(commentData);
  }

  @Test
  void getComment_withCreatedAtAndUpdatedAt_setsTimestampsCorrectly() {
    when(dgsDataFetchingEnvironment.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dgsDataFetchingEnvironment);

    assertThat(result.getData().getCreatedAt()).isNotNull();
    assertThat(result.getData().getUpdatedAt()).isNotNull();
  }

  @Test
  void articleComments_withFirst_returnsCommentsConnection() {
    Article article = Article.newBuilder().slug("test-slug").build();
    Map<String, ArticleData> articleMap = new HashMap<>();
    articleMap.put("test-slug", articleData);

    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(dgsDataFetchingEnvironment.getSource()).thenReturn(article);
    when(dgsDataFetchingEnvironment.getLocalContext()).thenReturn(articleMap);
    when(commentQueryService.findByArticleIdWithCursor(
            eq("article-id"), eq(currentUser), any(CursorPageParameter.class)))
        .thenReturn(commentPager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dgsDataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getEdges()).hasSize(1);
    assertThat(result.getData().getPageInfo()).isNotNull();
    assertThat(result.getData().getPageInfo().isHasNextPage()).isTrue();
  }

  @Test
  void articleComments_withLast_returnsCommentsConnection() {
    Article article = Article.newBuilder().slug("test-slug").build();
    Map<String, ArticleData> articleMap = new HashMap<>();
    articleMap.put("test-slug", articleData);
    CursorPager<CommentData> prevPager =
        new CursorPager<>(Arrays.asList(commentData), Direction.PREV, true);

    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(dgsDataFetchingEnvironment.getSource()).thenReturn(article);
    when(dgsDataFetchingEnvironment.getLocalContext()).thenReturn(articleMap);
    when(commentQueryService.findByArticleIdWithCursor(
            eq("article-id"), eq(currentUser), any(CursorPageParameter.class)))
        .thenReturn(prevPager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(null, null, 10, null, dgsDataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getEdges()).hasSize(1);
    assertThat(result.getData().getPageInfo().isHasPreviousPage()).isTrue();
  }

  @Test
  void articleComments_withAfterCursor_returnsCommentsConnection() {
    Article article = Article.newBuilder().slug("test-slug").build();
    Map<String, ArticleData> articleMap = new HashMap<>();
    articleMap.put("test-slug", articleData);

    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(dgsDataFetchingEnvironment.getSource()).thenReturn(article);
    when(dgsDataFetchingEnvironment.getLocalContext()).thenReturn(articleMap);
    when(commentQueryService.findByArticleIdWithCursor(
            eq("article-id"), eq(currentUser), any(CursorPageParameter.class)))
        .thenReturn(commentPager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(
            10, "1672531200000", null, null, dgsDataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getEdges()).hasSize(1);
  }

  @Test
  void articleComments_withBeforeCursor_returnsCommentsConnection() {
    Article article = Article.newBuilder().slug("test-slug").build();
    Map<String, ArticleData> articleMap = new HashMap<>();
    articleMap.put("test-slug", articleData);
    CursorPager<CommentData> prevPager =
        new CursorPager<>(Arrays.asList(commentData), Direction.PREV, true);

    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(dgsDataFetchingEnvironment.getSource()).thenReturn(article);
    when(dgsDataFetchingEnvironment.getLocalContext()).thenReturn(articleMap);
    when(commentQueryService.findByArticleIdWithCursor(
            eq("article-id"), eq(currentUser), any(CursorPageParameter.class)))
        .thenReturn(prevPager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(
            null, null, 10, "1672531200000", dgsDataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getEdges()).hasSize(1);
  }

  @Test
  void articleComments_neitherFirstNorLast_throwsIllegalArgumentException() {
    assertThatThrownBy(
            () ->
                commentDatafetcher.articleComments(
                    null, null, null, null, dgsDataFetchingEnvironment))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void articleComments_noAuthentication_returnsCommentsConnection() {
    Article article = Article.newBuilder().slug("test-slug").build();
    Map<String, ArticleData> articleMap = new HashMap<>();
    articleMap.put("test-slug", articleData);

    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
    when(dgsDataFetchingEnvironment.getSource()).thenReturn(article);
    when(dgsDataFetchingEnvironment.getLocalContext()).thenReturn(articleMap);
    when(commentQueryService.findByArticleIdWithCursor(
            eq("article-id"), isNull(), any(CursorPageParameter.class)))
        .thenReturn(commentPager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dgsDataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getEdges()).hasSize(1);
  }

  @Test
  void articleComments_emptyResults_returnsEmptyConnection() {
    Article article = Article.newBuilder().slug("test-slug").build();
    Map<String, ArticleData> articleMap = new HashMap<>();
    articleMap.put("test-slug", articleData);
    CursorPager<CommentData> emptyPager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);

    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(dgsDataFetchingEnvironment.getSource()).thenReturn(article);
    when(dgsDataFetchingEnvironment.getLocalContext()).thenReturn(articleMap);
    when(commentQueryService.findByArticleIdWithCursor(
            eq("article-id"), eq(currentUser), any(CursorPageParameter.class)))
        .thenReturn(emptyPager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dgsDataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getEdges()).isEmpty();
    assertThat(result.getData().getPageInfo().isHasNextPage()).isFalse();
  }

  @Test
  void articleComments_multipleComments_returnsAllComments() {
    DateTime now = DateTime.now();
    ProfileData authorProfile = new ProfileData("author-id", "author", "bio", "image", false);
    CommentData comment1 =
        new CommentData("comment-1", "Comment 1", "article-id", now, now, authorProfile);
    CommentData comment2 =
        new CommentData("comment-2", "Comment 2", "article-id", now, now, authorProfile);
    CommentData comment3 =
        new CommentData("comment-3", "Comment 3", "article-id", now, now, authorProfile);
    CursorPager<CommentData> multiPager =
        new CursorPager<>(Arrays.asList(comment1, comment2, comment3), Direction.NEXT, false);

    Article article = Article.newBuilder().slug("test-slug").build();
    Map<String, ArticleData> articleMap = new HashMap<>();
    articleMap.put("test-slug", articleData);

    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(dgsDataFetchingEnvironment.getSource()).thenReturn(article);
    when(dgsDataFetchingEnvironment.getLocalContext()).thenReturn(articleMap);
    when(commentQueryService.findByArticleIdWithCursor(
            eq("article-id"), eq(currentUser), any(CursorPageParameter.class)))
        .thenReturn(multiPager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dgsDataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getEdges()).hasSize(3);
    assertThat(result.getLocalContext()).isInstanceOf(Map.class);
    @SuppressWarnings("unchecked")
    Map<String, CommentData> localContext = (Map<String, CommentData>) result.getLocalContext();
    assertThat(localContext).hasSize(3);
    assertThat(localContext).containsKeys("comment-1", "comment-2", "comment-3");
  }

  @Test
  void articleComments_withFirstAndAfter_usesNextDirection() {
    Article article = Article.newBuilder().slug("test-slug").build();
    Map<String, ArticleData> articleMap = new HashMap<>();
    articleMap.put("test-slug", articleData);

    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(dgsDataFetchingEnvironment.getSource()).thenReturn(article);
    when(dgsDataFetchingEnvironment.getLocalContext()).thenReturn(articleMap);
    when(commentQueryService.findByArticleIdWithCursor(
            eq("article-id"), eq(currentUser), any(CursorPageParameter.class)))
        .thenReturn(commentPager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(
            5, "1672531200000", null, null, dgsDataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getData()).isNotNull();
  }

  @Test
  void articleComments_withLastAndBefore_usesPrevDirection() {
    Article article = Article.newBuilder().slug("test-slug").build();
    Map<String, ArticleData> articleMap = new HashMap<>();
    articleMap.put("test-slug", articleData);
    CursorPager<CommentData> prevPager =
        new CursorPager<>(Arrays.asList(commentData), Direction.PREV, true);

    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(dgsDataFetchingEnvironment.getSource()).thenReturn(article);
    when(dgsDataFetchingEnvironment.getLocalContext()).thenReturn(articleMap);
    when(commentQueryService.findByArticleIdWithCursor(
            eq("article-id"), eq(currentUser), any(CursorPageParameter.class)))
        .thenReturn(prevPager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(
            null, null, 5, "1672531200000", dgsDataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getData()).isNotNull();
  }
}
