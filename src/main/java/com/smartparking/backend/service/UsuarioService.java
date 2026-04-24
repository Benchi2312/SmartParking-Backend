package com.smartparking.backend.service;

import com.smartparking.backend.model.Usuario;

import java.util.List;

public interface UsuarioService {

    Usuario registrar(Usuario usuario);
    Usuario login(String email, String password);
    List<Usuario> listar();
}