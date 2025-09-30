package io.spring.infrastructure.repository;

import io.spring.core.comment.CommentReaction;
import io.spring.core.comment.CommentReactionRepository;
import io.spring.infrastructure.mybatis.mapper.CommentReactionMapper;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisCommentReactionRepository implements CommentReactionRepository {
  private CommentReactionMapper mapper;

  @Autowired
  public MyBatisCommentReactionRepository(CommentReactionMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void save(CommentReaction reaction) {
    CommentReaction existing = mapper.find(reaction.getCommentId(), reaction.getUserId());
    if (existing == null) {
      mapper.insert(reaction);
    } else if (!existing.getType().equals(reaction.getType())) {
      mapper.update(reaction);
    }
  }

  @Override
  public Optional<CommentReaction> find(String commentId, String userId) {
    return Optional.ofNullable(mapper.find(commentId, userId));
  }

  @Override
  public void remove(CommentReaction reaction) {
    mapper.delete(reaction);
  }
}
