package io.spring.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.graphql.types.User.Builder;
import java.util.Optional;
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

@ExtendWith(MockitoExtension.class)
class MeDatafetcherTest {
  @Mock private UserQueryService userQueryService;
  @Mock private JwtService jwtService;
  @InjectMocks private MeDatafetcher meDatafetcher;

  @Mock private DataFetchingEnvironment dataFetchingEnvironment;
  @Mock private SecurityContext securityContext;
  @Mock private Authentication authentication;

  private User user;
  private UserData userData;

  @BeforeEach
  void setUp() {
    user = new User("test@example.com", "testuser", "password", "bio", "image");
    userData =
        new UserData(user.getId(), user.getEmail(), user.getUsername(), user.getBio(), user.getImage());
    SecurityContextHolder.setContext(securityContext);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void getMe_validAuthentication_returnsUser() {
    String authHeader = "Bearer validtoken123";
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(user);
    when(userQueryService.findById(user.getId())).thenReturn(Optional.of(userData));

    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getMe(authHeader, dataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getEmail()).isEqualTo("test@example.com");
    assertThat(result.getData().getUsername()).isEqualTo("testuser");
    assertThat(result.getData().getToken()).isEqualTo("validtoken123");
    assertThat(result.getLocalContext()).isEqualTo(user);
  }

  @Test
  void getMe_anonymousAuthentication_returnsNull() {
    AnonymousAuthenticationToken anonymousAuth =
        new AnonymousAuthenticationToken(
            "anonymous",
            "anonymous",
            java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    when(securityContext.getAuthentication()).thenReturn(anonymousAuth);

    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getMe("Bearer token", dataFetchingEnvironment);

    assertThat(result).isNull();
  }

  @Test
  void getMe_nullPrincipal_returnsNull() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(null);

    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getMe("Bearer token", dataFetchingEnvironment);

    assertThat(result).isNull();
  }

  @Test
  void getMe_userNotFound_throwsResourceNotFoundException() {
    String authHeader = "Bearer validtoken123";
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(user);
    when(userQueryService.findById(user.getId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> meDatafetcher.getMe(authHeader, dataFetchingEnvironment))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void getMe_withBearerPrefix_extractsTokenCorrectly() {
    String authHeader = "Bearer mytoken456";
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(user);
    when(userQueryService.findById(user.getId())).thenReturn(Optional.of(userData));

    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getMe(authHeader, dataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getData().getToken()).isEqualTo("mytoken456");
  }

  @Test
  void getMe_validAuthenticationWithDifferentUser_returnsCorrectUser() {
    User differentUser =
        new User("different@example.com", "differentuser", "password", "different bio", "different.png");
    UserData differentUserData =
        new UserData(
            differentUser.getId(),
            differentUser.getEmail(),
            differentUser.getUsername(),
            differentUser.getBio(),
            differentUser.getImage());
    String authHeader = "Bearer differenttoken";
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(differentUser);
    when(userQueryService.findById(differentUser.getId())).thenReturn(Optional.of(differentUserData));

    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getMe(authHeader, dataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getData().getEmail()).isEqualTo("different@example.com");
    assertThat(result.getData().getUsername()).isEqualTo("differentuser");
    assertThat(result.getLocalContext()).isEqualTo(differentUser);
  }

  @Test
  void getUserPayloadUser_validUser_returnsUserWithToken() {
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(user);
    when(jwtService.toToken(user)).thenReturn("generated-jwt-token");

    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getUserPayloadUser(dataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getData()).isNotNull();
    assertThat(result.getData().getEmail()).isEqualTo("test@example.com");
    assertThat(result.getData().getUsername()).isEqualTo("testuser");
    assertThat(result.getData().getToken()).isEqualTo("generated-jwt-token");
    assertThat(result.getLocalContext()).isEqualTo(user);
  }

  @Test
  void getUserPayloadUser_differentUser_returnsCorrectUserWithToken() {
    User differentUser =
        new User("another@example.com", "anotheruser", "password", "another bio", "another.png");
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(differentUser);
    when(jwtService.toToken(differentUser)).thenReturn("another-jwt-token");

    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getUserPayloadUser(dataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getData().getEmail()).isEqualTo("another@example.com");
    assertThat(result.getData().getUsername()).isEqualTo("anotheruser");
    assertThat(result.getData().getToken()).isEqualTo("another-jwt-token");
    assertThat(result.getLocalContext()).isEqualTo(differentUser);
  }

  @Test
  void getUserPayloadUser_userWithEmptyBio_returnsUserWithToken() {
    User userWithEmptyBio = new User("empty@example.com", "emptyuser", "password", "", "image.png");
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(userWithEmptyBio);
    when(jwtService.toToken(userWithEmptyBio)).thenReturn("empty-bio-token");

    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getUserPayloadUser(dataFetchingEnvironment);

    assertThat(result).isNotNull();
    assertThat(result.getData().getEmail()).isEqualTo("empty@example.com");
    assertThat(result.getData().getUsername()).isEqualTo("emptyuser");
    assertThat(result.getData().getToken()).isEqualTo("empty-bio-token");
  }
}
