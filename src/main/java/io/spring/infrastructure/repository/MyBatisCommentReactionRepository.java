package io.spring.infrastructure.repository;

import io.spring.core.reaction.CommentReaction;
import io.spring.core.reaction.CommentReactionRepository;
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
  public void save(CommentReaction commentReaction) {
    CommentReaction existing =
        mapper.find(commentReaction.getCommentId(), commentReaction.getUserId());
    if (existing == null) {
      mapper.insert(commentReaction);
    } else if (!existing.getReactionType().equals(commentReaction.getReactionType())) {
      mapper.delete(existing);
      mapper.insert(commentReaction);
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
