package it.pagopa.pn.radd.mapper;

public interface BaseMapperInterface<T,S> {
    S toEntity(T source);
    T toDto(S source);
}
