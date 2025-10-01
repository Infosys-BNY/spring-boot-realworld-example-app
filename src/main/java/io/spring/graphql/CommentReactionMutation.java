package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.reaction.CommentReaction;
import io.spring.core.reaction.CommentReactionRepository;
import io.spring.core.reaction.ReactionType;
import io.spring.core.user.User;
import io.spring.graphql.DgsConstants.MUTATION;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.CommentPayload;
import lombok.AllArgsConstructor;

@DgsComponent
@AllArgsConstructor
public class CommentReactionMutation {

  private CommentRepository commentRepository;
  private CommentReactionRepository commentReactionRepository;
  private CommentQueryService commentQueryService;

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.LikeComment)
  public DataFetcherResult<CommentPayload> likeComment(@InputArgument("id") String commentId) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    Comment comment =
        commentRepository.findById("", commentId).orElseThrow(ResourceNotFoundException::new);
    CommentReaction commentReaction =
        new CommentReaction(comment.getId(), user.getId(), ReactionType.LIKE);
    commentReactionRepository.save(commentReaction);
    CommentData commentData =
        commentQueryService.findById(commentId, user).orElseThrow(ResourceNotFoundException::new);
    return DataFetcherResult.<CommentPayload>newResult()
        .localContext(commentData)
        .data(CommentPayload.newBuilder().build())
        .build();
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.DislikeComment)
  public DataFetcherResult<CommentPayload> dislikeComment(@InputArgument("id") String commentId) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    Comment comment =
        commentRepository.findById("", commentId).orElseThrow(ResourceNotFoundException::new);
    CommentReaction commentReaction =
        new CommentReaction(comment.getId(), user.getId(), ReactionType.DISLIKE);
    commentReactionRepository.save(commentReaction);
    CommentData commentData =
        commentQueryService.findById(commentId, user).orElseThrow(ResourceNotFoundException::new);
    return DataFetcherResult.<CommentPayload>newResult()
        .localContext(commentData)
        .data(CommentPayload.newBuilder().build())
        .build();
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.RemoveCommentReaction)
  public DataFetcherResult<CommentPayload> removeCommentReaction(
      @InputArgument("id") String commentId) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    commentRepository.findById("", commentId).orElseThrow(ResourceNotFoundException::new);
    commentReactionRepository
        .find(commentId, user.getId())
        .ifPresent(reaction -> commentReactionRepository.remove(reaction));
    CommentData commentData =
        commentQueryService.findById(commentId, user).orElseThrow(ResourceNotFoundException::new);
    return DataFetcherResult.<CommentPayload>newResult()
        .localContext(commentData)
        .data(CommentPayload.newBuilder().build())
        .build();
  }
}
