package io.spring.core.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class AuthorizationServiceTest {

  @Test
  public void canWriteArticle_userIsAuthor_returnsTrue() {
    User user = new User("john@example.com", "john", "password", "bio", "image");
    Article article = new Article("Title", "desc", "body", Arrays.asList("java"), user.getId());

    boolean canWrite = AuthorizationService.canWriteArticle(user, article);

    assertThat(canWrite, is(true));
  }

  @Test
  public void canWriteArticle_userIsNotAuthor_returnsFalse() {
    User user = new User("john@example.com", "john", "password", "bio", "image");
    User otherUser = new User("jane@example.com", "jane", "password", "bio", "image");
    Article article =
        new Article("Title", "desc", "body", Arrays.asList("java"), otherUser.getId());

    boolean canWrite = AuthorizationService.canWriteArticle(user, article);

    assertThat(canWrite, is(false));
  }

  @Test
  public void canWriteComment_userIsArticleAuthor_returnsTrue() {
    User user = new User("john@example.com", "john", "password", "bio", "image");
    User commenter = new User("jane@example.com", "jane", "password", "bio", "image");
    Article article = new Article("Title", "desc", "body", Arrays.asList("java"), user.getId());
    Comment comment = new Comment("Great article!", commenter.getId(), article.getId());

    boolean canWrite = AuthorizationService.canWriteComment(user, article, comment);

    assertThat(canWrite, is(true));
  }

  @Test
  public void canWriteComment_userIsCommentAuthor_returnsTrue() {
    User user = new User("john@example.com", "john", "password", "bio", "image");
    User articleAuthor = new User("jane@example.com", "jane", "password", "bio", "image");
    Article article =
        new Article("Title", "desc", "body", Arrays.asList("java"), articleAuthor.getId());
    Comment comment = new Comment("Great article!", user.getId(), article.getId());

    boolean canWrite = AuthorizationService.canWriteComment(user, article, comment);

    assertThat(canWrite, is(true));
  }

  @Test
  public void canWriteComment_userIsNeitherAuthor_returnsFalse() {
    User user = new User("john@example.com", "john", "password", "bio", "image");
    User articleAuthor = new User("jane@example.com", "jane", "password", "bio", "image");
    User commenter = new User("bob@example.com", "bob", "password", "bio", "image");
    Article article =
        new Article("Title", "desc", "body", Arrays.asList("java"), articleAuthor.getId());
    Comment comment = new Comment("Great article!", commenter.getId(), article.getId());

    boolean canWrite = AuthorizationService.canWriteComment(user, article, comment);

    assertThat(canWrite, is(false));
  }

  @Test
  public void canWriteComment_userIsBothAuthor_returnsTrue() {
    User user = new User("john@example.com", "john", "password", "bio", "image");
    Article article = new Article("Title", "desc", "body", Arrays.asList("java"), user.getId());
    Comment comment = new Comment("Great article!", user.getId(), article.getId());

    boolean canWrite = AuthorizationService.canWriteComment(user, article, comment);

    assertThat(canWrite, is(true));
  }
}
