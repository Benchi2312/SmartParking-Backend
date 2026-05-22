package com.smartparking.backend.dto;

import com.smartparking.backend.model.Espacio;
import com.smartparking.backend.model.EstadoEspacio;
import com.smartparking.backend.model.Vehiculo;

public class EspacioResponse {

    private Long id;
    private String numero;
    private EstadoEspacio estado;
    private VehiculoResponse vehiculo;

    public EspacioResponse(Long id, String numero, EstadoEspacio estado, VehiculoResponse vehiculo) {
        this.id = id;
        this.numero = numero;
        this.estado = estado;
        this.vehiculo = vehiculo;
    }

    public static EspacioResponse fromEspacio(Espacio espacio) {
        Vehiculo vehiculo = espacio.getVehiculo();

        return new EspacioResponse(
                espacio.getId(),
                espacio.getNumero(),
                espacio.getEstado(),
                vehiculo == null ? null : VehiculoResponse.fromVehiculo(vehiculo)
        );
    }

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public EstadoEspacio getEstado() {
        return estado;
    }

    public VehiculoResponse getVehiculo() {
        return vehiculo;
    }
}
