package io.spring.core.comment;

import java.util.Optional;

public interface CommentReactionRepository {
  void save(CommentReaction reaction);

  Optional<CommentReaction> find(String commentId, String userId);

  void remove(CommentReaction reaction);
}
