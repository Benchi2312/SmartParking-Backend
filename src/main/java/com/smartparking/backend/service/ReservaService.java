package com.smartparking.backend.service;

import com.smartparking.backend.dto.ReservaRequest;
import com.smartparking.backend.model.Reserva;

import java.util.List;

public interface ReservaService {
    List<Reserva> listarTodos();
    List<Reserva> listarPorUsuario(Long usuarioId);
    Reserva crear(ReservaRequest request);
}
