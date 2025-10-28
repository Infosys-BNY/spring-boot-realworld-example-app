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
class DuplicatedEmailValidatorTest {
  @Mock private UserRepository userRepository;
  @Mock private ConstraintValidatorContext context;
  @InjectMocks private DuplicatedEmailValidator validator;

  @Test
  void isValid_uniqueEmail_returnsTrue() {
    String email = "unique@example.com";
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    boolean result = validator.isValid(email, context);

    assertThat(result).isTrue();
    verify(userRepository).findByEmail(email);
  }

  @Test
  void isValid_duplicateEmail_returnsFalse() {
    String email = "duplicate@example.com";
    User existingUser = new User(email, "testuser", "password", "bio", "image");
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

    boolean result = validator.isValid(email, context);

    assertThat(result).isFalse();
    verify(userRepository).findByEmail(email);
  }

  @Test
  void isValid_nullEmail_returnsTrue() {
    boolean result = validator.isValid(null, context);

    assertThat(result).isTrue();
  }

  @Test
  void isValid_emptyEmail_returnsTrue() {
    boolean result = validator.isValid("", context);

    assertThat(result).isTrue();
  }
}
