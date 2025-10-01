package io.spring.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.application.TagsQueryService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagDatafetcherTest {
  @Mock private TagsQueryService tagsQueryService;
  @InjectMocks private TagDatafetcher tagDatafetcher;

  @Test
  void getTags_existingTags_returnsList() {
    List<String> expectedTags = Arrays.asList("java", "spring", "testing");
    when(tagsQueryService.allTags()).thenReturn(expectedTags);

    List<String> result = tagDatafetcher.getTags();

    assertThat(result).isNotNull();
    assertThat(result).hasSize(3);
    assertThat(result).containsExactly("java", "spring", "testing");
    verify(tagsQueryService).allTags();
  }

  @Test
  void getTags_emptyTags_returnsEmptyList() {
    when(tagsQueryService.allTags()).thenReturn(Collections.emptyList());

    List<String> result = tagDatafetcher.getTags();

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
    verify(tagsQueryService).allTags();
  }

  @Test
  void getTags_singleTag_returnsSingleElementList() {
    List<String> expectedTags = Collections.singletonList("graphql");
    when(tagsQueryService.allTags()).thenReturn(expectedTags);

    List<String> result = tagDatafetcher.getTags();

    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    assertThat(result).containsExactly("graphql");
  }

  @Test
  void getTags_multipleTags_returnsAllTags() {
    List<String> expectedTags = Arrays.asList("java", "kotlin", "spring", "graphql", "rest", "api");
    when(tagsQueryService.allTags()).thenReturn(expectedTags);

    List<String> result = tagDatafetcher.getTags();

    assertThat(result).isNotNull();
    assertThat(result).hasSize(6);
    assertThat(result).containsExactlyInAnyOrder("java", "kotlin", "spring", "graphql", "rest", "api");
  }

  @Test
  void getTags_duplicateTags_returnsAsProvided() {
    List<String> expectedTags = Arrays.asList("java", "java", "spring");
    when(tagsQueryService.allTags()).thenReturn(expectedTags);

    List<String> result = tagDatafetcher.getTags();

    assertThat(result).isNotNull();
    assertThat(result).hasSize(3);
    assertThat(result).containsExactly("java", "java", "spring");
  }

  @Test
  void getTags_callsServiceExactlyOnce() {
    List<String> expectedTags = Arrays.asList("tag1", "tag2");
    when(tagsQueryService.allTags()).thenReturn(expectedTags);

    tagDatafetcher.getTags();

    verify(tagsQueryService).allTags();
  }
}
