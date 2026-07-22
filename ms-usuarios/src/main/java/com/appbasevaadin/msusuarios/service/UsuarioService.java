package com.appbasevaadin.msusuarios.service;

import com.appbasevaadin.msusuarios.dto.UsuarioRequest;
import com.appbasevaadin.msusuarios.entity.TipoUsuario;
import com.appbasevaadin.msusuarios.entity.Usuario;
import com.appbasevaadin.msusuarios.exception.TipoUsuarioNoEncontradoException;
import com.appbasevaadin.msusuarios.exception.UsuarioNoEncontradoException;
import com.appbasevaadin.msusuarios.repository.TipoUsuarioRepository;
import com.appbasevaadin.msusuarios.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TipoUsuarioRepository tipoUsuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, TipoUsuarioRepository tipoUsuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.tipoUsuarioRepository = tipoUsuarioRepository;
    }

    public Usuario crear(UsuarioRequest request) {
        Usuario usuario = new Usuario();
        aplicarDatos(usuario, request);
        usuario.setActivo(request.getActivo() == null || request.getActivo());
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    @Transactional(readOnly = true)
    public Page<Usuario> buscar(String texto, Long tipoUsuarioId, Boolean activo, Pageable pageable) {
        return usuarioRepository.buscar(texto, tipoUsuarioId, activo, pageable);
    }

    public Usuario actualizar(Long id, UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
        aplicarDatos(usuario, request);
        if (request.getActivo() != null) {
            usuario.setActivo(request.getActivo());
        }
        return usuarioRepository.save(usuario);
    }

    public void eliminar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    private void aplicarDatos(Usuario usuario, UsuarioRequest request) {
        TipoUsuario tipoUsuario = tipoUsuarioRepository.findById(request.getTipoUsuarioId())
                .orElseThrow(() -> new TipoUsuarioNoEncontradoException(request.getTipoUsuarioId()));
        usuario.setNombre(request.getNombre());
        usuario.setApellidos(request.getApellidos());
        usuario.setEmail(request.getEmail());
        usuario.setTipoUsuario(tipoUsuario);
    }
}
