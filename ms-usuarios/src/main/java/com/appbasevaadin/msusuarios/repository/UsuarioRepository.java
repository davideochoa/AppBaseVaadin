package com.appbasevaadin.msusuarios.repository;

import com.appbasevaadin.msusuarios.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Override
    @EntityGraph(attributePaths = "tipoUsuario")
    Optional<Usuario> findById(Long id);

    @Query("""
            SELECT u FROM Usuario u
            JOIN FETCH u.tipoUsuario t
            WHERE (:texto IS NULL OR
                   LOWER(u.nombre) LIKE LOWER(CONCAT('%', CAST(:texto AS string), '%')) OR
                   LOWER(u.apellidos) LIKE LOWER(CONCAT('%', CAST(:texto AS string), '%')) OR
                   LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:texto AS string), '%')))
              AND (:tipoUsuarioId IS NULL OR t.id = :tipoUsuarioId)
              AND (:activo IS NULL OR u.activo = :activo)
            """)
    Page<Usuario> buscar(@Param("texto") String texto,
                          @Param("tipoUsuarioId") Long tipoUsuarioId,
                          @Param("activo") Boolean activo,
                          Pageable pageable);
}
