package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.article.Tag;
import io.spring.core.bookmark.ArticleBookmark;
import io.spring.core.bookmark.ArticleBookmarkRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ArticleBookmarkApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticleBookmarkApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private ArticleBookmarkRepository articleBookmarkRepository;

  @MockBean private ArticleRepository articleRepository;

  @MockBean private ArticleQueryService articleQueryService;

  private Article article;

  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
    User anotherUser = new User("other@test.com", "other", "123", "", "");
    article = new Article("title", "desc", "body", Arrays.asList("java"), anotherUser.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    ArticleData articleData =
        new ArticleData(
            article.getId(),
            article.getSlug(),
            article.getTitle(),
            article.getDescription(),
            article.getBody(),
            false,
            0,
            true,
            1,
            article.getCreatedAt(),
            article.getUpdatedAt(),
            article.getTags().stream().map(Tag::getName).collect(Collectors.toList()),
            new ProfileData(
                anotherUser.getId(),
                anotherUser.getUsername(),
                anotherUser.getBio(),
                anotherUser.getImage(),
                false));
    when(articleQueryService.findBySlug(eq(articleData.getSlug()), eq(user)))
        .thenReturn(Optional.of(articleData));
  }

  @Test
  public void should_bookmark_an_article_success() throws Exception {
    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/articles/{slug}/bookmark", article.getSlug())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("article.id", equalTo(article.getId()));

    verify(articleBookmarkRepository).save(any());
  }

  @Test
  public void should_unbookmark_an_article_success() throws Exception {
    when(articleBookmarkRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.of(new ArticleBookmark(article.getId(), user.getId())));
    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}/bookmark", article.getSlug())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("article.id", equalTo(article.getId()));
    verify(articleBookmarkRepository).remove(new ArticleBookmark(article.getId(), user.getId()));
  }

  @Test
  public void should_return_404_when_bookmarking_non_existent_article() throws Exception {
    when(articleRepository.findBySlug(eq("non-existent-slug"))).thenReturn(Optional.empty());
    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/articles/{slug}/bookmark", "non-existent-slug")
        .prettyPeek()
        .then()
        .statusCode(404);
  }

  @Test
  public void should_return_404_when_unbookmarking_non_existent_article() throws Exception {
    when(articleRepository.findBySlug(eq("non-existent-slug"))).thenReturn(Optional.empty());
    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}/bookmark", "non-existent-slug")
        .prettyPeek()
        .then()
        .statusCode(404);
  }
}
