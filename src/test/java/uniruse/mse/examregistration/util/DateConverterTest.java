package uniruse.mse.examregistration.util;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Date;

import org.junit.Test;

public class DateConverterTest {
	// JavaScript representation: 2018-06-24T17:02:52.644Z
	final static Long TEST_TIMESTAMP = 1529848972644L;

	@Test
	public void should_convertFromTimestampToLocalDateTime() {
		final LocalDateTime fromTimestamp = DateConverter.fromTimestamp(TEST_TIMESTAMP);

		assertEquals(2018, fromTimestamp.getYear());
		assertEquals(Month.JUNE, fromTimestamp.getMonth());
		assertEquals(24, fromTimestamp.getDayOfMonth());
		assertEquals(17, fromTimestamp.getHour());
		assertEquals(2, fromTimestamp.getMinute());
		assertEquals(52, fromTimestamp.getSecond());
		assertEquals(644, fromTimestamp.getNano() / 1000000);
	}

	@Test
	public void should_convertFromLocalDateTimeToTimestamp() {
		final LocalDateTime date = LocalDateTime.of(2018, Month.JUNE, 24, 17, 2, 52, 644 * 1000000);

		assertEquals(TEST_TIMESTAMP, DateConverter.toUnixTimestamp(date));
	}

	@Test
	public void should_convertFromLocalDateToDate() {
		final LocalDateTime date = LocalDateTime.of(2018, Month.JUNE, 24, 17, 2, 52, 644 * 1000000);
		final Date converted = new Date(DateConverter.toUnixTimestamp(date));

		assertEquals(TEST_TIMESTAMP, (Long) converted.getTime());
	}
}
