package com.appbasevaadin.msusers.repository;

import com.appbasevaadin.msusers.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Override
    @EntityGraph(attributePaths = "userType")
    Optional<User> findById(Long id);

    @EntityGraph(attributePaths = "userType")
    Optional<User> findByEmailIgnoreCase(String email);

    @Query("""
            SELECT u FROM User u
            JOIN FETCH u.userType t
            WHERE (:text IS NULL OR
                   LOWER(u.firstName) LIKE LOWER(CONCAT('%', CAST(:text AS string), '%')) OR
                   LOWER(u.lastName) LIKE LOWER(CONCAT('%', CAST(:text AS string), '%')) OR
                   LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:text AS string), '%')))
              AND (:userTypeId IS NULL OR t.id = :userTypeId)
              AND (:active IS NULL OR u.active = :active)
            """)
    Page<User> search(@Param("text") String text,
                       @Param("userTypeId") Long userTypeId,
                       @Param("active") Boolean active,
                       Pageable pageable);
}
