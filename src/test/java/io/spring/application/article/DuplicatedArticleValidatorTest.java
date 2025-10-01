package io.spring.application.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import java.util.Optional;
import javax.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DuplicatedArticleValidatorTest {
  @Mock private ArticleQueryService articleQueryService;
  @Mock private ConstraintValidatorContext context;
  @InjectMocks private DuplicatedArticleValidator validator;

  @Test
  void isValid_uniqueTitle_returnsTrue() {
    String title = "Unique Article Title";
    when(articleQueryService.findBySlug(any(String.class), eq(null))).thenReturn(Optional.empty());

    boolean result = validator.isValid(title, context);

    assertThat(result).isTrue();
  }

  @Test
  void isValid_duplicateTitle_returnsFalse() {
    String title = "Duplicate Article Title";
    ArticleData existingArticle = new ArticleData();
    when(articleQueryService.findBySlug(any(String.class), eq(null)))
        .thenReturn(Optional.of(existingArticle));

    boolean result = validator.isValid(title, context);

    assertThat(result).isFalse();
  }

  @Test
  void isValid_uniqueTitle_queriesWithCorrectSlug() {
    String title = "Test Article Title";
    String expectedSlug = "test-article-title";
    when(articleQueryService.findBySlug(any(String.class), eq(null))).thenReturn(Optional.empty());

    validator.isValid(title, context);

    verify(articleQueryService).findBySlug(expectedSlug, null);
  }

  @Test
  void isValid_titleWithSpecialCharacters_convertsToSlug() {
    String title = "Article with Special! Characters?";
    when(articleQueryService.findBySlug(any(String.class), eq(null))).thenReturn(Optional.empty());

    boolean result = validator.isValid(title, context);

    assertThat(result).isTrue();
    verify(articleQueryService).findBySlug(any(String.class), eq(null));
  }

  @Test
  void isValid_emptyTitle_returnsTrue() {
    String title = "";
    when(articleQueryService.findBySlug(any(String.class), eq(null))).thenReturn(Optional.empty());

    boolean result = validator.isValid(title, context);

    assertThat(result).isTrue();
  }
}
