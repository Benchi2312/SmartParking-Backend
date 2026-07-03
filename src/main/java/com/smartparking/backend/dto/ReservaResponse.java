package com.smartparking.backend.dto;

import com.smartparking.backend.model.Reserva;

import java.time.LocalDate;

public class ReservaResponse {

    private Long id;
    private LocalDate fecha;
    private String estado;
    private UsuarioResponse usuario;
    private VehiculoResponse vehiculo;
    private EspacioResponse espacio;

    public ReservaResponse(Long id, LocalDate fecha, String estado,
                           UsuarioResponse usuario,
                           VehiculoResponse vehiculo,
                           EspacioResponse espacio) {
        this.id = id;
        this.fecha = fecha;
        this.estado = estado;
        this.usuario = usuario;
        this.vehiculo = vehiculo;
        this.espacio = espacio;
    }

    public static ReservaResponse fromReserva(Reserva reserva) {
        return new ReservaResponse(
                reserva.getId(),
                reserva.getFecha(),
                reserva.getEstado().name(),
                UsuarioResponse.fromUsuario(reserva.getUsuario()),
                VehiculoResponse.fromVehiculo(reserva.getVehiculo()),
                reserva.getEspacio() == null ? null : EspacioResponse.fromEspacio(reserva.getEspacio())
        );
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getEstado() {
        return estado;
    }

    public UsuarioResponse getUsuario() {
        return usuario;
    }

    public VehiculoResponse getVehiculo() {
        return vehiculo;
    }

    public EspacioResponse getEspacio() {
        return espacio;
    }
}
