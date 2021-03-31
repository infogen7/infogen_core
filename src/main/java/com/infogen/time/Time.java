package com.infogen.time;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class Time {
	public static final ZoneId zoneId = ZoneId.of("Asia/Shanghai");
	public static final ZoneOffset offset = ZoneOffset.of("+08:00");

	public static Long GMT8() {
		return java.time.Clock.system(zoneId).millis();
	}

	public static Long UTC8() {
		return java.time.Clock.system(zoneId).millis();
	}

	public static Timestamp TimestampUTC8() {
		return Timestamp.from(Instant.now(java.time.Clock.system(zoneId)));
	}

	public static Date DateUTC8() {
		return new Date(java.time.Clock.system(zoneId).millis());
	}
}
