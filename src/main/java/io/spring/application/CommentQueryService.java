package io.spring.application;

import static java.util.stream.Collectors.toList;

import io.spring.application.data.CommentData;
import io.spring.application.data.CommentReactionCount;
import io.spring.core.reaction.ReactionType;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.CommentReactionsReadService;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentQueryService {
  private CommentReadService commentReadService;
  private UserRelationshipQueryService userRelationshipQueryService;
  private CommentReactionsReadService commentReactionsReadService;

  public Optional<CommentData> findById(String id, User user) {
    CommentData commentData = commentReadService.findById(id);
    if (commentData == null) {
      return Optional.empty();
    } else {
      commentData
          .getProfileData()
          .setFollowing(
              userRelationshipQueryService.isUserFollowing(
                  user.getId(), commentData.getProfileData().getId()));
      fillReactionInfo(id, user, commentData);
    }
    return Optional.ofNullable(commentData);
  }

  public List<CommentData> findByArticleId(String articleId, User user) {
    List<CommentData> comments = commentReadService.findByArticleId(articleId);
    if (comments.size() > 0) {
      fillReactionInfo(comments, user);
      if (user != null) {
        Set<String> followingAuthors =
            userRelationshipQueryService.followingAuthors(
                user.getId(),
                comments.stream()
                    .map(commentData -> commentData.getProfileData().getId())
                    .collect(Collectors.toList()));
        comments.forEach(
            commentData -> {
              if (followingAuthors.contains(commentData.getProfileData().getId())) {
                commentData.getProfileData().setFollowing(true);
              }
            });
      }
    }
    return comments;
  }

  public CursorPager<CommentData> findByArticleIdWithCursor(
      String articleId, User user, CursorPageParameter<DateTime> page) {
    List<CommentData> comments = commentReadService.findByArticleIdWithCursor(articleId, page);
    if (comments.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    }
    fillReactionInfo(comments, user);
    if (user != null) {
      Set<String> followingAuthors =
          userRelationshipQueryService.followingAuthors(
              user.getId(),
              comments.stream()
                  .map(commentData -> commentData.getProfileData().getId())
                  .collect(Collectors.toList()));
      comments.forEach(
          commentData -> {
            if (followingAuthors.contains(commentData.getProfileData().getId())) {
              commentData.getProfileData().setFollowing(true);
            }
          });
    }
    boolean hasExtra = comments.size() > page.getLimit();
    if (hasExtra) {
      comments.remove(page.getLimit());
    }
    if (!page.isNext()) {
      Collections.reverse(comments);
    }
    return new CursorPager<>(comments, page.getDirection(), hasExtra);
  }

  private void fillReactionInfo(List<CommentData> comments, User user) {
    setReactionCounts(comments);
    if (user != null) {
      setUserReactions(comments, user);
    }
  }

  private void setReactionCounts(List<CommentData> comments) {
    List<CommentReactionCount> reactionCounts =
        commentReactionsReadService.getReactionCountsForComments(
            comments.stream().map(CommentData::getId).collect(toList()));
    Map<String, CommentReactionCount> countMap = new HashMap<>();
    reactionCounts.forEach(count -> countMap.put(count.getId(), count));
    comments.forEach(
        commentData -> {
          CommentReactionCount count = countMap.get(commentData.getId());
          if (count != null) {
            commentData.setLikeCount(count.getLikeCount());
            commentData.setDislikeCount(count.getDislikeCount());
          }
        });
  }

  private void setUserReactions(List<CommentData> comments, User user) {
    List<String> commentIds = comments.stream().map(CommentData::getId).collect(toList());
    Map<String, io.spring.application.data.CommentUserReaction> userReactions =
        commentReactionsReadService.getUserReactions(commentIds, user.getId());
    if (userReactions != null) {
      comments.forEach(
          commentData -> {
            if (userReactions.containsKey(commentData.getId())) {
              ReactionType reactionType = userReactions.get(commentData.getId()).getReactionType();
              commentData.setUserReaction(reactionType);
            }
          });
    }
  }

  private void fillReactionInfo(String id, User user, CommentData commentData) {
    List<CommentReactionCount> counts =
        commentReactionsReadService.getReactionCountsForComments(List.of(id));
    if (!counts.isEmpty()) {
      commentData.setLikeCount(counts.get(0).getLikeCount());
      commentData.setDislikeCount(counts.get(0).getDislikeCount());
    }
    if (user != null) {
      Map<String, io.spring.application.data.CommentUserReaction> userReactions =
          commentReactionsReadService.getUserReactions(List.of(id), user.getId());
      if (userReactions != null && userReactions.containsKey(id)) {
        commentData.setUserReaction(userReactions.get(id).getReactionType());
      }
    }
  }
}
