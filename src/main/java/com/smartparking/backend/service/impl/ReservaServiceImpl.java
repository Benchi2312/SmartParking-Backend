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
import com.smartparking.backend.service.ConfiguracionService;
import com.smartparking.backend.service.ReservaService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final EspacioRepository espacioRepository;
    private final ConfiguracionService configuracionService;

    public ReservaServiceImpl(ReservaRepository reservaRepository,
                              UsuarioRepository usuarioRepository,
                              VehiculoRepository vehiculoRepository,
                              EspacioRepository espacioRepository,
                              ConfiguracionService configuracionService) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.espacioRepository = espacioRepository;
        this.configuracionService = configuracionService;
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
    public ReservaResponse cambiarEstado(Long id, String estadoStr) {
        EstadoReserva estado = convertirEstado(estadoStr);
        if (estado == EstadoReserva.CONFIRMADA) {
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
        reserva.setHoraInicio(LocalDateTime.now());
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
        reserva.setCanceladoPor("ADMIN");
        return ReservaResponse.fromReserva(reservaRepository.save(reserva));
    }

    @Override
    @Transactional
    public ReservaResponse cancelarPorUsuario(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id de la reserva es obligatorio");
        }

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        validarOwnership(reserva);

        if (reserva.getEstado() == EstadoReserva.CANCELADA || reserva.getEstado() == EstadoReserva.FINALIZADA) {
            throw new IllegalArgumentException(
                    "No se puede cancelar una reserva en estado " + reserva.getEstado().name().toLowerCase()
            );
        }

        if (reserva.getEstado() == EstadoReserva.CONFIRMADA) {
            Espacio espacio = reserva.getEspacio();
            if (espacio != null) {
                espacio.setEstado(EstadoEspacio.LIBRE);
                espacio.setVehiculo(null);
                espacioRepository.save(espacio);
            }
            reserva.setHoraFin(LocalDateTime.now());
            calcularCosto(reserva);
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        reserva.setCanceladoPor("USUARIO");
        return ReservaResponse.fromReserva(reservaRepository.save(reserva));
    }

    private void validarOwnership(Reserva reserva) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (esAdmin) {
            return;
        }

        Usuario usuarioAutenticado = obtenerUsuarioAutenticado();
        if (!reserva.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new IllegalArgumentException("No tienes permiso para cancelar esta reserva");
        }
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

    private EstadoReserva convertirEstado(String estadoStr) {
        if (estadoStr == null || estadoStr.trim().isEmpty()) {
            throw new IllegalArgumentException("El estado de la reserva es obligatorio");
        }

        try {
            EstadoReserva estado = EstadoReserva.valueOf(estadoStr.trim().toUpperCase());
            if (estado == EstadoReserva.CONFIRMADA || estado == EstadoReserva.CANCELADA) {
                return estado;
            }
        } catch (IllegalArgumentException e) {
            // valueOf no encontro una constante coincide
        }

        throw new IllegalArgumentException(
                "Estado invalido: '" + estadoStr + "'. El administrador solo puede cambiar la reserva a CONFIRMADA o CANCELADA"
        );
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

    private void calcularCosto(Reserva reserva) {
        if (reserva.getHoraInicio() == null || reserva.getHoraFin() == null) {
            return;
        }

        long minutos = Duration.between(reserva.getHoraInicio(), reserva.getHoraFin()).toMinutes();
        long horas = (long) Math.ceil(minutos / 60.0);

        BigDecimal tarifa = configuracionService.getTarifaPorHora();
        BigDecimal costo = tarifa.multiply(BigDecimal.valueOf(horas)).setScale(2, RoundingMode.HALF_UP);
        reserva.setCostoTotal(costo);
    }
}
