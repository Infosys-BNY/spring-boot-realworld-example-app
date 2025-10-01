package io.spring.core.reaction;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class CommentReaction {
  private String id;
  private String commentId;
  private String userId;
  private ReactionType reactionType;
  private DateTime createdAt;

  public CommentReaction(String commentId, String userId, ReactionType reactionType) {
    this.id = UUID.randomUUID().toString();
    this.commentId = commentId;
    this.userId = userId;
    this.reactionType = reactionType;
    this.createdAt = new DateTime();
  }
}
