package com.appbasevaadin.msusuarios.controller;

import com.appbasevaadin.msusuarios.dto.UsuarioRequest;
import com.appbasevaadin.msusuarios.dto.UsuarioResponse;
import com.appbasevaadin.msusuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse response = UsuarioResponse.desde(usuarioService.crear(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return UsuarioResponse.desde(usuarioService.obtenerPorId(id));
    }

    @GetMapping
    public Page<UsuarioResponse> buscar(@RequestParam(required = false) String texto,
                                         @RequestParam(required = false) Long tipoUsuarioId,
                                         @RequestParam(required = false) Boolean activo,
                                         Pageable pageable) {
        return usuarioService.buscar(texto, tipoUsuarioId, activo, pageable)
                .map(UsuarioResponse::desde);
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        return UsuarioResponse.desde(usuarioService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
