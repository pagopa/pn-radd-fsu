package it.pagopa.pn.radd.pojo;

import lombok.Data;

import java.time.Instant;

@Data
public class TimeInterval {
    private final Instant start;
    private final Instant end;
}