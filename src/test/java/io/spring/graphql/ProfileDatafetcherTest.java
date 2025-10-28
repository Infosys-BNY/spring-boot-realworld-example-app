package io.spring.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.Profile;
import io.spring.graphql.types.ProfilePayload;
import java.util.Arrays;
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
class ProfileDatafetcherTest {
  @Mock private ProfileQueryService profileQueryService;
  @InjectMocks private ProfileDatafetcher profileDatafetcher;

  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  private MockedStatic<SecurityUtil> securityUtilMock;
  private User currentUser;
  private User targetUser;
  private ProfileData profileData;

  @BeforeEach
  void setUp() {
    securityUtilMock = Mockito.mockStatic(SecurityUtil.class);
    currentUser = new User("current@example.com", "currentuser", "password", "bio", "image");
    targetUser = new User("target@example.com", "targetuser", "password", "target bio", "target.png");
    profileData = new ProfileData(
        targetUser.getId(),
        targetUser.getUsername(),
        targetUser.getBio(),
        targetUser.getImage(),
        false);
  }

  @AfterEach
  void tearDown() {
    securityUtilMock.close();
  }

  @Test
  void getUserProfile_validUser_returnsProfile() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(targetUser);
    when(profileQueryService.findByUsername(eq("targetuser"), eq(currentUser)))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getUserProfile(dataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo("targetuser");
    assertThat(result.getBio()).isEqualTo("target bio");
    assertThat(result.getImage()).isEqualTo("target.png");
    assertThat(result.getFollowing()).isFalse();
  }

  @Test
  void getUserProfile_noAuthentication_returnsProfile() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(targetUser);
    when(profileQueryService.findByUsername(eq("targetuser"), isNull()))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getUserProfile(dataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo("targetuser");
  }

  @Test
  void getUserProfile_profileNotFound_throwsResourceNotFoundException() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(targetUser);
    when(profileQueryService.findByUsername(eq("targetuser"), eq(currentUser)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> profileDatafetcher.getUserProfile(dataFetchingEnvironment))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void getUserProfile_followingProfile_returnsProfileWithFollowingTrue() {
    ProfileData followingProfileData = new ProfileData(
        targetUser.getId(),
        targetUser.getUsername(),
        targetUser.getBio(),
        targetUser.getImage(),
        true);
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(targetUser);
    when(profileQueryService.findByUsername(eq("targetuser"), eq(currentUser)))
        .thenReturn(Optional.of(followingProfileData));

    Profile result = profileDatafetcher.getUserProfile(dataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getFollowing()).isTrue();
  }

  @Test
  void getAuthor_validArticle_returnsProfile() {
    DateTime now = DateTime.now();
    ProfileData authorProfileData = new ProfileData(
        "author-id", "authoruser", "author bio", "author.png", false);
    ArticleData articleData = new ArticleData(
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
        authorProfileData);
    Map<String, ArticleData> articleMap = new HashMap<>();
    articleMap.put("test-slug", articleData);
    Article article = Article.newBuilder().slug("test-slug").build();

    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(articleMap);
    when(dataFetchingEnvironment.getSource()).thenReturn(article);
    when(profileQueryService.findByUsername(eq("authoruser"), eq(currentUser)))
        .thenReturn(Optional.of(authorProfileData));

    Profile result = profileDatafetcher.getAuthor(dataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo("authoruser");
    assertThat(result.getBio()).isEqualTo("author bio");
  }

  @Test
  void getAuthor_noAuthentication_returnsProfile() {
    DateTime now = DateTime.now();
    ProfileData authorProfileData = new ProfileData(
        "author-id", "authoruser", "author bio", "author.png", false);
    ArticleData articleData = new ArticleData(
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
        authorProfileData);
    Map<String, ArticleData> articleMap = new HashMap<>();
    articleMap.put("test-slug", articleData);
    Article article = Article.newBuilder().slug("test-slug").build();

    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(articleMap);
    when(dataFetchingEnvironment.getSource()).thenReturn(article);
    when(profileQueryService.findByUsername(eq("authoruser"), isNull()))
        .thenReturn(Optional.of(authorProfileData));

    Profile result = profileDatafetcher.getAuthor(dataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo("authoruser");
  }

  @Test
  void getAuthor_profileNotFound_throwsResourceNotFoundException() {
    DateTime now = DateTime.now();
    ProfileData authorProfileData = new ProfileData(
        "author-id", "authoruser", "author bio", "author.png", false);
    ArticleData articleData = new ArticleData(
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
        authorProfileData);
    Map<String, ArticleData> articleMap = new HashMap<>();
    articleMap.put("test-slug", articleData);
    Article article = Article.newBuilder().slug("test-slug").build();

    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(articleMap);
    when(dataFetchingEnvironment.getSource()).thenReturn(article);
    when(profileQueryService.findByUsername(eq("authoruser"), eq(currentUser)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> profileDatafetcher.getAuthor(dataFetchingEnvironment))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void getCommentAuthor_validComment_returnsProfile() {
    ProfileData commentAuthorProfileData = new ProfileData(
        "comment-author-id", "commentauthor", "comment author bio", "commentauthor.png", false);
    CommentData commentData = new CommentData(
        "comment-id",
        "comment body",
        "article-id",
        DateTime.now(),
        null,
        commentAuthorProfileData);
    Map<String, CommentData> commentMap = new HashMap<>();
    commentMap.put("comment-id", commentData);
    Comment comment = Comment.newBuilder().id("comment-id").build();

    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(commentMap);
    when(dataFetchingEnvironment.getSource()).thenReturn(comment);
    when(profileQueryService.findByUsername(eq("commentauthor"), eq(currentUser)))
        .thenReturn(Optional.of(commentAuthorProfileData));

    Profile result = profileDatafetcher.getCommentAuthor(dataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo("commentauthor");
    assertThat(result.getBio()).isEqualTo("comment author bio");
  }

  @Test
  void getCommentAuthor_noAuthentication_returnsProfile() {
    ProfileData commentAuthorProfileData = new ProfileData(
        "comment-author-id", "commentauthor", "comment author bio", "commentauthor.png", false);
    CommentData commentData = new CommentData(
        "comment-id",
        "comment body",
        "article-id",
        DateTime.now(),
        null,
        commentAuthorProfileData);
    Map<String, CommentData> commentMap = new HashMap<>();
    commentMap.put("comment-id", commentData);
    Comment comment = Comment.newBuilder().id("comment-id").build();

    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(commentMap);
    when(dataFetchingEnvironment.getSource()).thenReturn(comment);
    when(profileQueryService.findByUsername(eq("commentauthor"), isNull()))
        .thenReturn(Optional.of(commentAuthorProfileData));

    Profile result = profileDatafetcher.getCommentAuthor(dataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo("commentauthor");
  }

  @Test
  void getCommentAuthor_profileNotFound_throwsResourceNotFoundException() {
    ProfileData commentAuthorProfileData = new ProfileData(
        "comment-author-id", "commentauthor", "comment author bio", "commentauthor.png", false);
    CommentData commentData = new CommentData(
        "comment-id",
        "comment body",
        "article-id",
        DateTime.now(),
        null,
        commentAuthorProfileData);
    Map<String, CommentData> commentMap = new HashMap<>();
    commentMap.put("comment-id", commentData);
    Comment comment = Comment.newBuilder().id("comment-id").build();

    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(commentMap);
    when(dataFetchingEnvironment.getSource()).thenReturn(comment);
    when(profileQueryService.findByUsername(eq("commentauthor"), eq(currentUser)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> profileDatafetcher.getCommentAuthor(dataFetchingEnvironment))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void queryProfile_validUsername_returnsProfilePayload() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(dataFetchingEnvironment.getArgument("username")).thenReturn("targetuser");
    when(profileQueryService.findByUsername(eq("targetuser"), eq(currentUser)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = profileDatafetcher.queryProfile("targetuser", dataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getProfile()).isNotNull();
    assertThat(result.getProfile().getUsername()).isEqualTo("targetuser");
  }

  @Test
  void queryProfile_noAuthentication_returnsProfilePayload() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
    when(dataFetchingEnvironment.getArgument("username")).thenReturn("targetuser");
    when(profileQueryService.findByUsername(eq("targetuser"), isNull()))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = profileDatafetcher.queryProfile("targetuser", dataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getProfile()).isNotNull();
    assertThat(result.getProfile().getUsername()).isEqualTo("targetuser");
  }

  @Test
  void queryProfile_profileNotFound_throwsResourceNotFoundException() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(dataFetchingEnvironment.getArgument("username")).thenReturn("nonexistent");
    when(profileQueryService.findByUsername(eq("nonexistent"), eq(currentUser)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> profileDatafetcher.queryProfile("nonexistent", dataFetchingEnvironment))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
