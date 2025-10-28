package io.spring.core.favorite;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteTest {

  @Test
  public void constructor_withValidInputs_createsArticleFavorite() {
    ArticleFavorite favorite = new ArticleFavorite("article123", "user456");

    assertThat(favorite.getArticleId(), is("article123"));
    assertThat(favorite.getUserId(), is("user456"));
  }

  @Test
  public void noArgsConstructor_createsEmptyArticleFavorite() {
    ArticleFavorite favorite = new ArticleFavorite();
    assertNotNull(favorite);
  }

  @Test
  public void equals_sameArticleAndUser_areEqual() {
    ArticleFavorite favorite1 = new ArticleFavorite("article123", "user456");
    ArticleFavorite favorite2 = new ArticleFavorite("article123", "user456");

    assertEquals(favorite1, favorite2);
  }

  @Test
  public void equals_differentArticle_areNotEqual() {
    ArticleFavorite favorite1 = new ArticleFavorite("article123", "user456");
    ArticleFavorite favorite2 = new ArticleFavorite("article789", "user456");

    assertNotEquals(favorite1, favorite2);
  }

  @Test
  public void equals_differentUser_areNotEqual() {
    ArticleFavorite favorite1 = new ArticleFavorite("article123", "user456");
    ArticleFavorite favorite2 = new ArticleFavorite("article123", "user789");

    assertNotEquals(favorite1, favorite2);
  }

  @Test
  public void hashCode_sameArticleAndUser_haveSameHashCode() {
    ArticleFavorite favorite1 = new ArticleFavorite("article123", "user456");
    ArticleFavorite favorite2 = new ArticleFavorite("article123", "user456");

    assertEquals(favorite1.hashCode(), favorite2.hashCode());
  }
}
