package io.spring.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {
  @Mock private UserReadService userReadService;
  @InjectMocks private UserQueryService userQueryService;

  @Test
  void findById_existingUser_returnsUserData() {
    String userId = "user123";
    UserData userData = new UserData(userId, "test@example.com", "testuser", "bio", "image");
    when(userReadService.findById(userId)).thenReturn(userData);

    Optional<UserData> result = userQueryService.findById(userId);

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(userId);
    assertThat(result.get().getUsername()).isEqualTo("testuser");
    verify(userReadService).findById(userId);
  }

  @Test
  void findById_nonExistingUser_returnsEmpty() {
    String userId = "nonexistent";
    when(userReadService.findById(userId)).thenReturn(null);

    Optional<UserData> result = userQueryService.findById(userId);

    assertThat(result).isEmpty();
    verify(userReadService).findById(userId);
  }

  @Test
  void findById_validId_delegatesToUserReadService() {
    String userId = "user123";
    when(userReadService.findById(userId)).thenReturn(null);

    userQueryService.findById(userId);

    verify(userReadService).findById(userId);
  }
}
