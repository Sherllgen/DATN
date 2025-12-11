package com.project.evgo.user.internal;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for User entity.
 * Internal - not accessible by other modules.
 */
public interface UserRepository extends JpaRepository<User, Long> {
}
