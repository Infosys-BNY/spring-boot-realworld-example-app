package io.spring.application.data;

import lombok.Value;

@Value
public class ArticleBookmarkCount {
  private String id;
  private Integer count;
}
