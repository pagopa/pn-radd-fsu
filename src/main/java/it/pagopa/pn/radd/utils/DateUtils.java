package it.pagopa.pn.radd.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {

    private static final ZoneId italianZoneId =  ZoneId.of("Europe/Rome");

    private DateUtils(){}


    public static String formatDate(Instant instant)
    {
        if (instant == null)
            return null;

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
        return LocalDate.ofInstant(instant, italianZoneId).format(formatter);
    }

    public static String formatDate(Date date)
    {
        if (date == null)
            return null;
        LocalDateTime dateTime =  LocalDateTime.ofInstant(date.toInstant(), italianZoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

        return dateTime.format(formatter);
    }

    public static String formatTime(ZonedDateTime datetime)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        return datetime.format(formatter.withZone(italianZoneId));
    }


    public static LocalDate getLocalDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        return LocalDate.parse(date, formatter);
    }

    public static ZonedDateTime parseDate(String date)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
        LocalDate locdate = LocalDate.parse(date, formatter);

        return locdate.atStartOfDay(italianZoneId);
    }

    public static ZonedDateTime atStartOfDay(Instant instant)
    {
        LocalDate locdate = LocalDate.ofInstant(instant, italianZoneId);
        return locdate.atStartOfDay(italianZoneId);
    }

    public static ZonedDateTime parseTime(String date)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        return formatter.parse(date, ZonedDateTime::from);
    }
}
