package com.example.insurance.mapper;

import java.util.List;

/**
 * Base interface for object mappers.
 * All mapper interfaces should extend this.
 *
 * @param <E> Entity type
 * @param <D> DTO type
 */
public interface BaseMapper<E, D> {

    D toDto(E entity);

    E toEntity(D dto);

    List<D> toDtoList(List<E> entities);

    List<E> toEntityList(List<D> dtos);
}
