package io.spring.infrastructure.mybatis.readservice;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.mapper.UserMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@MybatisTest
public class UserRelationshipQueryServiceTest {

  @Autowired private UserRelationshipQueryService userRelationshipQueryService;

  @Autowired private UserMapper userMapper;

  private User user1;
  private User user2;
  private User user3;

  @BeforeEach
  void setUp() {
    user1 = new User("user1@example.com", "user1", "password", "", "");
    userMapper.insert(user1);

    user2 = new User("user2@example.com", "user2", "password", "", "");
    userMapper.insert(user2);

    user3 = new User("user3@example.com", "user3", "password", "", "");
    userMapper.insert(user3);
  }

  @Test
  void isUserFollowing_whenUserFollowsAnother_returnsTrue() {
    FollowRelation followRelation = new FollowRelation(user1.getId(), user2.getId());
    userMapper.saveRelation(followRelation);

    boolean result = userRelationshipQueryService.isUserFollowing(user1.getId(), user2.getId());

    assertTrue(result);
  }

  @Test
  void isUserFollowing_whenUserDoesNotFollowAnother_returnsFalse() {
    boolean result = userRelationshipQueryService.isUserFollowing(user1.getId(), user2.getId());

    assertFalse(result);
  }

  @Test
  void isUserFollowing_withSameUserId_returnsFalse() {
    boolean result = userRelationshipQueryService.isUserFollowing(user1.getId(), user1.getId());

    assertFalse(result);
  }

  @Test
  void followingAuthors_withFollowedAuthors_returnsSetOfIds() {
    FollowRelation followRelation1 = new FollowRelation(user1.getId(), user2.getId());
    userMapper.saveRelation(followRelation1);

    FollowRelation followRelation2 = new FollowRelation(user1.getId(), user3.getId());
    userMapper.saveRelation(followRelation2);

    List<String> authorIds = Arrays.asList(user2.getId(), user3.getId());
    Set<String> result = userRelationshipQueryService.followingAuthors(user1.getId(), authorIds);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.contains(user2.getId()));
    assertTrue(result.contains(user3.getId()));
  }

  @Test
  void followingAuthors_withNoFollows_returnsEmptySet() {
    List<String> authorIds = Arrays.asList(user2.getId(), user3.getId());
    Set<String> result = userRelationshipQueryService.followingAuthors(user1.getId(), authorIds);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void followingAuthors_withPartialFollows_returnsOnlyFollowedIds() {
    FollowRelation followRelation = new FollowRelation(user1.getId(), user2.getId());
    userMapper.saveRelation(followRelation);

    List<String> authorIds = Arrays.asList(user2.getId(), user3.getId());
    Set<String> result = userRelationshipQueryService.followingAuthors(user1.getId(), authorIds);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(result.contains(user2.getId()));
    assertFalse(result.contains(user3.getId()));
  }

  @Test
  void followedUsers_withFollowedUsers_returnsListOfUserIds() {
    FollowRelation followRelation1 = new FollowRelation(user1.getId(), user2.getId());
    userMapper.saveRelation(followRelation1);

    FollowRelation followRelation2 = new FollowRelation(user1.getId(), user3.getId());
    userMapper.saveRelation(followRelation2);

    List<String> result = userRelationshipQueryService.followedUsers(user1.getId());

    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.contains(user2.getId()));
    assertTrue(result.contains(user3.getId()));
  }

  @Test
  void followedUsers_withNoFollows_returnsEmptyList() {
    List<String> result = userRelationshipQueryService.followedUsers(user1.getId());

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }
}
