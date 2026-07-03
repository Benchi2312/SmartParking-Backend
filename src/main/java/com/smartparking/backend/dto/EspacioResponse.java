package com.smartparking.backend.dto;

import com.smartparking.backend.model.Espacio;
import com.smartparking.backend.model.Vehiculo;

public class EspacioResponse {

    private Long id;
    private String numero;
    private String estado;
    private VehiculoResponse vehiculo;
    private UsuarioResponse usuario;

    public EspacioResponse(Long id, String numero, String estado, VehiculoResponse vehiculo, UsuarioResponse usuario) {
        this.id = id;
        this.numero = numero;
        this.estado = estado;
        this.vehiculo = vehiculo;
        this.usuario = usuario;
    }

    public static EspacioResponse fromEspacio(Espacio espacio) {
        Vehiculo vehiculoEntity = espacio.getVehiculo();

        VehiculoResponse vehiculo = null;
        UsuarioResponse usuario = null;

        if (vehiculoEntity != null) {
            vehiculo = VehiculoResponse.fromVehiculo(vehiculoEntity);
            usuario = UsuarioResponse.fromUsuario(vehiculoEntity.getUsuario());
        }

        return new EspacioResponse(
                espacio.getId(),
                espacio.getNumero(),
                espacio.getEstado().name(),
                vehiculo,
                usuario
        );
    }

    public Long getId() { return id; }
    public String getNumero() { return numero; }
    public String getEstado() { return estado; }
    public VehiculoResponse getVehiculo() { return vehiculo; }
    public UsuarioResponse getUsuario() { return usuario; }
}
