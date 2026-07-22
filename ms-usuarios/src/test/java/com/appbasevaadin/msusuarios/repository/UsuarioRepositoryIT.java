package com.appbasevaadin.msusuarios.repository;

import com.appbasevaadin.msusuarios.entity.TipoUsuario;
import com.appbasevaadin.msusuarios.entity.Usuario;
import com.appbasevaadin.msusuarios.support.PostgresTestContainerBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsuarioRepositoryIT extends PostgresTestContainerBase {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;

    private TipoUsuario tipoAdministrador;

    @BeforeEach
    void setUp() {
        tipoAdministrador = tipoUsuarioRepository.findAll().stream()
                .filter(t -> t.getNombre().equals("Administrador"))
                .findFirst()
                .orElseGet(() -> {
                    TipoUsuario tipo = new TipoUsuario();
                    tipo.setNombre("Administrador");
                    tipo.setDescripcion("Acceso total");
                    return tipoUsuarioRepository.save(tipo);
                });

        Usuario usuario = new Usuario();
        usuario.setNombre("Ana");
        usuario.setApellidos("Garcia");
        usuario.setEmail("ana.garcia@example.com");
        usuario.setTipoUsuario(tipoAdministrador);
        usuarioRepository.save(usuario);
    }

    @Test
    void buscarConFiltroDeTextoNoLanzaErrorDeBytea() {
        assertThatCode(() -> usuarioRepository.buscar("ana", null, null, PageRequest.of(0, 10)))
                .doesNotThrowAnyException();

        Page<Usuario> resultado = usuarioRepository.buscar("ana", null, null, PageRequest.of(0, 10));
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getEmail()).isEqualTo("ana.garcia@example.com");
    }

    @Test
    void buscarConTextoNuloDevuelveTodos() {
        Page<Usuario> resultado = usuarioRepository.buscar(null, null, null, PageRequest.of(0, 10));
        assertThat(resultado.getContent()).isNotEmpty();
    }

    @Test
    void findByIdCargaTipoUsuarioSinLazyException() {
        Usuario guardado = usuarioRepository.findAll().get(0);

        Optional<Usuario> encontrado = usuarioRepository.findById(guardado.getId());

        assertThat(encontrado).isPresent();
        assertThatCode(() -> encontrado.get().getTipoUsuario().getNombre())
                .doesNotThrowAnyException();
    }
}
