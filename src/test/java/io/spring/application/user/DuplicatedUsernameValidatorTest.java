package io.spring.application.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import javax.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DuplicatedUsernameValidatorTest {
  @Mock private UserRepository userRepository;
  @Mock private ConstraintValidatorContext context;
  @InjectMocks private DuplicatedUsernameValidator validator;

  @Test
  void isValid_uniqueUsername_returnsTrue() {
    String username = "uniqueuser";
    when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

    boolean result = validator.isValid(username, context);

    assertThat(result).isTrue();
    verify(userRepository).findByUsername(username);
  }

  @Test
  void isValid_duplicateUsername_returnsFalse() {
    String username = "duplicateuser";
    User existingUser = new User("test@example.com", username, "password", "bio", "image");
    when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));

    boolean result = validator.isValid(username, context);

    assertThat(result).isFalse();
    verify(userRepository).findByUsername(username);
  }

  @Test
  void isValid_nullUsername_returnsTrue() {
    boolean result = validator.isValid(null, context);

    assertThat(result).isTrue();
  }

  @Test
  void isValid_emptyUsername_returnsTrue() {
    boolean result = validator.isValid("", context);

    assertThat(result).isTrue();
  }
}
