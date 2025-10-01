package io.spring.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ProfilePayload;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RelationMutationTest {
  @Mock private UserRepository userRepository;
  @Mock private ProfileQueryService profileQueryService;
  @InjectMocks private RelationMutation relationMutation;

  private MockedStatic<SecurityUtil> securityUtilMock;
  private User currentUser;
  private User targetUser;
  private ProfileData profileData;
  private FollowRelation followRelation;

  @BeforeEach
  void setUp() {
    securityUtilMock = Mockito.mockStatic(SecurityUtil.class);
    currentUser = new User("current@example.com", "currentuser", "password", "bio", "image");
    targetUser = new User("target@example.com", "targetuser", "password", "target bio", "target.png");
    profileData = new ProfileData(
        targetUser.getId(),
        targetUser.getUsername(),
        targetUser.getBio(),
        targetUser.getImage(),
        false);
    followRelation = new FollowRelation(currentUser.getId(), targetUser.getId());
  }

  @AfterEach
  void tearDown() {
    securityUtilMock.close();
  }

  @Test
  void follow_validUsername_returnsProfilePayload() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(targetUser));
    ProfileData followingProfileData = new ProfileData(
        targetUser.getId(),
        targetUser.getUsername(),
        targetUser.getBio(),
        targetUser.getImage(),
        true);
    when(profileQueryService.findByUsername("targetuser", currentUser))
        .thenReturn(Optional.of(followingProfileData));

    ProfilePayload result = relationMutation.follow("targetuser");

    assertThat(result).isNotNull();
    assertThat(result.getProfile()).isNotNull();
    assertThat(result.getProfile().getUsername()).isEqualTo("targetuser");
    assertThat(result.getProfile().getFollowing()).isTrue();
    verify(userRepository).saveRelation(any(FollowRelation.class));
  }

  @Test
  void follow_noAuthentication_throwsAuthenticationException() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

    assertThatThrownBy(() -> relationMutation.follow("targetuser"))
        .isInstanceOf(AuthenticationException.class);
  }

  @Test
  void follow_userNotFound_throwsResourceNotFoundException() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> relationMutation.follow("nonexistent"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void follow_alreadyFollowing_savesRelation() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(targetUser));
    ProfileData alreadyFollowingProfileData = new ProfileData(
        targetUser.getId(),
        targetUser.getUsername(),
        targetUser.getBio(),
        targetUser.getImage(),
        true);
    when(profileQueryService.findByUsername("targetuser", currentUser))
        .thenReturn(Optional.of(alreadyFollowingProfileData));

    ProfilePayload result = relationMutation.follow("targetuser");

    assertThat(result).isNotNull();
    assertThat(result.getProfile().getFollowing()).isTrue();
    verify(userRepository).saveRelation(any(FollowRelation.class));
  }

  @Test
  void unfollow_validUsername_returnsProfilePayload() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(targetUser));
    when(userRepository.findRelation(currentUser.getId(), targetUser.getId()))
        .thenReturn(Optional.of(followRelation));
    when(profileQueryService.findByUsername("targetuser", currentUser))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result = relationMutation.unfollow("targetuser");

    assertThat(result).isNotNull();
    assertThat(result.getProfile()).isNotNull();
    assertThat(result.getProfile().getUsername()).isEqualTo("targetuser");
    assertThat(result.getProfile().getFollowing()).isFalse();
    verify(userRepository).removeRelation(followRelation);
  }

  @Test
  void unfollow_noAuthentication_throwsAuthenticationException() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

    assertThatThrownBy(() -> relationMutation.unfollow("targetuser"))
        .isInstanceOf(AuthenticationException.class);
  }

  @Test
  void unfollow_userNotFound_throwsResourceNotFoundException() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> relationMutation.unfollow("nonexistent"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void unfollow_relationNotFound_throwsResourceNotFoundException() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(targetUser));
    when(userRepository.findRelation(currentUser.getId(), targetUser.getId()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> relationMutation.unfollow("targetuser"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void unfollow_notFollowing_throwsResourceNotFoundException() {
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(currentUser));
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(targetUser));
    when(userRepository.findRelation(currentUser.getId(), targetUser.getId()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> relationMutation.unfollow("targetuser"))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
