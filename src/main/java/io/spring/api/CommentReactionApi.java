package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentReaction;
import io.spring.core.comment.CommentReactionRepository;
import io.spring.core.comment.CommentRepository;
import io.spring.core.comment.ReactionType;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "comments/{commentId}")
@AllArgsConstructor
public class CommentReactionApi {
  private CommentRepository commentRepository;
  private CommentReactionRepository commentReactionRepository;
  private CommentQueryService commentQueryService;

  @PostMapping(path = "like")
  public ResponseEntity likeComment(
      @PathVariable("commentId") String commentId, @AuthenticationPrincipal User user) {
    return handleReaction(commentId, user, ReactionType.LIKE);
  }

  @PostMapping(path = "dislike")
  public ResponseEntity dislikeComment(
      @PathVariable("commentId") String commentId, @AuthenticationPrincipal User user) {
    return handleReaction(commentId, user, ReactionType.DISLIKE);
  }

  @DeleteMapping(path = "reaction")
  public ResponseEntity removeReaction(
      @PathVariable("commentId") String commentId, @AuthenticationPrincipal User user) {
    commentReactionRepository
        .find(commentId, user.getId())
        .ifPresent(reaction -> commentReactionRepository.remove(reaction));
    return responseCommentData(commentQueryService.findById(commentId, user).get());
  }

  private ResponseEntity handleReaction(String commentId, User user, ReactionType newType) {
    Comment comment =
        commentRepository.findById(null, commentId).orElseThrow(ResourceNotFoundException::new);
    commentReactionRepository
        .find(commentId, user.getId())
        .ifPresentOrElse(
            existingReaction -> {
              if (existingReaction.getType().equals(newType)) {
                commentReactionRepository.remove(existingReaction);
              } else {
                CommentReaction updatedReaction =
                    new CommentReaction(commentId, user.getId(), newType);
                commentReactionRepository.save(updatedReaction);
              }
            },
            () -> {
              CommentReaction newReaction = new CommentReaction(commentId, user.getId(), newType);
              commentReactionRepository.save(newReaction);
            });
    return responseCommentData(commentQueryService.findById(commentId, user).get());
  }

  private ResponseEntity<Map<String, Object>> responseCommentData(final CommentData commentData) {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("comment", commentData);
          }
        });
  }
}
