package com.smartparking.backend.service;

import com.smartparking.backend.dto.EspacioRequest;
import com.smartparking.backend.dto.EspacioResponse;

import java.util.List;

public interface EspacioService {
    List<EspacioResponse> listarTodos();
    EspacioResponse crear(EspacioRequest request);
    EspacioResponse actualizar(Long id, EspacioRequest request);
    void eliminar(Long id);
}
