package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.CommentReactionCount;
import io.spring.core.comment.CommentReaction;
import io.spring.core.comment.ReactionType;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentReactionsReadService {
  CommentReactionCount getReactionCounts(@Param("commentId") String commentId);

  List<CommentReactionCount> getReactionCountsForComments(
      @Param("commentIds") List<String> commentIds);

  ReactionType getUserReaction(
      @Param("commentId") String commentId, @Param("userId") String userId);

  List<CommentReaction> getUserReactionsForComments(
      @Param("commentIds") List<String> commentIds, @Param("userId") String userId);
}
