package com.smartparking.backend.service.impl;

import com.smartparking.backend.dto.ReservaRequest;
import com.smartparking.backend.dto.ReservaResponse;
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
import com.smartparking.backend.service.ReservaService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final EspacioRepository espacioRepository;

    public ReservaServiceImpl(ReservaRepository reservaRepository,
                              UsuarioRepository usuarioRepository,
                              VehiculoRepository vehiculoRepository,
                              EspacioRepository espacioRepository) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.espacioRepository = espacioRepository;
    }

    @Override
    public List<ReservaResponse> listarTodos() {
        return reservaRepository.findAll()
                .stream()
                .map(ReservaResponse::fromReserva)
                .toList();
    }

    @Override
    public List<ReservaResponse> listarMisReservas() {
        Usuario usuario = obtenerUsuarioAutenticado();
        return reservaRepository.findByUsuarioId(usuario.getId())
                .stream()
                .map(ReservaResponse::fromReserva)
                .toList();
    }

    @Override
    public List<ReservaResponse> listarPendientes() {
        validarAdmin();
        return reservaRepository.findByEstadoOrderByFechaAscIdAsc(EstadoReserva.PENDIENTE)
                .stream()
                .map(ReservaResponse::fromReserva)
                .toList();
    }

    @Override
    public ReservaResponse obtenerUltimaReserva() {
        Usuario usuario = obtenerUsuarioAutenticado();
        return reservaRepository.findFirstByUsuarioIdOrderByIdDesc(usuario.getId())
                .map(ReservaResponse::fromReserva)
                .orElse(null);
    }

    @Override
    public List<ReservaResponse> listarPorUsuario(Long usuarioId) {
        return reservaRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(ReservaResponse::fromReserva)
                .toList();
    }

    @Override
    @Transactional
    public ReservaResponse crear(ReservaRequest request) {
        validarCreacion(request);

        Usuario usuario = obtenerUsuarioAutenticado();

        Vehiculo vehiculo = vehiculoRepository.findById(request.getVehiculoId())
                .orElseThrow(() -> new RuntimeException("Vehiculo no encontrado"));

        if (!vehiculo.getUsuario().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("Solo puedes reservar con tus propios vehiculos");
        }

        Espacio espacio = espacioRepository.findById(request.getEspacioId())
                .orElseThrow(() -> new RuntimeException("Espacio no encontrado"));

        if (espacio.getEstado() != EstadoEspacio.LIBRE || espacio.getVehiculo() != null) {
            throw new IllegalArgumentException("El espacio seleccionado ya no esta disponible");
        }

        if (reservaRepository.existsByVehiculoIdAndFecha(vehiculo.getId(), request.getFecha())) {
            throw new IllegalArgumentException("Ya existe una reserva para este vehiculo en esa fecha");
        }

        Long vehiculoId = request.getVehiculoId();
        if (reservaRepository.existsByVehiculoIdAndEstadoIn(vehiculoId, List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA))) {
            throw new IllegalArgumentException("Este vehiculo ya tiene una reserva activa");
        }

        Reserva reserva = new Reserva();
        reserva.setFecha(request.getFecha());
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setUsuario(usuario);
        reserva.setVehiculo(vehiculo);
        reserva.setEspacio(espacio);

        return ReservaResponse.fromReserva(reservaRepository.save(reserva));
    }

    @Override
    @Transactional
    public ReservaResponse cambiarEstado(Long id, String estado) {
        String estadoNormalizado = normalizarEstadoAdmin(estado);
        if (estadoNormalizado.equals("CONFIRMADA")) {
            return aprobar(id);
        }

        return rechazar(id);
    }

    @Override
    @Transactional
    public ReservaResponse aprobar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id de la reserva es obligatorio");
        }

        validarAdmin();
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        if (EstadoReserva.PENDIENTE != reserva.getEstado()) {
            throw new IllegalArgumentException("Solo se pueden aprobar reservas pendientes");
        }

        Espacio espacio = reserva.getEspacio();
        if (espacio == null) {
            throw new IllegalArgumentException("La reserva no tiene un espacio asociado");
        }

        if (espacio.getEstado() != EstadoEspacio.LIBRE || espacio.getVehiculo() != null) {
            throw new IllegalArgumentException("No se puede aprobar porque el espacio ya fue ocupado");
        }

        Vehiculo vehiculo = reserva.getVehiculo();
        if (espacioRepository.vehiculoTieneEspacio(vehiculo.getId())) {
            throw new IllegalArgumentException("El vehiculo ya posee un espacio asignado");
        }

        reserva.setEstado(EstadoReserva.CONFIRMADA);
        espacio.setEstado(EstadoEspacio.OCUPADO);
        espacio.setVehiculo(vehiculo);
        espacioRepository.save(espacio);

        return ReservaResponse.fromReserva(reservaRepository.save(reserva));
    }

    @Override
    @Transactional
    public ReservaResponse rechazar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id de la reserva es obligatorio");
        }

        validarAdmin();
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        if (EstadoReserva.PENDIENTE != reserva.getEstado()) {
            throw new IllegalArgumentException("Solo se pueden rechazar reservas pendientes");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        return ReservaResponse.fromReserva(reservaRepository.save(reserva));
    }

    private void validarCreacion(ReservaRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Los datos de la reserva son obligatorios");
        }

        if (request.getVehiculoId() == null) {
            throw new IllegalArgumentException("El vehiculo es obligatorio");
        }

        if (request.getEspacioId() == null) {
            throw new IllegalArgumentException("Selecciona un espacio disponible");
        }

        if (request.getFecha() == null) {
            throw new IllegalArgumentException("La fecha de reserva es obligatoria");
        }

        if (request.getFecha().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se pueden crear reservas con fechas pasadas");
        }
    }

    private String normalizarEstadoAdmin(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            throw new IllegalArgumentException("El estado de la reserva es obligatorio");
        }

        String estadoNormalizado = estado.trim().toUpperCase();
        if (!estadoNormalizado.equals("CONFIRMADA") && !estadoNormalizado.equals("CANCELADA")) {
            throw new IllegalArgumentException("El administrador solo puede cambiar la reserva a CONFIRMADA o CANCELADA");
        }

        return estadoNormalizado;
    }

    private Usuario obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("No se encontro el usuario autenticado");
        }

        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));
    }

    private void validarAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getAuthorities().stream()
                .noneMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            throw new IllegalArgumentException("Solo un administrador puede realizar esta accion");
        }
    }
}
