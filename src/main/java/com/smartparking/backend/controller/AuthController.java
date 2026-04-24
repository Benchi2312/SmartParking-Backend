package com.smartparking.backend.controller;

import com.smartparking.backend.dto.AuthResponse;
import com.smartparking.backend.model.Usuario;
import com.smartparking.backend.service.UsuarioService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    //  REGISTER
    @PostMapping("/register")
    public Usuario register(@RequestBody Usuario usuario) {
        return usuarioService.registrar(usuario);
    }

    //  LOGIN
    @PostMapping("/login")
    public AuthResponse login(@RequestBody Usuario request) {

        Usuario usuario = usuarioService.login(request.getEmail(), request.getPassword());

        return new AuthResponse(
                "fake-token",
                usuario.getEmail(),
                usuario.getRol()
        );
    }

    // (Opcional pero útil para pruebas)
    @GetMapping("/usuarios")
    public Object listar() {
        return usuarioService.listar();
    }
}