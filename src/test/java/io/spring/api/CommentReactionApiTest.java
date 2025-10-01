package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentReaction;
import io.spring.core.comment.CommentReactionRepository;
import io.spring.core.comment.CommentRepository;
import io.spring.core.comment.ReactionType;
import io.spring.core.user.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CommentReactionApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class CommentReactionApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private CommentReactionRepository commentReactionRepository;
  @MockBean private CommentRepository commentRepository;
  @MockBean private CommentQueryService commentQueryService;

  private Comment comment;
  private CommentData commentData;

  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);

    User anotherUser = new User("other@test.com", "other", "123", "", "");
    comment = new Comment("test body", user.getId(), "article123");
    when(commentRepository.findById(any(), eq(comment.getId()))).thenReturn(Optional.of(comment));

    commentData =
        new CommentData(
            comment.getId(),
            comment.getBody(),
            comment.getArticleId(),
            comment.getCreatedAt(),
            comment.getCreatedAt(),
            0,
            0,
            null,
            new ProfileData(
                user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));

    when(commentQueryService.findById(eq(comment.getId()), eq(user)))
        .thenReturn(Optional.of(commentData));
  }

  @Test
  public void should_like_comment_success() throws Exception {
    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/comments/{commentId}/like", comment.getId())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("comment.id", equalTo(comment.getId()));

    verify(commentReactionRepository).save(any(CommentReaction.class));
  }

  @Test
  public void should_dislike_comment_success() throws Exception {
    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/comments/{commentId}/dislike", comment.getId())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("comment.id", equalTo(comment.getId()));

    verify(commentReactionRepository).save(any(CommentReaction.class));
  }

  @Test
  public void should_toggle_like_when_already_liked() throws Exception {
    CommentReaction existingReaction =
        new CommentReaction(comment.getId(), user.getId(), ReactionType.LIKE);
    when(commentReactionRepository.find(eq(comment.getId()), eq(user.getId())))
        .thenReturn(Optional.of(existingReaction));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/comments/{commentId}/like", comment.getId())
        .prettyPeek()
        .then()
        .statusCode(200);

    verify(commentReactionRepository).remove(existingReaction);
  }

  @Test
  public void should_switch_from_dislike_to_like() throws Exception {
    CommentReaction existingReaction =
        new CommentReaction(comment.getId(), user.getId(), ReactionType.DISLIKE);
    when(commentReactionRepository.find(eq(comment.getId()), eq(user.getId())))
        .thenReturn(Optional.of(existingReaction));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/comments/{commentId}/like", comment.getId())
        .prettyPeek()
        .then()
        .statusCode(200);

    verify(commentReactionRepository).save(any(CommentReaction.class));
  }

  @Test
  public void should_remove_reaction_success() throws Exception {
    CommentReaction existingReaction =
        new CommentReaction(comment.getId(), user.getId(), ReactionType.LIKE);
    when(commentReactionRepository.find(eq(comment.getId()), eq(user.getId())))
        .thenReturn(Optional.of(existingReaction));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/comments/{commentId}/reaction", comment.getId())
        .prettyPeek()
        .then()
        .statusCode(200);

    verify(commentReactionRepository).remove(existingReaction);
  }

  @Test
  public void should_return_404_when_comment_not_found() throws Exception {
    when(commentRepository.findById(any(), eq("nonexistent"))).thenReturn(Optional.empty());

    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/comments/{commentId}/like", "nonexistent")
        .prettyPeek()
        .then()
        .statusCode(404);
  }
}
