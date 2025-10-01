package io.spring.application.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @InjectMocks private UserService userService;

  private static final String DEFAULT_IMAGE = "https://default-image.com/avatar.png";

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(userService, "defaultImage", DEFAULT_IMAGE);
  }

  @Test
  void createUser_validInput_returnsUser() {
    RegisterParam registerParam = new RegisterParam("test@example.com", "testuser", "password123");
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

    User result = userService.createUser(registerParam);

    assertThat(result).isNotNull();
    assertThat(result.getEmail()).isEqualTo("test@example.com");
    assertThat(result.getUsername()).isEqualTo("testuser");
    assertThat(result.getPassword()).isEqualTo("encodedPassword");
    assertThat(result.getBio()).isEmpty();
    assertThat(result.getImage()).isEqualTo(DEFAULT_IMAGE);
  }

  @Test
  void createUser_validInput_encodesPassword() {
    RegisterParam registerParam = new RegisterParam("test@example.com", "testuser", "password123");
    when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

    User result = userService.createUser(registerParam);

    verify(passwordEncoder).encode("password123");
    assertThat(result.getPassword()).isEqualTo("encodedPassword");
  }

  @Test
  void createUser_validInput_savesToRepository() {
    RegisterParam registerParam = new RegisterParam("test@example.com", "testuser", "password123");
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

    userService.createUser(registerParam);

    verify(userRepository).save(any(User.class));
  }

  @Test
  void createUser_validInput_setsDefaultImage() {
    RegisterParam registerParam = new RegisterParam("test@example.com", "testuser", "password123");
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

    User result = userService.createUser(registerParam);

    assertThat(result.getImage()).isEqualTo(DEFAULT_IMAGE);
  }

  @Test
  void createUser_validInput_setsEmptyBio() {
    RegisterParam registerParam = new RegisterParam("test@example.com", "testuser", "password123");
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

    User result = userService.createUser(registerParam);

    assertThat(result.getBio()).isEmpty();
  }

  @Test
  void updateUser_validInput_updatesUser() {
    User existingUser =
        new User("old@example.com", "olduser", "oldpassword", "old bio", "old-image.png");
    UpdateUserParam updateUserParam =
        UpdateUserParam.builder()
            .email("new@example.com")
            .username("newuser")
            .password("newpassword")
            .bio("new bio")
            .image("new-image.png")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateUserParam);

    userService.updateUser(command);

    assertThat(existingUser.getEmail()).isEqualTo("new@example.com");
    assertThat(existingUser.getUsername()).isEqualTo("newuser");
    assertThat(existingUser.getPassword()).isEqualTo("newpassword");
    assertThat(existingUser.getBio()).isEqualTo("new bio");
    assertThat(existingUser.getImage()).isEqualTo("new-image.png");
  }

  @Test
  void updateUser_validInput_savesToRepository() {
    User existingUser =
        new User("old@example.com", "olduser", "oldpassword", "old bio", "old-image.png");
    UpdateUserParam updateUserParam =
        UpdateUserParam.builder()
            .email("new@example.com")
            .username("newuser")
            .password("newpassword")
            .bio("new bio")
            .image("new-image.png")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateUserParam);

    userService.updateUser(command);

    verify(userRepository).save(existingUser);
  }

  @Test
  void updateUser_emptyFields_keepsOriginalValues() {
    User existingUser =
        new User("old@example.com", "olduser", "oldpassword", "old bio", "old-image.png");
    UpdateUserParam updateUserParam =
        UpdateUserParam.builder().email("").username("").password("").bio("").image("").build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateUserParam);

    userService.updateUser(command);

    assertThat(existingUser.getEmail()).isEqualTo("old@example.com");
    assertThat(existingUser.getUsername()).isEqualTo("olduser");
    assertThat(existingUser.getPassword()).isEqualTo("oldpassword");
    assertThat(existingUser.getBio()).isEqualTo("old bio");
    assertThat(existingUser.getImage()).isEqualTo("old-image.png");
  }

  @Test
  void updateUser_partialUpdate_updatesOnlyProvidedFields() {
    User existingUser =
        new User("old@example.com", "olduser", "oldpassword", "old bio", "old-image.png");
    UpdateUserParam updateUserParam =
        UpdateUserParam.builder()
            .email("new@example.com")
            .username("")
            .password("")
            .bio("")
            .image("")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateUserParam);

    userService.updateUser(command);

    assertThat(existingUser.getEmail()).isEqualTo("new@example.com");
    assertThat(existingUser.getUsername()).isEqualTo("olduser");
    assertThat(existingUser.getPassword()).isEqualTo("oldpassword");
    assertThat(existingUser.getBio()).isEqualTo("old bio");
    assertThat(existingUser.getImage()).isEqualTo("old-image.png");
  }
}
