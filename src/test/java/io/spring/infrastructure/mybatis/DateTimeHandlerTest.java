package io.spring.infrastructure.mybatis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DateTimeHandlerTest {

  private DateTimeHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new DateTimeHandler();
  }

  @Test
  public void setParameter_withDateTime_setsTimestamp() throws Exception {
    PreparedStatement ps = mock(PreparedStatement.class);
    DateTime dateTime = new DateTime(2024, 1, 15, 10, 30, 0, DateTimeZone.UTC);

    handler.setParameter(ps, 1, dateTime, null);

    verify(ps).setTimestamp(eq(1), any(Timestamp.class), any());
  }

  @Test
  public void setParameter_withNull_setsNullTimestamp() throws Exception {
    PreparedStatement ps = mock(PreparedStatement.class);

    handler.setParameter(ps, 1, null, null);

    verify(ps).setTimestamp(eq(1), eq(null), any());
  }

  @Test
  public void getResult_withResultSetAndColumnName_returnsDateTime() throws Exception {
    ResultSet rs = mock(ResultSet.class);
    Timestamp timestamp =
        new Timestamp(new DateTime(2024, 1, 15, 10, 30, 0, DateTimeZone.UTC).getMillis());
    when(rs.getTimestamp(eq("created_at"), any())).thenReturn(timestamp);

    DateTime result = handler.getResult(rs, "created_at");

    assertEquals(timestamp.getTime(), result.getMillis());
  }

  @Test
  public void getResult_withResultSetAndColumnName_nullTimestamp_returnsNull() throws Exception {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getTimestamp(eq("created_at"), any())).thenReturn(null);

    DateTime result = handler.getResult(rs, "created_at");

    assertNull(result);
  }

  @Test
  public void getResult_withResultSetAndColumnIndex_returnsDateTime() throws Exception {
    ResultSet rs = mock(ResultSet.class);
    Timestamp timestamp =
        new Timestamp(new DateTime(2024, 1, 15, 10, 30, 0, DateTimeZone.UTC).getMillis());
    when(rs.getTimestamp(eq(1), any())).thenReturn(timestamp);

    DateTime result = handler.getResult(rs, 1);

    assertEquals(timestamp.getTime(), result.getMillis());
  }

  @Test
  public void getResult_withResultSetAndColumnIndex_nullTimestamp_returnsNull() throws Exception {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getTimestamp(eq(1), any())).thenReturn(null);

    DateTime result = handler.getResult(rs, 1);

    assertNull(result);
  }

  @Test
  public void getResult_withCallableStatement_returnsDateTime() throws Exception {
    CallableStatement cs = mock(CallableStatement.class);
    Timestamp timestamp =
        new Timestamp(new DateTime(2024, 1, 15, 10, 30, 0, DateTimeZone.UTC).getMillis());
    when(cs.getTimestamp(eq(1), any())).thenReturn(timestamp);

    DateTime result = handler.getResult(cs, 1);

    assertEquals(timestamp.getTime(), result.getMillis());
  }

  @Test
  public void getResult_withCallableStatement_nullTimestamp_returnsNull() throws Exception {
    CallableStatement cs = mock(CallableStatement.class);
    when(cs.getTimestamp(eq(1), any())).thenReturn(null);

    DateTime result = handler.getResult(cs, 1);

    assertNull(result);
  }

  @Test
  public void setParameter_preservesUTCTime() throws Exception {
    PreparedStatement ps = mock(PreparedStatement.class);
    DateTime utcDateTime = new DateTime(2024, 1, 15, 10, 30, 0, DateTimeZone.UTC);
    Timestamp expectedTimestamp = new Timestamp(utcDateTime.getMillis());

    handler.setParameter(ps, 1, utcDateTime, null);

    verify(ps).setTimestamp(eq(1), eq(expectedTimestamp), any());
  }
}
