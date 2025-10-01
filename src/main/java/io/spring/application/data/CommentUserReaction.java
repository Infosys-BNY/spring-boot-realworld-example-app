package io.spring.application.data;

import io.spring.core.reaction.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentUserReaction {
  private String id;
  private ReactionType reactionType;
}
