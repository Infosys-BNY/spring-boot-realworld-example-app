package io.spring.core.article;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TagTest {

  @Test
  void constructor_withName_generatesUUID() {
    Tag tag = new Tag("java");

    assertNotNull(tag.getId());
    assertNotNull(tag.getName());
    assertEquals("java", tag.getName());
  }

  @Test
  void constructor_withName_setsNameCorrectly() {
    Tag tag = new Tag("spring-boot");

    assertEquals("spring-boot", tag.getName());
  }

  @Test
  void equals_withSameName_returnsTrue() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");

    assertEquals(tag1, tag2);
  }

  @Test
  void equals_withDifferentName_returnsFalse() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("python");

    assertNotEquals(tag1, tag2);
  }

  @Test
  void hashCode_basedOnName() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");

    assertEquals(tag1.hashCode(), tag2.hashCode());
  }

  @Test
  void hashCode_differentForDifferentNames() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("python");

    assertNotEquals(tag1.hashCode(), tag2.hashCode());
  }

  @Test
  void noArgsConstructor_createsTag() {
    Tag tag = new Tag();

    assertNotNull(tag);
  }

  @Test
  void setName_updatesName() {
    Tag tag = new Tag();
    tag.setName("javascript");

    assertEquals("javascript", tag.getName());
  }
}
