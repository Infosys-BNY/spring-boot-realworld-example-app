package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FollowRelationTest {

  @Test
  void constructor_withUserIdAndTargetId_setsFieldsCorrectly() {
    FollowRelation followRelation = new FollowRelation("user123", "target456");

    assertEquals("user123", followRelation.getUserId());
    assertEquals("target456", followRelation.getTargetId());
  }

  @Test
  void constructor_setsUserId() {
    FollowRelation followRelation = new FollowRelation("user123", "target456");

    assertEquals("user123", followRelation.getUserId());
  }

  @Test
  void constructor_setsTargetId() {
    FollowRelation followRelation = new FollowRelation("user123", "target456");

    assertEquals("target456", followRelation.getTargetId());
  }

  @Test
  void noArgsConstructor_createsFollowRelation() {
    FollowRelation followRelation = new FollowRelation();

    assertNotNull(followRelation);
  }

  @Test
  void setUserId_updatesUserId() {
    FollowRelation followRelation = new FollowRelation();
    followRelation.setUserId("newUser");

    assertEquals("newUser", followRelation.getUserId());
  }

  @Test
  void setTargetId_updatesTargetId() {
    FollowRelation followRelation = new FollowRelation();
    followRelation.setTargetId("newTarget");

    assertEquals("newTarget", followRelation.getTargetId());
  }

  @Test
  void equals_withSameFields_returnsTrue() {
    FollowRelation followRelation1 = new FollowRelation("user123", "target456");
    FollowRelation followRelation2 = new FollowRelation("user123", "target456");

    assertEquals(followRelation1, followRelation2);
  }

  @Test
  void hashCode_consistentWithEquals() {
    FollowRelation followRelation1 = new FollowRelation("user123", "target456");
    FollowRelation followRelation2 = new FollowRelation("user123", "target456");

    assertEquals(followRelation1.hashCode(), followRelation2.hashCode());
  }
}
