package com.appbasevaadin.msusuarios.controller;

import com.appbasevaadin.msusuarios.dto.TipoUsuarioResponse;
import com.appbasevaadin.msusuarios.service.TipoUsuarioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tipos-usuario")
public class TipoUsuarioController {

    private final TipoUsuarioService tipoUsuarioService;

    public TipoUsuarioController(TipoUsuarioService tipoUsuarioService) {
        this.tipoUsuarioService = tipoUsuarioService;
    }

    @GetMapping
    public List<TipoUsuarioResponse> listar() {
        return tipoUsuarioService.listar().stream()
                .map(TipoUsuarioResponse::desde)
                .toList();
    }
}
