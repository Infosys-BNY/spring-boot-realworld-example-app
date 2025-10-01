package io.spring.core.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class TagTest {

  @Test
  public void constructor_withName_setsNameAndGeneratesId() {
    Tag tag = new Tag("java");
    assertThat(tag.getName(), is("java"));
    assertThat(tag.getId(), notNullValue());
  }

  @Test
  public void constructor_withName_generatesUniqueIds() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("spring");
    assertNotEquals(tag1.getId(), tag2.getId());
  }

  @Test
  public void noArgsConstructor_createsEmptyTag() {
    Tag tag = new Tag();
    assertNotNull(tag);
  }

  @Test
  public void equals_sameNames_areEqual() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");
    assertEquals(tag1, tag2);
  }

  @Test
  public void equals_differentNames_areNotEqual() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("spring");
    assertNotEquals(tag1, tag2);
  }

  @Test
  public void hashCode_sameNames_haveSameHashCode() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");
    assertEquals(tag1.hashCode(), tag2.hashCode());
  }
}
