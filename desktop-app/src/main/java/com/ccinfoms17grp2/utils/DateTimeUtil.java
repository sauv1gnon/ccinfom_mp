package com.ccinfoms17grp2.utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public final class DateTimeUtil {

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private DateTimeUtil() {
    }

    public static Timestamp toTimestamp(LocalDateTime dateTime) {
        return dateTime == null ? null : Timestamp.valueOf(dateTime);
    }

    public static LocalDateTime fromTimestamp(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? "" : DEFAULT_FORMATTER.format(dateTime);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return format(dateTime);
    }

    public static LocalDateTime parse(String value) {
        Objects.requireNonNull(value, "value");
        return LocalDateTime.parse(value, DEFAULT_FORMATTER);
    }
}
