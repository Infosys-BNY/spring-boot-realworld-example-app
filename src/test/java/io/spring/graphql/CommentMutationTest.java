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
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.User;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.CommentPayload;
import io.spring.graphql.types.DeletionStatus;
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
class CommentMutationTest {
  @Mock private ArticleRepository articleRepository;
  @Mock private CommentRepository commentRepository;
  @Mock private CommentQueryService commentQueryService;
  @InjectMocks private CommentMutation commentMutation;

  private MockedStatic<SecurityUtil> securityUtilMock;
  private MockedStatic<AuthorizationService> authorizationServiceMock;
  private User currentUser;
  private User otherUser;
  private Article article;
  private Comment comment;
  private CommentData commentData;

  @BeforeEach
  void setUp() {
    securityUtilMock = Mockito.mockStatic(SecurityUtil.class);
    authorizationServiceMock = Mockito.mockStatic(AuthorizationService.class);
    currentUser = new User("test@example.com", "testuser", "password", "bio", "image");
    otherUser = new User("other@example.com", "otheruser", "password", "bio", "image");
    article =
        new Article(
            "Test Title", "Test desc", "Test body", Arrays.asList("java"), currentUser.getId());
    comment = new Comment("Test comment body", currentUser.getId(), article.getId());
    commentData =
        new CommentData(
            comment.getId(),
            comment.getBody(),
            comment.getArticleId(),
            comment.getCreatedAt(),
            null,
            null);
  }

  @AfterEach
  void tearDown() {
    securityUtilMock.close();
    authorizationServiceMock.close();
  }

  @Test
  void createComment_validInput_returnsCommentPayload() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
    when(commentQueryService.findById(any(), eq(currentUser))).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result =
        commentMutation.createComment("test-slug", "Test comment");

    assertThat(result.getData()).isNotNull();
    assertThat(result.getLocalContext()).isEqualTo(commentData);
    verify(commentRepository).save(any(Comment.class));
    verify(commentQueryService).findById(any(), eq(currentUser));
  }

  @Test
  void createComment_noAuthentication_throwsAuthenticationException() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

    assertThatThrownBy(() -> commentMutation.createComment("test-slug", "Test comment"))
        .isInstanceOf(AuthenticationException.class);
  }

  @Test
  void createComment_articleNotFound_throwsResourceNotFoundException() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> commentMutation.createComment("nonexistent", "Test comment"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void createComment_commentDataNotFound_throwsResourceNotFoundException() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
    when(commentQueryService.findById(any(), eq(currentUser))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> commentMutation.createComment("test-slug", "Test comment"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void createComment_emptyBody_savesEmptyComment() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
    when(commentQueryService.findById(any(), eq(currentUser))).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result = commentMutation.createComment("test-slug", "");

    assertThat(result.getData()).isNotNull();
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  void removeComment_validInput_returnsDeletionStatus() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), "comment-id"))
        .thenReturn(Optional.of(comment));
    authorizationServiceMock
        .when(() -> AuthorizationService.canWriteComment(currentUser, article, comment))
        .thenReturn(true);

    DeletionStatus result = commentMutation.removeComment("test-slug", "comment-id");

    assertThat(result).isNotNull();
    assertThat(result.getSuccess()).isTrue();
    verify(commentRepository).remove(comment);
  }

  @Test
  void removeComment_noAuthentication_throwsAuthenticationException() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

    assertThatThrownBy(() -> commentMutation.removeComment("test-slug", "comment-id"))
        .isInstanceOf(AuthenticationException.class);
  }

  @Test
  void removeComment_articleNotFound_throwsResourceNotFoundException() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> commentMutation.removeComment("nonexistent", "comment-id"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void removeComment_commentNotFound_throwsResourceNotFoundException() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), "nonexistent")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> commentMutation.removeComment("test-slug", "nonexistent"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void removeComment_unauthorizedUser_throwsNoAuthorizationException() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(otherUser));
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), "comment-id"))
        .thenReturn(Optional.of(comment));
    authorizationServiceMock
        .when(() -> AuthorizationService.canWriteComment(otherUser, article, comment))
        .thenReturn(false);

    assertThatThrownBy(() -> commentMutation.removeComment("test-slug", "comment-id"))
        .isInstanceOf(NoAuthorizationException.class);
  }
}
