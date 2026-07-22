package com.appbasevaadin.msusuarios.service;

import com.appbasevaadin.msusuarios.dto.UsuarioRequest;
import com.appbasevaadin.msusuarios.entity.TipoUsuario;
import com.appbasevaadin.msusuarios.entity.Usuario;
import com.appbasevaadin.msusuarios.exception.TipoUsuarioNoEncontradoException;
import com.appbasevaadin.msusuarios.exception.UsuarioNoEncontradoException;
import com.appbasevaadin.msusuarios.repository.TipoUsuarioRepository;
import com.appbasevaadin.msusuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TipoUsuarioRepository tipoUsuarioRepository;

    private UsuarioService usuarioService;

    private TipoUsuario tipoUsuario;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, tipoUsuarioRepository);
        tipoUsuario = new TipoUsuario();
        tipoUsuario.setId(1L);
        tipoUsuario.setNombre("Usuario");
    }

    @Test
    void crearGuardaUsuarioActivoPorDefecto() {
        UsuarioRequest request = new UsuarioRequest();
        request.setNombre("Luis");
        request.setApellidos("Perez");
        request.setEmail("luis.perez@example.com");
        request.setTipoUsuarioId(1L);

        when(tipoUsuarioRepository.findById(1L)).thenReturn(Optional.of(tipoUsuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario creado = usuarioService.crear(request);

        assertThat(creado.isActivo()).isTrue();
        assertThat(creado.getEmail()).isEqualTo("luis.perez@example.com");
        assertThat(creado.getTipoUsuario()).isEqualTo(tipoUsuario);
    }

    @Test
    void crearConTipoUsuarioInexistenteLanzaExcepcion() {
        UsuarioRequest request = new UsuarioRequest();
        request.setNombre("Luis");
        request.setApellidos("Perez");
        request.setEmail("luis.perez@example.com");
        request.setTipoUsuarioId(99L);

        when(tipoUsuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.crear(request))
                .isInstanceOf(TipoUsuarioNoEncontradoException.class);
    }

    @Test
    void obtenerPorIdInexistenteLanzaExcepcion() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.obtenerPorId(1L))
                .isInstanceOf(UsuarioNoEncontradoException.class);
    }

    @Test
    void eliminarMarcaUsuarioComoInactivo() {
        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setActivo(true);
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        usuarioService.eliminar(5L);

        assertThat(usuario.isActivo()).isFalse();
        verify(usuarioRepository).save(usuario);
    }
}
