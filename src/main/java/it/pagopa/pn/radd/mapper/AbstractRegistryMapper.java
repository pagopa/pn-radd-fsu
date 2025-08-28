package it.pagopa.pn.radd.mapper;

import lombok.CustomLog;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;

@CustomLog
public abstract class AbstractRegistryMapper {

    protected Instant toInstant(Date date) {
        return Optional.ofNullable(date)
                .map(Date::toInstant)
                .orElse(null);
    }

    protected Date toDate(Instant instant) {
        return Optional.ofNullable(instant)
                .map(Date::from)
                .orElse(null);
    }

    protected String toStringDate(Instant instant) {
        return Optional.ofNullable(instant)
                .map(i -> OffsetDateTime.ofInstant(i, ZoneOffset.UTC).toLocalDate().toString())
                .orElse(null);
    }

    protected Instant parseDateString(String dateStr) {
        try {
            return Optional.ofNullable(dateStr)
                    .map(LocalDate::parse)
                    .map(d -> d.atStartOfDay().toInstant(ZoneOffset.UTC))
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

}
