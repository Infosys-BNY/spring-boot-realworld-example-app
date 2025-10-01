package io.spring.application.data;

import lombok.Value;

@Value
public class CommentReactionCount {
  private String id;
  private Integer likeCount;
  private Integer dislikeCount;
}
