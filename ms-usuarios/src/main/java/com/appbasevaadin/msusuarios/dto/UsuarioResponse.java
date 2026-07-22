package com.appbasevaadin.msusuarios.dto;

import com.appbasevaadin.msusuarios.entity.Usuario;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UsuarioResponse {

    private final Long id;
    private final String nombre;
    private final String apellidos;
    private final String email;
    private final boolean activo;
    private final LocalDateTime fechaCreacion;
    private final TipoUsuarioResponse tipoUsuario;

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getEmail(),
                usuario.isActivo(),
                usuario.getFechaCreacion(),
                TipoUsuarioResponse.desde(usuario.getTipoUsuario())
        );
    }
}
