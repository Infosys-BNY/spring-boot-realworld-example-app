package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.CommentData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisCommentRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import java.util.Arrays;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({
  MyBatisArticleRepository.class,
  MyBatisUserRepository.class,
  MyBatisCommentRepository.class
})
public class CommentReadServiceTest extends DbTestBase {
  @Autowired private CommentReadService commentReadService;
  @Autowired private ArticleRepository articleRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private CommentRepository commentRepository;

  private User user;
  private Article article;
  private Comment comment1;
  private Comment comment2;

  @BeforeEach
  public void setUp() {
    user = new User("test@example.com", "testuser", "password", "bio", "image");
    userRepository.save(user);

    article =
        new Article("Test Article", "description", "body", Arrays.asList("java"), user.getId());
    articleRepository.save(article);

    comment1 = new Comment("First comment", user.getId(), article.getId());
    comment2 = new Comment("Second comment", user.getId(), article.getId());
    commentRepository.save(comment1);
    commentRepository.save(comment2);
  }

  @Test
  public void findById_existingComment_returnsCommentData() {
    CommentData commentData = commentReadService.findById(comment1.getId());

    Assertions.assertNotNull(commentData);
    Assertions.assertEquals(comment1.getId(), commentData.getId());
    Assertions.assertEquals("First comment", commentData.getBody());
    Assertions.assertNotNull(commentData.getProfileData());
    Assertions.assertEquals("testuser", commentData.getProfileData().getUsername());
  }

  @Test
  public void findById_nonExistingComment_returnsNull() {
    CommentData commentData = commentReadService.findById("nonexistent-id");

    Assertions.assertNull(commentData);
  }

  @Test
  public void findByArticleId_existingArticle_returnsAllComments() {
    List<CommentData> comments = commentReadService.findByArticleId(article.getId());

    Assertions.assertNotNull(comments);
    Assertions.assertEquals(2, comments.size());

    boolean hasComment1 = comments.stream().anyMatch(c -> c.getId().equals(comment1.getId()));
    boolean hasComment2 = comments.stream().anyMatch(c -> c.getId().equals(comment2.getId()));
    Assertions.assertTrue(hasComment1);
    Assertions.assertTrue(hasComment2);
  }

  @Test
  public void findByArticleId_articleWithNoComments_returnsEmptyList() {
    Article emptyArticle =
        new Article("Empty Article", "desc", "body", Arrays.asList("test"), user.getId());
    articleRepository.save(emptyArticle);

    List<CommentData> comments = commentReadService.findByArticleId(emptyArticle.getId());

    Assertions.assertNotNull(comments);
    Assertions.assertTrue(comments.isEmpty());
  }

  @Test
  public void findByArticleId_nonExistingArticle_returnsEmptyList() {
    List<CommentData> comments = commentReadService.findByArticleId("nonexistent-article-id");

    Assertions.assertNotNull(comments);
    Assertions.assertTrue(comments.isEmpty());
  }

  @Test
  public void findByArticleIdWithCursor_firstPage_returnsLimitedComments() {
    Comment comment3 = new Comment("Third comment", user.getId(), article.getId());
    commentRepository.save(comment3);

    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 2, Direction.NEXT);
    List<CommentData> comments =
        commentReadService.findByArticleIdWithCursor(article.getId(), page);

    Assertions.assertNotNull(comments);
    Assertions.assertTrue(comments.size() <= page.getQueryLimit());
  }

  @Test
  public void findByArticleIdWithCursor_withCursor_returnsCommentsAfterCursor() {
    Comment comment3 = new Comment("Third comment", user.getId(), article.getId());
    commentRepository.save(comment3);

    DateTime cursor = comment2.getCreatedAt();
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(cursor, 10, Direction.NEXT);
    List<CommentData> comments =
        commentReadService.findByArticleIdWithCursor(article.getId(), page);

    Assertions.assertNotNull(comments);
    for (CommentData comment : comments) {
      Assertions.assertTrue(
          comment.getCreatedAt().isAfter(cursor) || comment.getCreatedAt().equals(cursor));
    }
  }
}
