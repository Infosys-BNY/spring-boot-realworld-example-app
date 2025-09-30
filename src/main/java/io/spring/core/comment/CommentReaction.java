package io.spring.core.comment;

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
  private ReactionType type;
  private DateTime createdAt;

  public CommentReaction(String commentId, String userId, ReactionType type) {
    this.id = UUID.randomUUID().toString();
    this.commentId = commentId;
    this.userId = userId;
    this.type = type;
    this.createdAt = new DateTime();
  }
}
