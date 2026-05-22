package com.smartparking.backend.controller;

import com.smartparking.backend.dto.AuthResponse;
import com.smartparking.backend.dto.LoginRequest;
import com.smartparking.backend.dto.RegisterRequest;
import com.smartparking.backend.dto.UsuarioResponse;
import com.smartparking.backend.model.Usuario;
import com.smartparking.backend.security.jwt.JwtService;
import com.smartparking.backend.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/auth", "/auth"})
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public AuthController(UsuarioService usuarioService,
                          JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    //  REGISTER
    @PostMapping("/register")
    public UsuarioResponse register(@RequestBody RegisterRequest request) {
        log.info("Register request recibido: {}", request);
        Usuario usuario = usuarioService.registrar(request);
        return UsuarioResponse.fromUsuario(usuario);
    }

    //  LOGIN
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {

        Usuario usuario = usuarioService.login(request.getEmail(), request.getPassword());
        String rol = normalizarRol(usuario.getRol());
        UserDetails userDetails = new User(
                usuario.getEmail(),
                usuario.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + rol))
        );
        String token = jwtService.generateToken(Map.of("rol", rol), userDetails);
        usuario.setRol(rol);
        UsuarioResponse usuarioResponse = UsuarioResponse.fromUsuario(usuario);

        return new AuthResponse(
                token,
                usuarioResponse,
                rol
        );
    }

    // (Opcional pero útil para pruebas)
    @GetMapping("/usuarios")
    public Object listar() {
        return usuarioService.listar();
    }

    private String normalizarRol(String rol) {
        if (rol == null || rol.isBlank()) {
            return "USER";
        }

        return rol.trim().toUpperCase().replace("ROLE_", "");
    }
}
