package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.UserData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({MyBatisUserRepository.class})
public class UserReadServiceTest extends DbTestBase {
  @Autowired private UserReadService userReadService;
  @Autowired private UserRepository userRepository;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("john@example.com", "johndoe", "password", "My bio", "http://image.url");
    userRepository.save(user);
  }

  @Test
  public void findByUsername_existingUser_returnsUserData() {
    UserData userData = userReadService.findByUsername("johndoe");

    Assertions.assertNotNull(userData);
    Assertions.assertEquals(user.getId(), userData.getId());
    Assertions.assertEquals("johndoe", userData.getUsername());
    Assertions.assertEquals("john@example.com", userData.getEmail());
    Assertions.assertEquals("My bio", userData.getBio());
    Assertions.assertEquals("http://image.url", userData.getImage());
  }

  @Test
  public void findByUsername_nonExistingUser_returnsNull() {
    UserData userData = userReadService.findByUsername("nonexistent");

    Assertions.assertNull(userData);
  }

  @Test
  public void findById_existingUser_returnsUserData() {
    UserData userData = userReadService.findById(user.getId());

    Assertions.assertNotNull(userData);
    Assertions.assertEquals(user.getId(), userData.getId());
    Assertions.assertEquals("johndoe", userData.getUsername());
    Assertions.assertEquals("john@example.com", userData.getEmail());
  }

  @Test
  public void findById_nonExistingUser_returnsNull() {
    UserData userData = userReadService.findById("nonexistent-id");

    Assertions.assertNull(userData);
  }

  @Test
  public void findByUsername_caseSensitive_exactMatch() {
    UserData userData = userReadService.findByUsername("johndoe");
    Assertions.assertNotNull(userData);

    UserData upperCaseData = userReadService.findByUsername("JOHNDOE");
    Assertions.assertNull(upperCaseData);
  }
}
