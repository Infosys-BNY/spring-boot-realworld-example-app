package io.spring.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

class DateTimeCursorTest {

  @Test
  void constructor_validDateTime_storesData() {
    DateTime dateTime = new DateTime(2025, 10, 1, 12, 0, 0, DateTimeZone.UTC);

    DateTimeCursor cursor = new DateTimeCursor(dateTime);

    assertThat(cursor.getData()).isEqualTo(dateTime);
  }

  @Test
  void toString_validDateTime_returnsMillisAsString() {
    DateTime dateTime = new DateTime(2025, 10, 1, 12, 0, 0, DateTimeZone.UTC);
    long expectedMillis = dateTime.getMillis();

    DateTimeCursor cursor = new DateTimeCursor(dateTime);

    assertThat(cursor.toString()).isEqualTo(String.valueOf(expectedMillis));
  }

  @Test
  void parse_validCursorString_returnsDateTime() {
    DateTime originalDateTime = new DateTime(2025, 10, 1, 12, 0, 0, DateTimeZone.UTC);
    String cursorString = String.valueOf(originalDateTime.getMillis());

    DateTime parsedDateTime = DateTimeCursor.parse(cursorString);

    assertThat(parsedDateTime).isNotNull();
    assertThat(parsedDateTime.getMillis()).isEqualTo(originalDateTime.getMillis());
    assertThat(parsedDateTime.getZone()).isEqualTo(DateTimeZone.UTC);
  }

  @Test
  void parse_nullCursor_returnsNull() {
    DateTime result = DateTimeCursor.parse(null);

    assertThat(result).isNull();
  }

  @Test
  void parse_validCursor_returnsUTCTimezone() {
    String cursorString = String.valueOf(System.currentTimeMillis());

    DateTime parsedDateTime = DateTimeCursor.parse(cursorString);

    assertThat(parsedDateTime).isNotNull();
    assertThat(parsedDateTime.getZone()).isEqualTo(DateTimeZone.UTC);
  }

  @Test
  void toString_andParse_roundTrip() {
    DateTime originalDateTime = new DateTime(2025, 10, 1, 12, 0, 0, DateTimeZone.UTC);
    DateTimeCursor cursor = new DateTimeCursor(originalDateTime);

    String cursorString = cursor.toString();
    DateTime parsedDateTime = DateTimeCursor.parse(cursorString);

    assertThat(parsedDateTime.getMillis()).isEqualTo(originalDateTime.getMillis());
  }
}
