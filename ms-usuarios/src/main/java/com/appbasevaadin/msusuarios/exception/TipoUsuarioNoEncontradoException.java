package com.appbasevaadin.msusuarios.exception;

public class TipoUsuarioNoEncontradoException extends RuntimeException {

    public TipoUsuarioNoEncontradoException(Long id) {
        super("No existe un tipo de usuario con id " + id);
    }
}
