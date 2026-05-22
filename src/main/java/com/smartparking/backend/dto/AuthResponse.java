package com.smartparking.backend.dto;

public class AuthResponse {

    private String token;
    private UsuarioResponse usuario;
    private String rol;

    public AuthResponse(String token, UsuarioResponse usuario, String rol) {
        this.token = token;
        this.usuario = usuario;
        this.rol = rol;
    }

    public String getToken() {
        return token;
    }

    public UsuarioResponse getUsuario() {
        return usuario;
    }

    public String getRol() {
        return rol;
    }

    // Getters de compatibilidad para no romper clientes que consumian el login anterior.
    public Long getId() {
        return usuario.getId();
    }

    public String getNombre() {
        return usuario.getNombre();
    }

    public String getEmail() {
        return usuario.getEmail();
    }
}
