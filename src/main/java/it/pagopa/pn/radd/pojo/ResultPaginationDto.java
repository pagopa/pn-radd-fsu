package it.pagopa.pn.radd.pojo;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class ResultPaginationDto<T,K> {
    private List<T> resultsPage;
    private boolean moreResult;
    private List<K> nextPagesKey;
}
