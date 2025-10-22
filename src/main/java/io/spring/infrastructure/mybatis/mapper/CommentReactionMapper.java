package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.reaction.CommentReaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentReactionMapper {
  CommentReaction find(@Param("commentId") String commentId, @Param("userId") String userId);

  void insert(@Param("reaction") CommentReaction reaction);

  void delete(@Param("reaction") CommentReaction reaction);
}
