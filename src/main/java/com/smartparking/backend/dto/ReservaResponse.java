package com.smartparking.backend.dto;

import com.smartparking.backend.model.Reserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReservaResponse {

    private Long id;
    private LocalDate fecha;
    private String estado;
    private UsuarioResponse usuario;
    private VehiculoResponse vehiculo;
    private EspacioResponse espacio;
    private String canceladoPor;
    private LocalDateTime horaInicio;
    private LocalDateTime horaFin;
    private BigDecimal costoTotal;

    public ReservaResponse(Long id, LocalDate fecha, String estado,
                           UsuarioResponse usuario,
                           VehiculoResponse vehiculo,
                           EspacioResponse espacio,
                           String canceladoPor,
                           LocalDateTime horaInicio,
                           LocalDateTime horaFin,
                           BigDecimal costoTotal) {
        this.id = id;
        this.fecha = fecha;
        this.estado = estado;
        this.usuario = usuario;
        this.vehiculo = vehiculo;
        this.espacio = espacio;
        this.canceladoPor = canceladoPor;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.costoTotal = costoTotal;
    }

    public static ReservaResponse fromReserva(Reserva reserva) {
        return new ReservaResponse(
                reserva.getId(),
                reserva.getFecha(),
                reserva.getEstado().name(),
                UsuarioResponse.fromUsuario(reserva.getUsuario()),
                VehiculoResponse.fromVehiculo(reserva.getVehiculo()),
                reserva.getEspacio() == null ? null : EspacioResponse.fromEspacio(reserva.getEspacio()),
                reserva.getCanceladoPor(),
                reserva.getHoraInicio(),
                reserva.getHoraFin(),
                reserva.getCostoTotal()
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

    public String getCanceladoPor() {
        return canceladoPor;
    }

    public LocalDateTime getHoraInicio() {
        return horaInicio;
    }

    public LocalDateTime getHoraFin() {
        return horaFin;
    }

    public BigDecimal getCostoTotal() {
        return costoTotal;
    }
}
