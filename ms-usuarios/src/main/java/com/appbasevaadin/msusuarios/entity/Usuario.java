package com.appbasevaadin.msusuarios.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, columnDefinition = "varchar(100)")
    private String nombre;

    @Column(name = "apellidos", nullable = false, columnDefinition = "varchar(100)")
    private String apellidos;

    @Column(name = "email", nullable = false, unique = true, columnDefinition = "varchar(150)")
    private String email;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_usuario_id", nullable = false)
    private TipoUsuario tipoUsuario;

    @jakarta.persistence.PrePersist
    void alPersistir() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
