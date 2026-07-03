package com.smartparking.backend.service;

import com.smartparking.backend.dto.VehiculoRequest;
import com.smartparking.backend.dto.VehiculoResponse;

import java.util.List;

public interface VehiculoService {
    List<VehiculoResponse> listarTodos();
    List<VehiculoResponse> listarMisVehiculos();
    List<VehiculoResponse> listarPorUsuario(Long usuarioId);
    VehiculoResponse crear(VehiculoRequest request);
    VehiculoResponse actualizar(Long id, VehiculoRequest request);
    void eliminar(Long id);
}
