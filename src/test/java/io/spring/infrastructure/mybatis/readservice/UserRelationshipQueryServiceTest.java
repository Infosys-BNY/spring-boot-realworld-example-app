package io.spring.infrastructure.mybatis.readservice;

import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({MyBatisUserRepository.class})
public class UserRelationshipQueryServiceTest extends DbTestBase {
  @Autowired private UserRelationshipQueryService userRelationshipQueryService;
  @Autowired private UserRepository userRepository;

  private User user1;
  private User user2;
  private User user3;

  @BeforeEach
  public void setUp() {
    user1 = new User("user1@example.com", "user1", "password", "bio", "image");
    user2 = new User("user2@example.com", "user2", "password", "bio", "image");
    user3 = new User("user3@example.com", "user3", "password", "bio", "image");
    userRepository.save(user1);
    userRepository.save(user2);
    userRepository.save(user3);
  }

  @Test
  public void isUserFollowing_userFollowsAnother_returnsTrue() {
    FollowRelation relation = new FollowRelation(user1.getId(), user2.getId());
    userRepository.saveRelation(relation);

    boolean isFollowing =
        userRelationshipQueryService.isUserFollowing(user1.getId(), user2.getId());

    Assertions.assertTrue(isFollowing);
  }

  @Test
  public void isUserFollowing_userNotFollowingAnother_returnsFalse() {
    boolean isFollowing =
        userRelationshipQueryService.isUserFollowing(user1.getId(), user2.getId());

    Assertions.assertFalse(isFollowing);
  }

  @Test
  public void isUserFollowing_reverseRelation_returnsFalse() {
    FollowRelation relation = new FollowRelation(user1.getId(), user2.getId());
    userRepository.saveRelation(relation);

    boolean isFollowing =
        userRelationshipQueryService.isUserFollowing(user2.getId(), user1.getId());

    Assertions.assertFalse(isFollowing);
  }

  @Test
  public void followingAuthors_userFollowsSomeAuthors_returnsFollowedAuthorIds() {
    userRepository.saveRelation(new FollowRelation(user1.getId(), user2.getId()));
    userRepository.saveRelation(new FollowRelation(user1.getId(), user3.getId()));

    List<String> authorIds = Arrays.asList(user2.getId(), user3.getId());
    Set<String> followingAuthors =
        userRelationshipQueryService.followingAuthors(user1.getId(), authorIds);

    Assertions.assertNotNull(followingAuthors);
    Assertions.assertEquals(2, followingAuthors.size());
    Assertions.assertTrue(followingAuthors.contains(user2.getId()));
    Assertions.assertTrue(followingAuthors.contains(user3.getId()));
  }

  @Test
  public void followingAuthors_userFollowsNone_returnsEmptySet() {
    List<String> authorIds = Arrays.asList(user2.getId(), user3.getId());
    Set<String> followingAuthors =
        userRelationshipQueryService.followingAuthors(user1.getId(), authorIds);

    Assertions.assertNotNull(followingAuthors);
    Assertions.assertTrue(followingAuthors.isEmpty());
  }

  @Test
  public void followingAuthors_partialFollows_returnsOnlyFollowedAuthors() {
    userRepository.saveRelation(new FollowRelation(user1.getId(), user2.getId()));

    List<String> authorIds = Arrays.asList(user2.getId(), user3.getId());
    Set<String> followingAuthors =
        userRelationshipQueryService.followingAuthors(user1.getId(), authorIds);

    Assertions.assertNotNull(followingAuthors);
    Assertions.assertEquals(1, followingAuthors.size());
    Assertions.assertTrue(followingAuthors.contains(user2.getId()));
    Assertions.assertFalse(followingAuthors.contains(user3.getId()));
  }

  @Test
  public void followedUsers_userFollowsMultiple_returnsAllFollowedUserIds() {
    userRepository.saveRelation(new FollowRelation(user1.getId(), user2.getId()));
    userRepository.saveRelation(new FollowRelation(user1.getId(), user3.getId()));

    List<String> followedUsers = userRelationshipQueryService.followedUsers(user1.getId());

    Assertions.assertNotNull(followedUsers);
    Assertions.assertEquals(2, followedUsers.size());
    Assertions.assertTrue(followedUsers.contains(user2.getId()));
    Assertions.assertTrue(followedUsers.contains(user3.getId()));
  }

  @Test
  public void followedUsers_userFollowsNone_returnsEmptyList() {
    List<String> followedUsers = userRelationshipQueryService.followedUsers(user1.getId());

    Assertions.assertNotNull(followedUsers);
    Assertions.assertTrue(followedUsers.isEmpty());
  }
}
