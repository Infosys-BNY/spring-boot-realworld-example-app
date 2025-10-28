package io.spring.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UpdateUserCommand;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
import io.spring.graphql.types.UserResult;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserMutationTest {
  @Mock private UserService userService;
  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder encryptService;
  @InjectMocks private UserMutation userMutation;

  @Mock private SecurityContext securityContext;
  @Mock private Authentication authentication;
  private User user;

  @BeforeEach
  void setUp() {
    user = new User("test@example.com", "testuser", "password", "bio", "image");
    SecurityContextHolder.setContext(securityContext);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void createUser_validInput_returnsUserPayload() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("test@example.com")
            .username("testuser")
            .password("password123")
            .build();
    when(userService.createUser(any(RegisterParam.class))).thenReturn(user);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertThat(result.getData()).isNotNull();
    assertThat(result.getData()).isInstanceOf(UserPayload.class);
    assertThat(result.getLocalContext()).isEqualTo(user);
    verify(userService).createUser(any(RegisterParam.class));
  }

  @Test
  void createUser_constraintViolation_returnsErrorData() {
    CreateUserInput input =
        CreateUserInput.newBuilder().email("").username("").password("").build();
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation = createMockViolation("email", "must not be blank");
    violations.add(violation);
    ConstraintViolationException exception = new ConstraintViolationException(violations);
    when(userService.createUser(any(RegisterParam.class))).thenThrow(exception);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertThat(result.getData()).isNotNull();
    assertThat(result.getLocalContext()).isNull();
  }

  @Test
  void createUser_multipleViolations_returnsErrorData() {
    CreateUserInput input =
        CreateUserInput.newBuilder().email("invalid").username("").password("short").build();
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("email", "must be valid email"));
    violations.add(createMockViolation("username", "must not be blank"));
    violations.add(createMockViolation("password", "must be at least 8 characters"));
    ConstraintViolationException exception = new ConstraintViolationException(violations);
    when(userService.createUser(any(RegisterParam.class))).thenThrow(exception);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertThat(result.getData()).isNotNull();
  }

  @Test
  void login_validCredentials_returnsUserPayload() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("password123", user.getPassword())).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login("password123", "test@example.com");

    assertThat(result.getData()).isNotNull();
    assertThat(result.getLocalContext()).isEqualTo(user);
    verify(userRepository).findByEmail("test@example.com");
    verify(encryptService).matches("password123", user.getPassword());
  }

  @Test
  void login_invalidPassword_throwsInvalidAuthenticationException() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("wrongpassword", user.getPassword())).thenReturn(false);

    assertThatThrownBy(() -> userMutation.login("wrongpassword", "test@example.com"))
        .isInstanceOf(InvalidAuthenticationException.class);
  }

  @Test
  void login_userNotFound_throwsInvalidAuthenticationException() {
    when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userMutation.login("password123", "nonexistent@example.com"))
        .isInstanceOf(InvalidAuthenticationException.class);
  }

  @Test
  void login_emptyPassword_throwsInvalidAuthenticationException() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(encryptService.matches("", user.getPassword())).thenReturn(false);

    assertThatThrownBy(() -> userMutation.login("", "test@example.com"))
        .isInstanceOf(InvalidAuthenticationException.class);
  }

  @Test
  void updateUser_validInput_returnsUserPayload() {
    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .email("new@example.com")
            .username("newusername")
            .bio("new bio")
            .image("new-image.png")
            .password("newpassword123")
            .build();
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(user);

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertThat(result).isNotNull();
    assertThat(result.getData()).isNotNull();
    assertThat(result.getLocalContext()).isEqualTo(user);
    verify(userService).updateUser(any(UpdateUserCommand.class));
  }

  @Test
  void updateUser_partialUpdate_returnsUserPayload() {
    UpdateUserInput input = UpdateUserInput.newBuilder().email("new@example.com").build();
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(user);

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertThat(result).isNotNull();
    assertThat(result.getData()).isNotNull();
    assertThat(result.getLocalContext()).isEqualTo(user);
    verify(userService).updateUser(any(UpdateUserCommand.class));
  }

  @Test
  void updateUser_anonymousAuthentication_returnsNull() {
    AnonymousAuthenticationToken anonymousAuth =
        new AnonymousAuthenticationToken(
            "anonymous", "anonymous", java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    when(securityContext.getAuthentication()).thenReturn(anonymousAuth);
    UpdateUserInput input = UpdateUserInput.newBuilder().email("new@example.com").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertThat(result).isNull();
  }

  @Test
  void updateUser_nullPrincipal_returnsNull() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(null);
    UpdateUserInput input = UpdateUserInput.newBuilder().email("new@example.com").build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertThat(result).isNull();
  }

  @SuppressWarnings("unchecked")
  private ConstraintViolation<?> createMockViolation(String propertyPath, String message) {
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("registerParam." + propertyPath);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn(message);
    when(violation.getRootBeanClass()).thenReturn((Class) RegisterParam.class);
    
    javax.validation.metadata.ConstraintDescriptor descriptor = mock(javax.validation.metadata.ConstraintDescriptor.class);
    java.lang.annotation.Annotation annotation = mock(java.lang.annotation.Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn((javax.validation.metadata.ConstraintDescriptor) descriptor);
    
    return violation;
  }
}
