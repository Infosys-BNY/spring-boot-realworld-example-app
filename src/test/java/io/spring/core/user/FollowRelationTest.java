package io.spring.core.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class FollowRelationTest {

  @Test
  public void constructor_withValidInputs_createsFollowRelation() {
    FollowRelation relation = new FollowRelation("user123", "target456");

    assertThat(relation.getUserId(), is("user123"));
    assertThat(relation.getTargetId(), is("target456"));
  }

  @Test
  public void noArgsConstructor_createsEmptyFollowRelation() {
    FollowRelation relation = new FollowRelation();
    assertNotNull(relation);
  }

  @Test
  public void setUserId_updatesUserId() {
    FollowRelation relation = new FollowRelation("user123", "target456");
    relation.setUserId("newUser789");

    assertThat(relation.getUserId(), is("newUser789"));
  }

  @Test
  public void setTargetId_updatesTargetId() {
    FollowRelation relation = new FollowRelation("user123", "target456");
    relation.setTargetId("newTarget789");

    assertThat(relation.getTargetId(), is("newTarget789"));
  }
}
