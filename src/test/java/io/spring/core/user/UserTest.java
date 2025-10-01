package io.spring.core.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  public void constructor_withValidInputs_createsUser() {
    User user = new User("john@example.com", "john", "password", "My bio", "http://image.url");

    assertThat(user.getEmail(), is("john@example.com"));
    assertThat(user.getUsername(), is("john"));
    assertThat(user.getPassword(), is("password"));
    assertThat(user.getBio(), is("My bio"));
    assertThat(user.getImage(), is("http://image.url"));
    assertThat(user.getId(), notNullValue());
  }

  @Test
  public void constructor_generatesUniqueIds() {
    User user1 = new User("john@example.com", "john", "password", "bio", "image");
    User user2 = new User("jane@example.com", "jane", "password", "bio", "image");

    assertNotEquals(user1.getId(), user2.getId());
  }

  @Test
  public void noArgsConstructor_createsEmptyUser() {
    User user = new User();
    assertNotNull(user);
  }

  @Test
  public void update_withAllNonEmptyFields_updatesAllFields() {
    User user = new User("old@example.com", "oldname", "oldpass", "old bio", "old image");

    user.update("new@example.com", "newname", "newpass", "new bio", "new image");

    assertThat(user.getEmail(), is("new@example.com"));
    assertThat(user.getUsername(), is("newname"));
    assertThat(user.getPassword(), is("newpass"));
    assertThat(user.getBio(), is("new bio"));
    assertThat(user.getImage(), is("new image"));
  }

  @Test
  public void update_withNullEmail_keepsOldEmail() {
    User user = new User("old@example.com", "oldname", "oldpass", "old bio", "old image");

    user.update(null, "newname", "newpass", "new bio", "new image");

    assertThat(user.getEmail(), is("old@example.com"));
    assertThat(user.getUsername(), is("newname"));
  }

  @Test
  public void update_withEmptyEmail_keepsOldEmail() {
    User user = new User("old@example.com", "oldname", "oldpass", "old bio", "old image");

    user.update("", "newname", "newpass", "new bio", "new image");

    assertThat(user.getEmail(), is("old@example.com"));
    assertThat(user.getUsername(), is("newname"));
  }

  @Test
  public void update_withNullUsername_keepsOldUsername() {
    User user = new User("old@example.com", "oldname", "oldpass", "old bio", "old image");

    user.update("new@example.com", null, "newpass", "new bio", "new image");

    assertThat(user.getEmail(), is("new@example.com"));
    assertThat(user.getUsername(), is("oldname"));
  }

  @Test
  public void update_withEmptyUsername_keepsOldUsername() {
    User user = new User("old@example.com", "oldname", "oldpass", "old bio", "old image");

    user.update("new@example.com", "", "newpass", "new bio", "new image");

    assertThat(user.getEmail(), is("new@example.com"));
    assertThat(user.getUsername(), is("oldname"));
  }

  @Test
  public void update_withNullPassword_keepsOldPassword() {
    User user = new User("old@example.com", "oldname", "oldpass", "old bio", "old image");

    user.update("new@example.com", "newname", null, "new bio", "new image");

    assertThat(user.getPassword(), is("oldpass"));
  }

  @Test
  public void update_withEmptyPassword_keepsOldPassword() {
    User user = new User("old@example.com", "oldname", "oldpass", "old bio", "old image");

    user.update("new@example.com", "newname", "", "new bio", "new image");

    assertThat(user.getPassword(), is("oldpass"));
  }

  @Test
  public void update_withNullBio_keepsOldBio() {
    User user = new User("old@example.com", "oldname", "oldpass", "old bio", "old image");

    user.update("new@example.com", "newname", "newpass", null, "new image");

    assertThat(user.getBio(), is("old bio"));
  }

  @Test
  public void update_withEmptyBio_keepsOldBio() {
    User user = new User("old@example.com", "oldname", "oldpass", "old bio", "old image");

    user.update("new@example.com", "newname", "newpass", "", "new image");

    assertThat(user.getBio(), is("old bio"));
  }

  @Test
  public void update_withNullImage_keepsOldImage() {
    User user = new User("old@example.com", "oldname", "oldpass", "old bio", "old image");

    user.update("new@example.com", "newname", "newpass", "new bio", null);

    assertThat(user.getImage(), is("old image"));
  }

  @Test
  public void update_withEmptyImage_keepsOldImage() {
    User user = new User("old@example.com", "oldname", "oldpass", "old bio", "old image");

    user.update("new@example.com", "newname", "newpass", "new bio", "");

    assertThat(user.getImage(), is("old image"));
  }

  @Test
  public void update_withAllNullFields_keepsAllOldFields() {
    User user = new User("old@example.com", "oldname", "oldpass", "old bio", "old image");

    user.update(null, null, null, null, null);

    assertThat(user.getEmail(), is("old@example.com"));
    assertThat(user.getUsername(), is("oldname"));
    assertThat(user.getPassword(), is("oldpass"));
    assertThat(user.getBio(), is("old bio"));
    assertThat(user.getImage(), is("old image"));
  }

  @Test
  public void equals_sameId_areEqual() {
    User user1 = new User("john@example.com", "john", "password", "bio", "image");
    assertEquals(user1, user1);
  }

  @Test
  public void equals_differentId_areNotEqual() {
    User user1 = new User("john@example.com", "john", "password", "bio", "image");
    User user2 = new User("jane@example.com", "jane", "password", "bio", "image");
    assertNotEquals(user1, user2);
  }
}
