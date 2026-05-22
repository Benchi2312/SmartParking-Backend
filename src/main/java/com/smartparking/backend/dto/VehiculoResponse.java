package com.smartparking.backend.dto;

import com.smartparking.backend.model.Vehiculo;

public class VehiculoResponse {

    private Long id;
    private String marca;
    private String modelo;
    private String placa;
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioEmail;

    public VehiculoResponse(Long id, String marca, String modelo, String placa, Long usuarioId,
                            String usuarioNombre, String usuarioEmail) {
        this.id = id;
        this.marca = marca;
        this.modelo = modelo;
        this.placa = placa;
        this.usuarioId = usuarioId;
        this.usuarioNombre = usuarioNombre;
        this.usuarioEmail = usuarioEmail;
    }

    public static VehiculoResponse fromVehiculo(Vehiculo vehiculo) {
        return new VehiculoResponse(
                vehiculo.getId(),
                vehiculo.getMarca(),
                vehiculo.getModelo(),
                vehiculo.getPlaca(),
                vehiculo.getUsuario().getId(),
                vehiculo.getUsuario().getNombre(),
                vehiculo.getUsuario().getEmail()
        );
    }

    public Long getId() {
        return id;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public String getPlaca() {
        return placa;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public String getUsuarioEmail() {
        return usuarioEmail;
    }
}
