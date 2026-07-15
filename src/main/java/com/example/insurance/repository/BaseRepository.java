package com.example.insurance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.UUID;

/**
 * Base repository interface.
 * All repository interfaces should extend this.
 */
@NoRepositoryBean
public interface BaseRepository<T> extends JpaRepository<T, UUID> {
    // Base repository methods will be defined here
}
