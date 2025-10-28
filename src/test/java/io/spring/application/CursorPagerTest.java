package io.spring.application;

import static org.assertj.core.api.Assertions.assertThat;

import io.spring.application.CursorPager.Direction;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class CursorPagerTest {

  @Test
  void constructor_nextDirectionWithExtra_setsNextTrue() {
    List<TestNode> data = Arrays.asList(new TestNode("1"), new TestNode("2"));

    CursorPager<TestNode> pager = new CursorPager<>(data, Direction.NEXT, true);

    assertThat(pager.hasNext()).isTrue();
    assertThat(pager.hasPrevious()).isFalse();
  }

  @Test
  void constructor_nextDirectionWithoutExtra_setsNextFalse() {
    List<TestNode> data = Arrays.asList(new TestNode("1"), new TestNode("2"));

    CursorPager<TestNode> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertThat(pager.hasNext()).isFalse();
    assertThat(pager.hasPrevious()).isFalse();
  }

  @Test
  void constructor_prevDirectionWithExtra_setsPreviousTrue() {
    List<TestNode> data = Arrays.asList(new TestNode("1"), new TestNode("2"));

    CursorPager<TestNode> pager = new CursorPager<>(data, Direction.PREV, true);

    assertThat(pager.hasNext()).isFalse();
    assertThat(pager.hasPrevious()).isTrue();
  }

  @Test
  void constructor_prevDirectionWithoutExtra_setsPreviousFalse() {
    List<TestNode> data = Arrays.asList(new TestNode("1"), new TestNode("2"));

    CursorPager<TestNode> pager = new CursorPager<>(data, Direction.PREV, false);

    assertThat(pager.hasNext()).isFalse();
    assertThat(pager.hasPrevious()).isFalse();
  }

  @Test
  void getData_returnsProvidedData() {
    List<TestNode> data = Arrays.asList(new TestNode("1"), new TestNode("2"));

    CursorPager<TestNode> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertThat(pager.getData()).isEqualTo(data);
    assertThat(pager.getData()).hasSize(2);
  }

  @Test
  void getStartCursor_withData_returnsFirstNodeCursor() {
    TestNode node1 = new TestNode("1");
    TestNode node2 = new TestNode("2");
    List<TestNode> data = Arrays.asList(node1, node2);

    CursorPager<TestNode> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertThat(pager.getStartCursor()).isEqualTo(node1.getCursor());
  }

  @Test
  void getStartCursor_emptyData_returnsNull() {
    List<TestNode> data = Collections.emptyList();

    CursorPager<TestNode> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertThat(pager.getStartCursor()).isNull();
  }

  @Test
  void getEndCursor_withData_returnsLastNodeCursor() {
    TestNode node1 = new TestNode("1");
    TestNode node2 = new TestNode("2");
    List<TestNode> data = Arrays.asList(node1, node2);

    CursorPager<TestNode> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertThat(pager.getEndCursor()).isEqualTo(node2.getCursor());
  }

  @Test
  void getEndCursor_emptyData_returnsNull() {
    List<TestNode> data = Collections.emptyList();

    CursorPager<TestNode> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertThat(pager.getEndCursor()).isNull();
  }

  @Test
  void getEndCursor_singleElement_returnsSameAsStartCursor() {
    TestNode node = new TestNode("1");
    List<TestNode> data = Collections.singletonList(node);

    CursorPager<TestNode> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertThat(pager.getStartCursor()).isEqualTo(pager.getEndCursor());
  }

  private static class TestNode implements Node {
    private final TestCursor cursor;

    TestNode(String id) {
      this.cursor = new TestCursor(id);
    }

    @Override
    public PageCursor getCursor() {
      return cursor;
    }
  }

  private static class TestCursor extends PageCursor<String> {
    TestCursor(String data) {
      super(data);
    }
  }
}
