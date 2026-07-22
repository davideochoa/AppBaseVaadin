package com.appbasevaadin.msaudit.repository;

import com.appbasevaadin.msaudit.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    @Query("""
            SELECT a FROM AuditEvent a
            WHERE (:type IS NULL OR a.type = CAST(:type AS string))
              AND (:email IS NULL OR LOWER(a.email) = LOWER(CAST(:email AS string)))
            ORDER BY a.occurredAt DESC
            """)
    Page<AuditEvent> search(@Param("type") String type,
                             @Param("email") String email,
                             Pageable pageable);
}
