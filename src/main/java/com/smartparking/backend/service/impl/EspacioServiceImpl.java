package com.smartparking.backend.service.impl;

import com.smartparking.backend.dto.EspacioRequest;
import com.smartparking.backend.dto.EspacioResponse;
import com.smartparking.backend.model.Espacio;
import com.smartparking.backend.model.EstadoEspacio;
import com.smartparking.backend.model.Vehiculo;
import com.smartparking.backend.repository.EspacioRepository;
import com.smartparking.backend.repository.VehiculoRepository;
import com.smartparking.backend.service.EspacioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EspacioServiceImpl implements EspacioService {

    private final EspacioRepository espacioRepository;
    private final VehiculoRepository vehiculoRepository;

    public EspacioServiceImpl(EspacioRepository espacioRepository,
                              VehiculoRepository vehiculoRepository) {
        this.espacioRepository = espacioRepository;
        this.vehiculoRepository = vehiculoRepository;
    }

    @Override
    public List<EspacioResponse> listarTodos() {
        return espacioRepository.listarTodosConVehiculo()
                .stream()
                .map(EspacioResponse::fromEspacio)
                .toList();
    }

    @Override
    @Transactional
    public EspacioResponse crear(EspacioRequest request) {
        validarRequest(request);
        String numero = normalizarNumero(request.getNumero());

        if (espacioRepository.existeNumero(numero)) {
            throw new IllegalArgumentException("Ya existe un espacio con ese numero");
        }

        Espacio espacio = new Espacio();
        espacio.setNumero(numero);
        aplicarEstadoYVehiculo(espacio, request);

        return EspacioResponse.fromEspacio(espacioRepository.save(espacio));
    }

    @Override
    @Transactional
    public EspacioResponse actualizar(Long id, EspacioRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("El id del espacio es obligatorio");
        }

        validarRequest(request);
        String numero = normalizarNumero(request.getNumero());

        if (espacioRepository.existeNumeroEnOtroEspacio(numero, id)) {
            throw new IllegalArgumentException("Ya existe otro espacio con ese numero");
        }

        Espacio espacio = espacioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Espacio no encontrado"));

        espacio.setNumero(numero);
        aplicarEstadoYVehiculo(espacio, request);

        return EspacioResponse.fromEspacio(espacioRepository.save(espacio));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id del espacio es obligatorio");
        }

        if (!espacioRepository.existsById(id)) {
            throw new RuntimeException("Espacio no encontrado");
        }

        espacioRepository.deleteById(id);
    }

    private void aplicarEstadoYVehiculo(Espacio espacio, EspacioRequest request) {
        Vehiculo vehiculo = null;

        if (request.getVehiculoId() != null) {
            vehiculo = vehiculoRepository.findById(request.getVehiculoId())
                    .orElseThrow(() -> new RuntimeException("Vehiculo no encontrado"));
        }

        EstadoEspacio estado = request.getEstado() == null ? EstadoEspacio.LIBRE : request.getEstado();

        if (estado == EstadoEspacio.LIBRE) {
            vehiculo = null;
        }

        if ((estado == EstadoEspacio.OCUPADO || estado == EstadoEspacio.RESERVADO) && vehiculo == null) {
            throw new IllegalArgumentException("Selecciona un vehiculo para ocupar o reservar el espacio");
        }

        espacio.setEstado(estado);
        espacio.setVehiculo(vehiculo);
    }

    private void validarRequest(EspacioRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Los datos del espacio son obligatorios");
        }

        if (request.getNumero() == null || request.getNumero().trim().isEmpty()) {
            throw new IllegalArgumentException("El numero del espacio es obligatorio");
        }
    }

    private String normalizarNumero(String numero) {
        return numero.trim().toUpperCase();
    }
}
