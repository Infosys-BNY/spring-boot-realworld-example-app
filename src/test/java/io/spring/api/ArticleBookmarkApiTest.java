package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    io.restassured.module.mockmvc.RestAssuredMockMvc.mockMvc(mvc);
    article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());
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
            false,
            0,
            article.getCreatedAt(),
            article.getUpdatedAt(),
            article.getTags().stream().map(Tag::getName).collect(Collectors.toList()),
            new ProfileData(
                user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));
    when(articleQueryService.findBySlug(eq(article.getSlug()), eq(user)))
        .thenReturn(Optional.of(articleData));
  }

  @Test
  public void should_bookmark_an_article_success() throws Exception {
    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .when()
        .post("/articles/{slug}/bookmark", article.getSlug())
        .then()
        .statusCode(200);

    verify(articleBookmarkRepository).save(any(ArticleBookmark.class));
  }

  @Test
  public void should_unbookmark_an_article_success() throws Exception {
    ArticleBookmark articleBookmark = new ArticleBookmark(article.getId(), user.getId());
    when(articleBookmarkRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.of(articleBookmark));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}/bookmark", article.getSlug())
        .then()
        .statusCode(200);

    verify(articleBookmarkRepository).remove(eq(articleBookmark));
  }
}
