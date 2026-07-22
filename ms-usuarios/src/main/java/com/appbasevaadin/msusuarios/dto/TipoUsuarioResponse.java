package com.appbasevaadin.msusuarios.dto;

import com.appbasevaadin.msusuarios.entity.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TipoUsuarioResponse {

    private final Long id;
    private final String nombre;
    private final String descripcion;

    public static TipoUsuarioResponse desde(TipoUsuario tipoUsuario) {
        return new TipoUsuarioResponse(tipoUsuario.getId(), tipoUsuario.getNombre(), tipoUsuario.getDescripcion());
    }
}
