package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.CommentReactionCount;
import io.spring.application.data.CommentUserReaction;
import io.spring.core.reaction.CommentReaction;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentReactionsReadService {
  CommentReaction getUserReaction(
      @Param("commentId") String commentId, @Param("userId") String userId);

  List<CommentReactionCount> getReactionCountsForComments(@Param("ids") List<String> ids);

  @MapKey("id")
  Map<String, CommentUserReaction> getUserReactions(
      @Param("ids") List<String> ids, @Param("userId") String userId);
}
