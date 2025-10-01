package io.spring.infrastructure.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.spring.core.comment.CommentReaction;
import io.spring.core.comment.CommentReactionRepository;
import io.spring.core.comment.ReactionType;
import io.spring.infrastructure.DbTestBase;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({MyBatisCommentReactionRepository.class})
public class MyBatisCommentReactionRepositoryTest extends DbTestBase {
  @Autowired private CommentReactionRepository commentReactionRepository;

  @Test
  public void should_save_and_fetch_comment_reaction() {
    String commentId = "comment123";
    String userId = "user123";
    CommentReaction reaction = new CommentReaction(commentId, userId, ReactionType.LIKE);

    commentReactionRepository.save(reaction);

    Optional<CommentReaction> fetched = commentReactionRepository.find(commentId, userId);
    assertTrue(fetched.isPresent());
    assertThat(fetched.get().getCommentId(), is(commentId));
    assertThat(fetched.get().getUserId(), is(userId));
    assertThat(fetched.get().getType(), is(ReactionType.LIKE));
  }

  @Test
  public void should_update_existing_reaction_when_different_type() {
    String commentId = "comment123";
    String userId = "user123";
    CommentReaction likeReaction = new CommentReaction(commentId, userId, ReactionType.LIKE);
    commentReactionRepository.save(likeReaction);

    CommentReaction dislikeReaction = new CommentReaction(commentId, userId, ReactionType.DISLIKE);
    commentReactionRepository.save(dislikeReaction);

    Optional<CommentReaction> fetched = commentReactionRepository.find(commentId, userId);
    assertTrue(fetched.isPresent());
    assertThat(fetched.get().getType(), is(ReactionType.DISLIKE));
  }

  @Test
  public void should_not_save_duplicate_reaction_with_same_type() {
    String commentId = "comment123";
    String userId = "user123";
    CommentReaction reaction1 = new CommentReaction(commentId, userId, ReactionType.LIKE);
    CommentReaction reaction2 = new CommentReaction(commentId, userId, ReactionType.LIKE);

    commentReactionRepository.save(reaction1);
    commentReactionRepository.save(reaction2);

    Optional<CommentReaction> fetched = commentReactionRepository.find(commentId, userId);
    assertTrue(fetched.isPresent());
    assertThat(fetched.get().getType(), is(ReactionType.LIKE));
  }

  @Test
  public void should_remove_comment_reaction() {
    String commentId = "comment123";
    String userId = "user123";
    CommentReaction reaction = new CommentReaction(commentId, userId, ReactionType.LIKE);

    commentReactionRepository.save(reaction);
    Optional<CommentReaction> saved = commentReactionRepository.find(commentId, userId);
    assertTrue(saved.isPresent());

    commentReactionRepository.remove(saved.get());
    Optional<CommentReaction> removed = commentReactionRepository.find(commentId, userId);
    assertFalse(removed.isPresent());
  }

  @Test
  public void should_return_empty_when_reaction_not_found() {
    Optional<CommentReaction> fetched =
        commentReactionRepository.find("nonexistent", "nonexistent");
    assertFalse(fetched.isPresent());
  }
}
