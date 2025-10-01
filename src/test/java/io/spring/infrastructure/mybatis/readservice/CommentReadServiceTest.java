package io.spring.infrastructure.mybatis.readservice;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.CommentData;
import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.mapper.ArticleMapper;
import io.spring.infrastructure.mybatis.mapper.CommentMapper;
import io.spring.infrastructure.mybatis.mapper.UserMapper;
import java.util.Arrays;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@MybatisTest
public class CommentReadServiceTest {

  @Autowired private CommentReadService commentReadService;

  @Autowired private CommentMapper commentMapper;

  @Autowired private ArticleMapper articleMapper;

  @Autowired private UserMapper userMapper;

  private User author;
  private Article article;
  private Comment comment;

  @BeforeEach
  void setUp() {
    author = new User("test@example.com", "testuser", "password", "", "");
    userMapper.insert(author);

    article =
        new Article("Test Title", "Test Description", "Test Body", Arrays.asList(), author.getId());
    articleMapper.insert(article);

    comment = new Comment("Test comment content", author.getId(), article.getId());
    commentMapper.insert(comment);
  }

  @Test
  void findById_withValidId_returnsCommentData() {
    CommentData result = commentReadService.findById(comment.getId());

    assertNotNull(result);
    assertEquals(comment.getId(), result.getId());
    assertEquals("Test comment content", result.getBody());
  }

  @Test
  void findById_withInvalidId_returnsNull() {
    CommentData result = commentReadService.findById("non-existent-id");

    assertNull(result);
  }

  @Test
  void findByArticleId_withCommentsPresent_returnsCommentList() {
    List<CommentData> results = commentReadService.findByArticleId(article.getId());

    assertNotNull(results);
    assertEquals(1, results.size());
    assertEquals(comment.getId(), results.get(0).getId());
    assertEquals("Test comment content", results.get(0).getBody());
  }

  @Test
  void findByArticleId_withNoComments_returnsEmptyList() {
    Article emptyArticle =
        new Article("Empty Article", "Description", "Body", Arrays.asList(), author.getId());
    articleMapper.insert(emptyArticle);

    List<CommentData> results = commentReadService.findByArticleId(emptyArticle.getId());

    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void findByArticleIdWithCursor_withValidCursor_returnsPaginatedComments() {
    Comment comment2 = new Comment("Second comment", author.getId(), article.getId());
    commentMapper.insert(comment2);

    Comment comment3 = new Comment("Third comment", author.getId(), article.getId());
    commentMapper.insert(comment3);

    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 2, Direction.NEXT);

    List<CommentData> results = commentReadService.findByArticleIdWithCursor(article.getId(), page);

    assertNotNull(results);
  }

  @Test
  void findByArticleIdWithCursor_withPreviousDirection_returnsPaginatedComments() {
    Comment comment2 = new Comment("Second comment", author.getId(), article.getId());
    commentMapper.insert(comment2);

    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(new DateTime().plusHours(1), 10, Direction.PREV);

    List<CommentData> results = commentReadService.findByArticleIdWithCursor(article.getId(), page);

    assertNotNull(results);
  }
}
