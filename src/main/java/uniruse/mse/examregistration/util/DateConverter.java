package uniruse.mse.examregistration.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public final class DateConverter {
	private static ZoneId zone =  ZoneId.systemDefault();

	public static Long toUnixTimestamp(LocalDateTime date) {
		return date.atZone(zone).toInstant().toEpochMilli();
	}

	public static LocalDateTime fromTimestamp(Long timestamp) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zone);
	}

	public static Date fromLocalDateTime(LocalDateTime date) {
	    return java.sql.Timestamp.valueOf(date);
	}

	public static LocalDateTime toLocalDateTime(Date date) {
		return date.toInstant().atZone(zone).toLocalDateTime();
	}
}
