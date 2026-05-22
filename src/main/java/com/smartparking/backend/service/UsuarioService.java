package com.smartparking.backend.service;

import com.smartparking.backend.dto.RegisterRequest;
import com.smartparking.backend.model.Usuario;

import java.util.List;

public interface UsuarioService {

    Usuario registrar(RegisterRequest request);
    Usuario login(String email, String password);
    List<Usuario> listar();
}
