package com.smartparking.backend.service;

import com.smartparking.backend.dto.ReservaRequest;
import com.smartparking.backend.dto.ReservaResponse;

import java.util.List;

public interface ReservaService {
    List<ReservaResponse> listarTodos();
    List<ReservaResponse> listarMisReservas();
    List<ReservaResponse> listarPendientes();
    List<ReservaResponse> listarPorUsuario(Long usuarioId);
    ReservaResponse crear(ReservaRequest request);
    ReservaResponse cambiarEstado(Long id, String estado);
    ReservaResponse aprobar(Long id);
    ReservaResponse rechazar(Long id);
    ReservaResponse obtenerUltimaReserva();
}
