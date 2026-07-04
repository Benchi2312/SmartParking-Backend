package com.smartparking.backend.service.impl;

import com.smartparking.backend.dto.EspacioRequest;
import com.smartparking.backend.dto.EspacioResponse;
import com.smartparking.backend.model.Espacio;
import com.smartparking.backend.model.EstadoEspacio;
import com.smartparking.backend.model.EstadoReserva;
import com.smartparking.backend.model.Reserva;
import com.smartparking.backend.model.Usuario;
import com.smartparking.backend.model.Vehiculo;
import com.smartparking.backend.repository.EspacioRepository;
import com.smartparking.backend.repository.ReservaRepository;
import com.smartparking.backend.repository.UsuarioRepository;
import com.smartparking.backend.repository.VehiculoRepository;
import com.smartparking.backend.service.EspacioService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EspacioServiceImpl implements EspacioService {

    private final EspacioRepository espacioRepository;
    private final ReservaRepository reservaRepository;
    private final VehiculoRepository vehiculoRepository;
    private final UsuarioRepository usuarioRepository;

    public EspacioServiceImpl(EspacioRepository espacioRepository,
                              ReservaRepository reservaRepository,
                              VehiculoRepository vehiculoRepository,
                              UsuarioRepository usuarioRepository) {
        this.espacioRepository = espacioRepository;
        this.reservaRepository = reservaRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<EspacioResponse> listarTodos() {
        return espacioRepository.listarTodosConVehiculo()
                .stream()
                .map(EspacioResponse::fromEspacio)
                .toList();
    }

    @Override
    public List<EspacioResponse> listarMisEspacios() {
        Usuario usuario = obtenerUsuarioAutenticado();
        return espacioRepository.listarPorUsuarioId(usuario.getId())
                .stream()
                .map(EspacioResponse::fromEspacio)
                .toList();
    }

    @Override
    public List<EspacioResponse> listarDisponibles() {
        return espacioRepository.listarPorEstado(EstadoEspacio.LIBRE)
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
        aplicarEstadoYVehiculo(espacio, request, null);

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
        aplicarEstadoYVehiculo(espacio, request, id);

        return EspacioResponse.fromEspacio(espacioRepository.save(espacio));
    }

    @Override
    @Transactional
    public EspacioResponse liberar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id del espacio es obligatorio");
        }

        Espacio espacio = espacioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Espacio no encontrado"));

        if (espacio.getVehiculo() != null) {
            List<Reserva> reservasActivas = reservaRepository.findByVehiculoIdAndEspacioIdAndEstado(
                    espacio.getVehiculo().getId(),
                    espacio.getId(),
                    EstadoReserva.CONFIRMADA
            );

            for (Reserva reserva : reservasActivas) {
                reserva.setEstado(EstadoReserva.FINALIZADA);
                reservaRepository.save(reserva);
            }
        }

        espacio.setEstado(EstadoEspacio.LIBRE);
        espacio.setVehiculo(null);

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

    private void aplicarEstadoYVehiculo(Espacio espacio, EspacioRequest request, Long espacioId) {
        Vehiculo vehiculo = null;

        if (request.getVehiculoId() != null) {
            vehiculo = vehiculoRepository.findById(request.getVehiculoId())
                    .orElseThrow(() -> new RuntimeException("Vehiculo no encontrado"));
        }

        EstadoEspacio estado = (vehiculo != null) ? EstadoEspacio.OCUPADO : EstadoEspacio.LIBRE;

        if (vehiculo != null) {
            boolean yaTieneEspacio = espacioId == null
                    ? espacioRepository.vehiculoTieneEspacio(vehiculo.getId())
                    : espacioRepository.vehiculoTieneOtroEspacio(vehiculo.getId(), espacioId);

            if (yaTieneEspacio) {
                throw new IllegalArgumentException("El vehiculo ya posee un espacio asignado");
            }
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

    private Usuario obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("No se encontro el usuario autenticado");
        }

        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));
    }
}
