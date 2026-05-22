package com.smartparking.backend.service.impl;

import com.smartparking.backend.dto.RegisterRequest;
import com.smartparking.backend.model.Usuario;
import com.smartparking.backend.repository.UsuarioRepository;
import com.smartparking.backend.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Usuario registrar(RegisterRequest request) {
        validarRegistro(request);

        log.info("Password recibido DTO: {}", request.getPassword() == null ? "NULL" : "PRESENTE");

        //  Validar si ya existe
        if (usuarioRepository.findByEmail(request.getEmail().trim()).isPresent()) {
            throw new RuntimeException("Correo ya registrado");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        log.info("Password encoded: {}", encodedPassword == null ? "NULL" : "GENERADO");

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre().trim());
        usuario.setEmail(request.getEmail().trim());
        usuario.setPassword(encodedPassword);
        usuario.setRol("USER");

        log.info("Usuario antes de guardar: email={}, rol={}, passwordPresente={}",
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getPassword() != null && !usuario.getPassword().isBlank());

        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario login(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contrasena es obligatoria");
        }

        Usuario usuario = usuarioRepository.findByEmail(email.trim())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        usuario.setRol(normalizarRol(usuario.getRol()));
        return usuario;
    }

    @Override
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    private void validarRegistro(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Los datos de registro son obligatorios");
        }

        if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contrasena es obligatoria");
        }
    }

    private String normalizarRol(String rol) {
        if (rol == null || rol.isBlank()) {
            return "USER";
        }

        return rol.trim().toUpperCase().replace("ROLE_", "");
    }

}
