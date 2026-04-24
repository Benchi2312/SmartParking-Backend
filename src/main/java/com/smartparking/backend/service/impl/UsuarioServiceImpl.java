package com.smartparking.backend.service.impl;

import com.smartparking.backend.model.Usuario;
import com.smartparking.backend.repository.UsuarioRepository;
import com.smartparking.backend.service.UsuarioService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Usuario registrar(Usuario usuario) {

        //  Validar si ya existe
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RuntimeException("Correo ya registrado");
        }

        //  Encriptar password
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // Rol por defecto
        usuario.setRol("USER");

        return usuarioRepository.save(usuario);
    }
    @Override
    public Usuario login(String email, String password) {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        return usuario;
    }

    @Override
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }


}