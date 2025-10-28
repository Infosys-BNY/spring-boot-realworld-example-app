package io.spring.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.NewArticleParam;
import io.spring.application.article.UpdateArticleParam;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ArticlePayload;
import io.spring.graphql.types.CreateArticleInput;
import io.spring.graphql.types.DeletionStatus;
import io.spring.graphql.types.UpdateArticleInput;
import java.util.Arrays;
import java.util.Optional;
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
class ArticleMutationTest {
  @Mock private ArticleCommandService articleCommandService;
  @Mock private ArticleRepository articleRepository;
  @Mock private ArticleFavoriteRepository articleFavoriteRepository;
  @InjectMocks private ArticleMutation articleMutation;

  private MockedStatic<SecurityUtil> securityUtilMock;
  private User currentUser;
  private User otherUser;
  private Article article;

  @BeforeEach
  void setUp() {
    securityUtilMock = Mockito.mockStatic(SecurityUtil.class);
    currentUser = new User("test@example.com", "testuser", "password", "bio", "image");
    otherUser = new User("other@example.com", "otheruser", "password", "bio", "image");
    article =
        new Article("Test Title", "Test desc", "Test body", Arrays.asList("java"), currentUser.getId());
  }

  @AfterEach
  void tearDown() {
    securityUtilMock.close();
  }

  @Test
  void createArticle_validInput_returnsArticlePayload() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Article")
            .description("Test description")
            .body("Test body")
            .tagList(Arrays.asList("java", "spring"))
            .build();
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(articleCommandService.createArticle(any(NewArticleParam.class), eq(currentUser)))
        .thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertThat(result.getData()).isNotNull();
    assertThat(result.getLocalContext()).isEqualTo(article);
    verify(articleCommandService).createArticle(any(NewArticleParam.class), eq(currentUser));
  }

  @Test
  void createArticle_nullTagList_createsArticleWithEmptyTags() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Article")
            .description("Test description")
            .body("Test body")
            .tagList(null)
            .build();
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(articleCommandService.createArticle(any(NewArticleParam.class), eq(currentUser)))
        .thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertThat(result.getData()).isNotNull();
    verify(articleCommandService).createArticle(any(NewArticleParam.class), eq(currentUser));
  }

  @Test
  void createArticle_noAuthentication_throwsAuthenticationException() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Article")
            .description("Test description")
            .body("Test body")
            .build();
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

    assertThatThrownBy(() -> articleMutation.createArticle(input))
        .isInstanceOf(AuthenticationException.class);
  }

  @Test
  void updateArticle_validInput_returnsArticlePayload() {
    UpdateArticleInput input =
        UpdateArticleInput.newBuilder()
            .title("Updated Title")
            .body("Updated body")
            .description("Updated desc")
            .build();
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(eq(article), any(UpdateArticleParam.class)))
        .thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.updateArticle("test-slug", input);

    assertThat(result.getData()).isNotNull();
    assertThat(result.getLocalContext()).isEqualTo(article);
    verify(articleCommandService).updateArticle(eq(article), any(UpdateArticleParam.class));
  }

  @Test
  void updateArticle_articleNotFound_throwsResourceNotFoundException() {
    UpdateArticleInput input =
        UpdateArticleInput.newBuilder()
            .title("Updated Title")
            .body("Updated body")
            .description("Updated desc")
            .build();
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> articleMutation.updateArticle("nonexistent", input))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void updateArticle_unauthorizedUser_throwsNoAuthorizationException() {
    UpdateArticleInput input =
        UpdateArticleInput.newBuilder()
            .title("Updated Title")
            .body("Updated body")
            .description("Updated desc")
            .build();
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(otherUser));
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));

    assertThatThrownBy(() -> articleMutation.updateArticle("test-slug", input))
        .isInstanceOf(NoAuthorizationException.class);
  }

  @Test
  void updateArticle_noAuthentication_throwsAuthenticationException() {
    UpdateArticleInput input =
        UpdateArticleInput.newBuilder().title("Updated Title").build();
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

    assertThatThrownBy(() -> articleMutation.updateArticle("test-slug", input))
        .isInstanceOf(AuthenticationException.class);
  }

  @Test
  void favoriteArticle_validSlug_returnsArticlePayload() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle("test-slug");

    assertThat(result.getData()).isNotNull();
    assertThat(result.getLocalContext()).isEqualTo(article);
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  void favoriteArticle_articleNotFound_throwsResourceNotFoundException() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> articleMutation.favoriteArticle("nonexistent"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void favoriteArticle_noAuthentication_throwsAuthenticationException() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

    assertThatThrownBy(() -> articleMutation.favoriteArticle("test-slug"))
        .isInstanceOf(AuthenticationException.class);
  }

  @Test
  void unfavoriteArticle_validSlug_returnsArticlePayload() {
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), currentUser.getId());
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), currentUser.getId()))
        .thenReturn(Optional.of(favorite));

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("test-slug");

    assertThat(result.getData()).isNotNull();
    assertThat(result.getLocalContext()).isEqualTo(article);
    verify(articleFavoriteRepository).remove(favorite);
  }

  @Test
  void unfavoriteArticle_notFavorited_returnsArticlePayload() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), currentUser.getId()))
        .thenReturn(Optional.empty());

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("test-slug");

    assertThat(result.getData()).isNotNull();
    assertThat(result.getLocalContext()).isEqualTo(article);
  }

  @Test
  void unfavoriteArticle_noAuthentication_throwsAuthenticationException() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

    assertThatThrownBy(() -> articleMutation.unfavoriteArticle("test-slug"))
        .isInstanceOf(AuthenticationException.class);
  }

  @Test
  void deleteArticle_validSlug_returnsDeletionStatus() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle("test-slug");

    assertThat(result).isNotNull();
    assertThat(result.getSuccess()).isTrue();
    verify(articleRepository).remove(article);
  }

  @Test
  void deleteArticle_articleNotFound_throwsResourceNotFoundException() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> articleMutation.deleteArticle("nonexistent"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void deleteArticle_unauthorizedUser_throwsNoAuthorizationException() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(otherUser));
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));

    assertThatThrownBy(() -> articleMutation.deleteArticle("test-slug"))
        .isInstanceOf(NoAuthorizationException.class);
  }

  @Test
  void deleteArticle_noAuthentication_throwsAuthenticationException() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

    assertThatThrownBy(() -> articleMutation.deleteArticle("test-slug"))
        .isInstanceOf(AuthenticationException.class);
  }
}
