package io.spring.infrastructure.mybatis.readservice;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import io.spring.application.data.CommentReactionCount;
import io.spring.core.comment.CommentReaction;
import io.spring.core.comment.CommentReactionRepository;
import io.spring.core.comment.ReactionType;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisCommentReactionRepository;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({MyBatisCommentReactionRepository.class})
public class CommentReactionsReadServiceTest extends DbTestBase {
  @Autowired private CommentReactionsReadService commentReactionsReadService;
  @Autowired private CommentReactionRepository commentReactionRepository;

  private String commentId1;
  private String commentId2;
  private String userId1;
  private String userId2;
  private String userId3;

  @BeforeEach
  public void setUp() {
    commentId1 = "comment1";
    commentId2 = "comment2";
    userId1 = "user1";
    userId2 = "user2";
    userId3 = "user3";
  }

  @Test
  public void should_get_reaction_counts_for_single_comment() {
    commentReactionRepository.save(new CommentReaction(commentId1, userId1, ReactionType.LIKE));
    commentReactionRepository.save(new CommentReaction(commentId1, userId2, ReactionType.LIKE));
    commentReactionRepository.save(new CommentReaction(commentId1, userId3, ReactionType.DISLIKE));

    CommentReactionCount counts = commentReactionsReadService.getReactionCounts(commentId1);

    assertThat(counts, notNullValue());
    assertThat(counts.getId(), is(commentId1));
    assertThat(counts.getLikeCount(), is(2));
    assertThat(counts.getDislikeCount(), is(1));
  }

  @Test
  public void should_get_reaction_counts_for_multiple_comments() {
    commentReactionRepository.save(new CommentReaction(commentId1, userId1, ReactionType.LIKE));
    commentReactionRepository.save(new CommentReaction(commentId1, userId2, ReactionType.DISLIKE));
    commentReactionRepository.save(new CommentReaction(commentId2, userId1, ReactionType.LIKE));
    commentReactionRepository.save(new CommentReaction(commentId2, userId2, ReactionType.LIKE));
    commentReactionRepository.save(new CommentReaction(commentId2, userId3, ReactionType.LIKE));

    List<CommentReactionCount> counts =
        commentReactionsReadService.getReactionCountsForComments(
            Arrays.asList(commentId1, commentId2));

    assertThat(counts.size(), is(2));

    CommentReactionCount count1 =
        counts.stream().filter(c -> c.getId().equals(commentId1)).findFirst().orElse(null);
    assertThat(count1, notNullValue());
    assertThat(count1.getLikeCount(), is(1));
    assertThat(count1.getDislikeCount(), is(1));

    CommentReactionCount count2 =
        counts.stream().filter(c -> c.getId().equals(commentId2)).findFirst().orElse(null);
    assertThat(count2, notNullValue());
    assertThat(count2.getLikeCount(), is(3));
    assertThat(count2.getDislikeCount(), is(0));
  }

  @Test
  public void should_get_user_reaction_for_comment() {
    commentReactionRepository.save(new CommentReaction(commentId1, userId1, ReactionType.LIKE));

    ReactionType userReaction = commentReactionsReadService.getUserReaction(commentId1, userId1);

    assertThat(userReaction, is(ReactionType.LIKE));
  }

  @Test
  public void should_return_null_when_user_has_no_reaction() {
    ReactionType userReaction = commentReactionsReadService.getUserReaction(commentId1, userId1);

    assertThat(userReaction, nullValue());
  }

  @Test
  public void should_get_user_reactions_for_multiple_comments() {
    commentReactionRepository.save(new CommentReaction(commentId1, userId1, ReactionType.LIKE));
    commentReactionRepository.save(new CommentReaction(commentId2, userId1, ReactionType.DISLIKE));

    List<CommentReaction> userReactions =
        commentReactionsReadService.getUserReactionsForComments(
            Arrays.asList(commentId1, commentId2), userId1);

    assertThat(userReactions.size(), is(2));
    CommentReaction reaction1 =
        userReactions.stream()
            .filter(r -> r.getCommentId().equals(commentId1))
            .findFirst()
            .orElse(null);
    assertThat(reaction1, notNullValue());
    assertThat(reaction1.getType(), is(ReactionType.LIKE));

    CommentReaction reaction2 =
        userReactions.stream()
            .filter(r -> r.getCommentId().equals(commentId2))
            .findFirst()
            .orElse(null);
    assertThat(reaction2, notNullValue());
    assertThat(reaction2.getType(), is(ReactionType.DISLIKE));
  }

  @Test
  public void should_return_empty_list_when_user_has_no_reactions() {
    List<CommentReaction> userReactions =
        commentReactionsReadService.getUserReactionsForComments(
            Arrays.asList(commentId1, commentId2), userId1);

    assertThat(userReactions.size(), is(0));
  }

  @Test
  public void should_return_zero_counts_for_comment_with_no_reactions() {
    CommentReactionCount counts = commentReactionsReadService.getReactionCounts(commentId1);

    assertThat(counts, nullValue());
  }
}
