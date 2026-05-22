package com.smartparking.backend.dto;

import com.smartparking.backend.model.EstadoEspacio;

public class EspacioRequest {

    private String numero;
    private EstadoEspacio estado;
    private Long vehiculoId;

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public EstadoEspacio getEstado() {
        return estado;
    }

    public void setEstado(EstadoEspacio estado) {
        this.estado = estado;
    }

    public Long getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(Long vehiculoId) {
        this.vehiculoId = vehiculoId;
    }
}
