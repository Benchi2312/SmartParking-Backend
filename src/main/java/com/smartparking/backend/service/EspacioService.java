package com.smartparking.backend.service;

import com.smartparking.backend.dto.EspacioRequest;
import com.smartparking.backend.dto.EspacioResponse;

import java.util.List;

public interface EspacioService {
    List<EspacioResponse> listarTodos();
    List<EspacioResponse> listarDisponibles();
    EspacioResponse crear(EspacioRequest request);
    EspacioResponse actualizar(Long id, EspacioRequest request);
    EspacioResponse liberar(Long id);
    void eliminar(Long id);
}
