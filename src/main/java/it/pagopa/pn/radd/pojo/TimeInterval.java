package it.pagopa.pn.radd.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class TimeInterval {
    private Instant start;
    private Instant end;
}