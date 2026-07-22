package com.appbasevaadin.msusuarios.controller;

import com.appbasevaadin.msusuarios.dto.UsuarioRequest;
import com.appbasevaadin.msusuarios.entity.TipoUsuario;
import com.appbasevaadin.msusuarios.repository.TipoUsuarioRepository;
import com.appbasevaadin.msusuarios.support.PostgresTestContainerBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UsuarioControllerIT extends PostgresTestContainerBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;

    private Long tipoUsuarioId;

    @BeforeEach
    void setUp() {
        tipoUsuarioId = tipoUsuarioRepository.findAll().stream()
                .filter(t -> t.getNombre().equals("Usuario"))
                .findFirst()
                .orElseGet(() -> {
                    TipoUsuario tipo = new TipoUsuario();
                    tipo.setNombre("Usuario");
                    tipo.setDescripcion("Acceso estandar");
                    return tipoUsuarioRepository.save(tipo);
                })
                .getId();
    }

    private UsuarioRequest crearRequestValido() {
        UsuarioRequest request = new UsuarioRequest();
        request.setNombre("Maria");
        request.setApellidos("Lopez");
        request.setEmail("maria.lopez." + System.nanoTime() + "@example.com");
        request.setTipoUsuarioId(tipoUsuarioId);
        return request;
    }

    @Test
    void flujoCompletoCrudDeUsuario() {
        UsuarioRequest request = crearRequestValido();

        ResponseEntity<String> creado = restTemplate.postForEntity("/usuarios", request, String.class);
        assertThat(creado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(creado.getBody()).contains(request.getEmail());
    }

    @Test
    void crearConEmailInvalidoDevuelve400() {
        UsuarioRequest request = crearRequestValido();
        request.setEmail("no-es-un-email");

        ResponseEntity<String> respuesta = restTemplate.postForEntity("/usuarios", request, String.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(respuesta.getBody()).contains("VALIDACION");
    }

    @Test
    void obtenerUsuarioInexistenteDevuelve404() {
        ResponseEntity<String> respuesta = restTemplate.getForEntity("/usuarios/999999", String.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(respuesta.getBody()).contains("NO_ENCONTRADO");
    }

    @Test
    void crearConEmailDuplicadoDevuelve409() {
        UsuarioRequest request = crearRequestValido();
        restTemplate.postForEntity("/usuarios", request, String.class);

        ResponseEntity<String> segundaRespuesta = restTemplate.postForEntity("/usuarios", request, String.class);

        assertThat(segundaRespuesta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void listarTiposUsuarioIncluyeAdministradorYUsuario() {
        ResponseEntity<String> respuesta = restTemplate.getForEntity("/tipos-usuario", String.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).contains("Administrador").contains("Usuario");
    }
}
