package io.spring.core.reaction;

import java.util.Optional;

public interface CommentReactionRepository {
  void save(CommentReaction commentReaction);

  Optional<CommentReaction> find(String commentId, String userId);

  void remove(CommentReaction reaction);
}
