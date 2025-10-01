package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.application.data.CommentReactionCount;
import io.spring.core.comment.CommentReaction;
import io.spring.core.comment.ReactionType;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.CommentReactionsReadService;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.Collections;
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
    }
    enrichCommentWithReactions(commentData, user);
    commentData
        .getProfileData()
        .setFollowing(
            userRelationshipQueryService.isUserFollowing(
                user.getId(), commentData.getProfileData().getId()));
    return Optional.ofNullable(commentData);
  }

  public List<CommentData> findByArticleId(String articleId, User user) {
    List<CommentData> comments = commentReadService.findByArticleId(articleId);
    if (comments.size() > 0) {
      enrichCommentsWithReactions(comments, user);
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
    enrichCommentsWithReactions(comments, user);
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

  private void enrichCommentWithReactions(CommentData comment, User user) {
    CommentReactionCount counts = commentReactionsReadService.getReactionCounts(comment.getId());
    if (counts != null) {
      comment.setLikeCount(counts.getLikeCount());
      comment.setDislikeCount(counts.getDislikeCount());
    } else {
      comment.setLikeCount(0);
      comment.setDislikeCount(0);
    }
    if (user != null) {
      ReactionType userReaction =
          commentReactionsReadService.getUserReaction(comment.getId(), user.getId());
      comment.setUserReaction(userReaction);
    }
  }

  private void enrichCommentsWithReactions(List<CommentData> comments, User user) {
    List<String> commentIds =
        comments.stream().map(CommentData::getId).collect(Collectors.toList());
    List<CommentReactionCount> counts =
        commentReactionsReadService.getReactionCountsForComments(commentIds);
    Map<String, CommentReactionCount> countsMap =
        counts.stream().collect(Collectors.toMap(CommentReactionCount::getId, c -> c));
    comments.forEach(
        comment -> {
          CommentReactionCount count = countsMap.get(comment.getId());
          if (count != null) {
            comment.setLikeCount(count.getLikeCount());
            comment.setDislikeCount(count.getDislikeCount());
          } else {
            comment.setLikeCount(0);
            comment.setDislikeCount(0);
          }
        });
    if (user != null) {
      List<CommentReaction> userReactions =
          commentReactionsReadService.getUserReactionsForComments(commentIds, user.getId());
      if (userReactions != null && !userReactions.isEmpty()) {
        Map<String, ReactionType> userReactionsMap =
            userReactions.stream()
                .collect(Collectors.toMap(CommentReaction::getCommentId, CommentReaction::getType));
        comments.forEach(comment -> comment.setUserReaction(userReactionsMap.get(comment.getId())));
      }
    }
  }
}
