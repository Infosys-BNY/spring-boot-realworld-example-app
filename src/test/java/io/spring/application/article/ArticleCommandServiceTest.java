package io.spring.application.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArticleCommandServiceTest {
  @Mock private ArticleRepository articleRepository;
  @InjectMocks private ArticleCommandService articleCommandService;

  private User creator;
  private NewArticleParam newArticleParam;
  private UpdateArticleParam updateArticleParam;

  @BeforeEach
  void setUp() {
    creator = new User("test@example.com", "testuser", "password", "bio", "image");
  }

  @Test
  void createArticle_validInput_returnsArticle() {
    newArticleParam =
        NewArticleParam.builder()
            .title("Test Article")
            .description("Test description")
            .body("Test body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article result = articleCommandService.createArticle(newArticleParam, creator);

    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("Test Article");
    assertThat(result.getDescription()).isEqualTo("Test description");
    assertThat(result.getBody()).isEqualTo("Test body");
    assertThat(result.getUserId()).isEqualTo(creator.getId());
  }

  @Test
  void createArticle_validInput_savesArticleToRepository() {
    newArticleParam =
        NewArticleParam.builder()
            .title("Test Article")
            .description("Test description")
            .body("Test body")
            .tagList(Arrays.asList("java"))
            .build();

    articleCommandService.createArticle(newArticleParam, creator);

    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void createArticle_withEmptyTagList_createsArticleWithoutTags() {
    newArticleParam =
        NewArticleParam.builder()
            .title("Test Article")
            .description("Test description")
            .body("Test body")
            .tagList(Arrays.asList())
            .build();

    Article result = articleCommandService.createArticle(newArticleParam, creator);

    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("Test Article");
    assertThat(result.getTags()).isEmpty();
  }

  @Test
  void updateArticle_validInput_updatesAndReturnsArticle() {
    Article existingArticle =
        new Article(
            "Original Title",
            "Original desc",
            "Original body",
            Arrays.asList("tag1"),
            creator.getId());
    updateArticleParam = new UpdateArticleParam("Updated Title", "Updated body", "Updated desc");

    Article result = articleCommandService.updateArticle(existingArticle, updateArticleParam);

    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("Updated Title");
    assertThat(result.getDescription()).isEqualTo("Updated desc");
    assertThat(result.getBody()).isEqualTo("Updated body");
  }

  @Test
  void updateArticle_validInput_savesArticleToRepository() {
    Article existingArticle =
        new Article(
            "Original Title",
            "Original desc",
            "Original body",
            Arrays.asList("tag1"),
            creator.getId());
    updateArticleParam = new UpdateArticleParam("Updated Title", "Updated body", "Updated desc");

    articleCommandService.updateArticle(existingArticle, updateArticleParam);

    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void updateArticle_emptyFields_keepsOriginalValues() {
    Article existingArticle =
        new Article(
            "Original Title",
            "Original desc",
            "Original body",
            Arrays.asList("tag1"),
            creator.getId());
    updateArticleParam = new UpdateArticleParam("", "", "");

    Article result = articleCommandService.updateArticle(existingArticle, updateArticleParam);

    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("Original Title");
    assertThat(result.getDescription()).isEqualTo("Original desc");
    assertThat(result.getBody()).isEqualTo("Original body");
  }

  @Test
  void updateArticle_partialUpdate_updatesOnlyProvidedFields() {
    Article existingArticle =
        new Article(
            "Original Title",
            "Original desc",
            "Original body",
            Arrays.asList("tag1"),
            creator.getId());
    updateArticleParam = new UpdateArticleParam("Updated Title", "", "");

    Article result = articleCommandService.updateArticle(existingArticle, updateArticleParam);

    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("Updated Title");
    assertThat(result.getDescription()).isEqualTo("Original desc");
    assertThat(result.getBody()).isEqualTo("Original body");
  }
}
