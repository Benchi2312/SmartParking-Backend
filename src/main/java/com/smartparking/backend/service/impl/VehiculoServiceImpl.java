package com.smartparking.backend.service.impl;

import com.smartparking.backend.dto.VehiculoRequest;
import com.smartparking.backend.dto.VehiculoResponse;
import com.smartparking.backend.model.Usuario;
import com.smartparking.backend.model.Vehiculo;
import com.smartparking.backend.repository.UsuarioRepository;
import com.smartparking.backend.repository.VehiculoRepository;
import com.smartparking.backend.service.VehiculoService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Service
public class VehiculoServiceImpl implements VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final UsuarioRepository usuarioRepository;

    public VehiculoServiceImpl(VehiculoRepository vehiculoRepository,
                               UsuarioRepository usuarioRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<VehiculoResponse> listarTodos() {
        return vehiculoRepository.listarTodosConUsuario()
                .stream()
                .map(VehiculoResponse::fromVehiculo)
                .toList();
    }

    @Override
    public List<VehiculoResponse> listarPorUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El usuarioId es obligatorio");
        }

        return vehiculoRepository.buscarPorUsuario(usuarioId)
                .stream()
                .map(VehiculoResponse::fromVehiculo)
                .toList();
    }

    @Override
    @Transactional
    public VehiculoResponse crear(VehiculoRequest request) {
        validarRequest(request);
        String placa = normalizarPlaca(request.getPlaca());

        if (vehiculoRepository.existePlaca(placa)) {
            throw new IllegalArgumentException("Ya existe un vehiculo registrado con esa placa");
        }

        Long usuarioId = request.getUsuarioId() != null ? request.getUsuarioId() : obtenerUsuarioAutenticado().getId();

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setMarca(request.getMarca().trim());
        vehiculo.setModelo(request.getModelo().trim());
        vehiculo.setPlaca(placa);
        vehiculo.setUsuario(usuario);

        return VehiculoResponse.fromVehiculo(vehiculoRepository.save(vehiculo));
    }

    @Override
    @Transactional
    public VehiculoResponse actualizar(Long id, VehiculoRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("El id del vehiculo es obligatorio");
        }

        validarRequest(request);
        String placa = normalizarPlaca(request.getPlaca());

        if (vehiculoRepository.existePlacaEnOtroVehiculo(placa, id)) {
            throw new IllegalArgumentException("Ya existe otro vehiculo registrado con esa placa");
        }

        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehiculo no encontrado"));

        vehiculo.setMarca(request.getMarca().trim());
        vehiculo.setModelo(request.getModelo().trim());
        vehiculo.setPlaca(placa);

        return VehiculoResponse.fromVehiculo(vehiculoRepository.save(vehiculo));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id del vehiculo es obligatorio");
        }

        if (!vehiculoRepository.existsById(id)) {
            throw new RuntimeException("Vehiculo no encontrado");
        }

        vehiculoRepository.deleteById(id);
    }

    private void validarRequest(VehiculoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Los datos del vehiculo son obligatorios");
        }

        if (estaVacio(request.getPlaca())) {
            throw new IllegalArgumentException("La placa es obligatoria");
        }

        if (estaVacio(request.getMarca())) {
            throw new IllegalArgumentException("La marca es obligatoria");
        }

        if (estaVacio(request.getModelo())) {
            throw new IllegalArgumentException("El modelo es obligatorio");
        }
    }

    private boolean estaVacio(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizarPlaca(String placa) {
        return placa.trim().toUpperCase();
    }

    private Usuario obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("No se encontro el usuario autenticado");
        }

        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));
    }
}
