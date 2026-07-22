package com.appbasevaadin.msusuarios.service;

import com.appbasevaadin.msusuarios.entity.TipoUsuario;
import com.appbasevaadin.msusuarios.repository.TipoUsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TipoUsuarioService {

    private final TipoUsuarioRepository tipoUsuarioRepository;

    public TipoUsuarioService(TipoUsuarioRepository tipoUsuarioRepository) {
        this.tipoUsuarioRepository = tipoUsuarioRepository;
    }

    public List<TipoUsuario> listar() {
        return tipoUsuarioRepository.findAll();
    }
}
