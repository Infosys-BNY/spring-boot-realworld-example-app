package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.core.reaction.CommentReaction;
import io.spring.core.reaction.CommentReactionRepository;
import io.spring.core.reaction.ReactionType;
import io.spring.core.user.User;
import java.util.HashMap;
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
  private CommentReactionRepository commentReactionRepository;
  private CommentQueryService commentQueryService;

  @PostMapping("/like")
  public ResponseEntity likeComment(
      @PathVariable("commentId") String commentId, @AuthenticationPrincipal User user) {
    commentQueryService.findById(commentId, user).orElseThrow(ResourceNotFoundException::new);
    CommentReaction commentReaction =
        new CommentReaction(commentId, user.getId(), ReactionType.LIKE);
    commentReactionRepository.save(commentReaction);
    return responseCommentData(commentQueryService.findById(commentId, user).get());
  }

  @PostMapping("/dislike")
  public ResponseEntity dislikeComment(
      @PathVariable("commentId") String commentId, @AuthenticationPrincipal User user) {
    commentQueryService.findById(commentId, user).orElseThrow(ResourceNotFoundException::new);
    CommentReaction commentReaction =
        new CommentReaction(commentId, user.getId(), ReactionType.DISLIKE);
    commentReactionRepository.save(commentReaction);
    return responseCommentData(commentQueryService.findById(commentId, user).get());
  }

  @DeleteMapping("/reaction")
  public ResponseEntity removeReaction(
      @PathVariable("commentId") String commentId, @AuthenticationPrincipal User user) {
    commentQueryService.findById(commentId, user).orElseThrow(ResourceNotFoundException::new);
    commentReactionRepository
        .find(commentId, user.getId())
        .ifPresent(reaction -> commentReactionRepository.remove(reaction));
    return responseCommentData(commentQueryService.findById(commentId, user).get());
  }

  private ResponseEntity<HashMap<String, Object>> responseCommentData(
      final CommentData commentData) {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("comment", commentData);
          }
        });
  }
}
