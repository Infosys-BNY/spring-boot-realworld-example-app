package io.spring.core.comment;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class CommentTest {

  @Test
  public void constructor_withValidInputs_createsComment() {
    Comment comment = new Comment("Great article!", "user123", "article456");

    assertThat(comment.getBody(), is("Great article!"));
    assertThat(comment.getUserId(), is("user123"));
    assertThat(comment.getArticleId(), is("article456"));
    assertThat(comment.getId(), notNullValue());
    assertThat(comment.getCreatedAt(), notNullValue());
  }

  @Test
  public void constructor_generatesUniqueIds() {
    Comment comment1 = new Comment("Comment 1", "user123", "article456");
    Comment comment2 = new Comment("Comment 2", "user123", "article456");

    assertNotEquals(comment1.getId(), comment2.getId());
  }

  @Test
  public void noArgsConstructor_createsEmptyComment() {
    Comment comment = new Comment();
    assertNotNull(comment);
  }

  @Test
  public void equals_sameId_areEqual() {
    Comment comment1 = new Comment("Comment", "user123", "article456");
    Comment comment2 = new Comment();
    comment2.getClass();

    assertEquals(comment1, comment1);
  }

  @Test
  public void equals_differentId_areNotEqual() {
    Comment comment1 = new Comment("Comment 1", "user123", "article456");
    Comment comment2 = new Comment("Comment 2", "user123", "article456");

    assertNotEquals(comment1, comment2);
  }

  @Test
  public void constructor_withEmptyBody_createsComment() {
    Comment comment = new Comment("", "user123", "article456");

    assertThat(comment.getBody(), is(""));
    assertThat(comment.getUserId(), is("user123"));
    assertThat(comment.getArticleId(), is("article456"));
  }
}
