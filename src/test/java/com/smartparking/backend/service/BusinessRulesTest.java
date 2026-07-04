package com.smartparking.backend.service;

import com.smartparking.backend.dto.ReservaRequest;
import com.smartparking.backend.dto.ReservaResponse;
import com.smartparking.backend.dto.VehiculoRequest;
import com.smartparking.backend.exception.ApiErrorResponse;
import com.smartparking.backend.exception.ApiExceptionHandler;
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
import com.smartparking.backend.service.impl.ReservaServiceImpl;
import com.smartparking.backend.service.impl.VehiculoServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BusinessRulesTest {

    @Test
    void actualizar_debeRechazarVehiculoQueNoPerteneceAlUsuarioAutenticado() {
        VehiculoRepository vehiculoRepository = mock(VehiculoRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        VehiculoServiceImpl service = new VehiculoServiceImpl(vehiculoRepository, usuarioRepository);

        Usuario usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(2L);
        usuarioAutenticado.setEmail("user2@test.com");

        Usuario propietario = new Usuario();
        propietario.setId(1L);
        propietario.setEmail("user1@test.com");

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(10L);
        vehiculo.setMarca("Toyota");
        vehiculo.setModelo("Corolla");
        vehiculo.setPlaca("ABC-123");
        vehiculo.setUsuario(propietario);

        when(usuarioRepository.findByEmail("user2@test.com")).thenReturn(Optional.of(usuarioAutenticado));
        when(vehiculoRepository.existePlacaEnOtroVehiculo(any(), anyLong())).thenReturn(false);
        when(vehiculoRepository.findById(10L)).thenReturn(Optional.of(vehiculo));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "user2@test.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );

        VehiculoRequest request = new VehiculoRequest();
        request.setMarca("Honda");
        request.setModelo("Civic");
        request.setPlaca("ABC-123");

        assertThrows(IllegalArgumentException.class, () -> service.actualizar(10L, request));

        SecurityContextHolder.clearContext();
    }

    @Test
    void crearReserva_debeRechazarDuplicadosParaElMismoVehiculoYFecha() {
        ReservaRepository reservaRepository = mock(ReservaRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        VehiculoRepository vehiculoRepository = mock(VehiculoRepository.class);
        EspacioRepository espacioRepository = mock(EspacioRepository.class);
        ReservaServiceImpl service = new ReservaServiceImpl(
                reservaRepository,
                usuarioRepository,
                vehiculoRepository,
                espacioRepository
        );

        Usuario usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(2L);
        usuarioAutenticado.setEmail("user2@test.com");

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(20L);
        vehiculo.setUsuario(usuarioAutenticado);

        Espacio espacio = new Espacio();
        espacio.setId(30L);
        espacio.setEstado(EstadoEspacio.LIBRE);
        espacio.setVehiculo(null);

        when(usuarioRepository.findByEmail("user2@test.com")).thenReturn(Optional.of(usuarioAutenticado));
        when(vehiculoRepository.findById(20L)).thenReturn(Optional.of(vehiculo));
        when(espacioRepository.findById(30L)).thenReturn(Optional.of(espacio));
        when(reservaRepository.existsByVehiculoIdAndFecha(20L, LocalDate.of(2026, 6, 20))).thenReturn(true);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "user2@test.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );

        ReservaRequest request = new ReservaRequest();
        request.setVehiculoId(20L);
        request.setEspacioId(30L);
        request.setFecha(LocalDate.of(2026, 6, 20));

        assertThrows(IllegalArgumentException.class, () -> service.crear(request));

        SecurityContextHolder.clearContext();
    }

    @Test
    void crearReserva_debeEstablecerEstadoPendiente() {
        ReservaRepository reservaRepository = mock(ReservaRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        VehiculoRepository vehiculoRepository = mock(VehiculoRepository.class);
        EspacioRepository espacioRepository = mock(EspacioRepository.class);
        ReservaServiceImpl service = new ReservaServiceImpl(
                reservaRepository, usuarioRepository, vehiculoRepository, espacioRepository
        );

        Usuario usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(2L);
        usuarioAutenticado.setEmail("user2@test.com");

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(20L);
        vehiculo.setUsuario(usuarioAutenticado);

        Espacio espacio = new Espacio();
        espacio.setId(30L);
        espacio.setEstado(EstadoEspacio.LIBRE);
        espacio.setVehiculo(null);

        when(usuarioRepository.findByEmail("user2@test.com")).thenReturn(Optional.of(usuarioAutenticado));
        when(vehiculoRepository.findById(20L)).thenReturn(Optional.of(vehiculo));
        when(espacioRepository.findById(30L)).thenReturn(Optional.of(espacio));
        when(reservaRepository.existsByVehiculoIdAndFecha(any(), any())).thenReturn(false);
        when(reservaRepository.existsByVehiculoIdAndEstadoIn(any(), any())).thenReturn(false);
        when(reservaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "user2@test.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );

        ReservaRequest request = new ReservaRequest();
        request.setVehiculoId(20L);
        request.setEspacioId(30L);
        request.setFecha(LocalDate.of(2026, 7, 20));

        ReservaResponse response = service.crear(request);
        assertEquals("PENDIENTE", response.getEstado());

        SecurityContextHolder.clearContext();
    }

    @Test
    void aprobarReserva_debeCambiarEstadoAConfirmada() {
        ReservaRepository reservaRepository = mock(ReservaRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        VehiculoRepository vehiculoRepository = mock(VehiculoRepository.class);
        EspacioRepository espacioRepository = mock(EspacioRepository.class);
        ReservaServiceImpl service = new ReservaServiceImpl(
                reservaRepository, usuarioRepository, vehiculoRepository, espacioRepository
        );

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Test User");
        usuario.setEmail("test@test.com");

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(20L);
        vehiculo.setUsuario(usuario);

        Espacio espacio = new Espacio();
        espacio.setId(30L);
        espacio.setEstado(EstadoEspacio.LIBRE);
        espacio.setVehiculo(null);

        Reserva reserva = new Reserva();
        reserva.setId(100L);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setUsuario(usuario);
        reserva.setVehiculo(vehiculo);
        reserva.setEspacio(espacio);

        when(reservaRepository.findById(100L)).thenReturn(Optional.of(reserva));
        when(espacioRepository.vehiculoTieneEspacio(20L)).thenReturn(false);
        when(reservaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(espacioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin@test.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );

        ReservaResponse response = service.aprobar(100L);
        assertEquals("CONFIRMADA", response.getEstado());

        SecurityContextHolder.clearContext();
    }

    @Test
    void rechazarReserva_debeCambiarEstadoACancelada() {
        ReservaRepository reservaRepository = mock(ReservaRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        VehiculoRepository vehiculoRepository = mock(VehiculoRepository.class);
        EspacioRepository espacioRepository = mock(EspacioRepository.class);
        ReservaServiceImpl service = new ReservaServiceImpl(
                reservaRepository, usuarioRepository, vehiculoRepository, espacioRepository
        );

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Test User");
        usuario.setEmail("test@test.com");

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(20L);
        vehiculo.setUsuario(usuario);

        Reserva reserva = new Reserva();
        reserva.setId(100L);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setUsuario(usuario);
        reserva.setVehiculo(vehiculo);

        when(reservaRepository.findById(100L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin@test.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );

        ReservaResponse response = service.rechazar(100L);
        assertEquals("CANCELADA", response.getEstado());

        SecurityContextHolder.clearContext();
    }

    @Test
    void cambiarEstado_conEstadoInvalido_debeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReservaRepository reservaRepository = mock(ReservaRepository.class);
            UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
            VehiculoRepository vehiculoRepository = mock(VehiculoRepository.class);
            EspacioRepository espacioRepository = mock(EspacioRepository.class);
            ReservaServiceImpl service = new ReservaServiceImpl(
                    reservaRepository, usuarioRepository, vehiculoRepository, espacioRepository
            );

            service.cambiarEstado(1L, "INVALIDO");
        });
    }

    @Test
    void user_CancelarPropiaReservaPendiente_debeQuedarCancelada() {
        ReservaRepository reservaRepository = mock(ReservaRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        VehiculoRepository vehiculoRepository = mock(VehiculoRepository.class);
        EspacioRepository espacioRepository = mock(EspacioRepository.class);
        ReservaServiceImpl service = new ReservaServiceImpl(
                reservaRepository, usuarioRepository, vehiculoRepository, espacioRepository
        );

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("User");
        usuario.setEmail("user@test.com");

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(10L);
        vehiculo.setUsuario(usuario);

        Espacio espacio = new Espacio();
        espacio.setId(20L);
        espacio.setEstado(EstadoEspacio.LIBRE);

        Reserva reserva = new Reserva();
        reserva.setId(100L);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setUsuario(usuario);
        reserva.setVehiculo(vehiculo);
        reserva.setEspacio(espacio);

        when(usuarioRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));
        when(reservaRepository.findById(100L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "user@test.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );

        ReservaResponse response = service.cancelarPorUsuario(100L);
        assertEquals("CANCELADA", response.getEstado());

        SecurityContextHolder.clearContext();
    }

    @Test
    void user_CancelarPropiaReservaConfirmada_debeLiberarEspacio() {
        ReservaRepository reservaRepository = mock(ReservaRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        VehiculoRepository vehiculoRepository = mock(VehiculoRepository.class);
        EspacioRepository espacioRepository = mock(EspacioRepository.class);
        ReservaServiceImpl service = new ReservaServiceImpl(
                reservaRepository, usuarioRepository, vehiculoRepository, espacioRepository
        );

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("User");
        usuario.setEmail("user@test.com");

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(10L);
        vehiculo.setUsuario(usuario);

        Espacio espacio = new Espacio();
        espacio.setId(20L);
        espacio.setEstado(EstadoEspacio.OCUPADO);
        espacio.setVehiculo(vehiculo);

        Reserva reserva = new Reserva();
        reserva.setId(100L);
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setUsuario(usuario);
        reserva.setVehiculo(vehiculo);
        reserva.setEspacio(espacio);

        when(usuarioRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));
        when(reservaRepository.findById(100L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(espacioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "user@test.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );

        ReservaResponse response = service.cancelarPorUsuario(100L);
        assertEquals("CANCELADA", response.getEstado());
        assertEquals(EstadoEspacio.LIBRE, espacio.getEstado());
        assertEquals(null, espacio.getVehiculo());

        SecurityContextHolder.clearContext();
    }

    @Test
    void user_CancelarReservaDeOtroUsuario_debeLanzarExcepcion() {
        ReservaRepository reservaRepository = mock(ReservaRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        VehiculoRepository vehiculoRepository = mock(VehiculoRepository.class);
        EspacioRepository espacioRepository = mock(EspacioRepository.class);
        ReservaServiceImpl service = new ReservaServiceImpl(
                reservaRepository, usuarioRepository, vehiculoRepository, espacioRepository
        );

        Usuario propietario = new Usuario();
        propietario.setId(1L);

        Usuario otroUsuario = new Usuario();
        otroUsuario.setId(2L);
        otroUsuario.setEmail("other@test.com");

        Reserva reserva = new Reserva();
        reserva.setId(100L);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setUsuario(propietario);

        when(usuarioRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otroUsuario));
        when(reservaRepository.findById(100L)).thenReturn(Optional.of(reserva));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "other@test.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );

        assertThrows(IllegalArgumentException.class, () -> service.cancelarPorUsuario(100L));

        SecurityContextHolder.clearContext();
    }

    @Test
    void cancelarReservaYaCancelada_debeLanzarExcepcion() {
        ReservaRepository reservaRepository = mock(ReservaRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        VehiculoRepository vehiculoRepository = mock(VehiculoRepository.class);
        EspacioRepository espacioRepository = mock(EspacioRepository.class);
        ReservaServiceImpl service = new ReservaServiceImpl(
                reservaRepository, usuarioRepository, vehiculoRepository, espacioRepository
        );

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("user@test.com");

        Reserva reserva = new Reserva();
        reserva.setId(100L);
        reserva.setEstado(EstadoReserva.CANCELADA);
        reserva.setUsuario(usuario);

        when(usuarioRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));
        when(reservaRepository.findById(100L)).thenReturn(Optional.of(reserva));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "user@test.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );

        assertThrows(IllegalArgumentException.class, () -> service.cancelarPorUsuario(100L));

        SecurityContextHolder.clearContext();
    }

    @Test
    void admin_CancelarReservaDeCualquierUsuario_debeRetornar200() {
        ReservaRepository reservaRepository = mock(ReservaRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        VehiculoRepository vehiculoRepository = mock(VehiculoRepository.class);
        EspacioRepository espacioRepository = mock(EspacioRepository.class);
        ReservaServiceImpl service = new ReservaServiceImpl(
                reservaRepository, usuarioRepository, vehiculoRepository, espacioRepository
        );

        Usuario propietario = new Usuario();
        propietario.setId(1L);

        Usuario admin = new Usuario();
        admin.setId(2L);
        admin.setEmail("admin@test.com");

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(10L);
        vehiculo.setUsuario(propietario);

        Reserva reserva = new Reserva();
        reserva.setId(100L);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setUsuario(propietario);
        reserva.setVehiculo(vehiculo);

        when(reservaRepository.findById(100L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin@test.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );

        ReservaResponse response = service.cancelarPorUsuario(100L);
        assertEquals("CANCELADA", response.getEstado());

        SecurityContextHolder.clearContext();
    }

    @Test
    void dataIntegrityHandler_debeRetornar409YErrorCorreoParaEmailDuplicado() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        String h2Message = "Unique index or primary key violation: \"PUBLIC.UK_DNVTM... ON PUBLIC.USUARIO(EMAIL) VALUES ('dup@test.com', ...)\"";
        DataIntegrityViolationException ex = new DataIntegrityViolationException(h2Message);

        ResponseEntity<ApiErrorResponse> response = handler.handleDataIntegrity(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Correo duplicado", response.getBody().getError());
        assertEquals("Ya existe un usuario registrado con ese correo electronico", response.getBody().getMessage());
    }

    @Test
    void dataIntegrityHandler_debeRetornar409YErrorPlacaParaPlacaDuplicada() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        String h2Message = "Unique index or primary key violation: \"PUBLIC.UK_SDFGT... ON PUBLIC.VEHICULO(PLACA) VALUES ('ABC-123', ...)\"";
        DataIntegrityViolationException ex = new DataIntegrityViolationException(h2Message);

        ResponseEntity<ApiErrorResponse> response = handler.handleDataIntegrity(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Placa duplicada", response.getBody().getError());
        assertEquals("Ya existe un vehiculo registrado con esa placa", response.getBody().getMessage());
    }

    @Test
    void dataIntegrityHandler_debeRetornar409YErrorNumeroParaNumeroDuplicado() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        String h2Message = "Unique index or primary key violation: \"PUBLIC.UK_GHJKL... ON PUBLIC.ESPACIO(NUMERO) VALUES ('A-01', ...)\"";
        DataIntegrityViolationException ex = new DataIntegrityViolationException(h2Message);

        ResponseEntity<ApiErrorResponse> response = handler.handleDataIntegrity(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Numero duplicado", response.getBody().getError());
        assertEquals("Ya existe un espacio registrado con ese numero", response.getBody().getMessage());
    }

    @Test
    void rechazarReserva_debeEstablecerCanceladoPorAdmin() {
        ReservaRepository reservaRepository = mock(ReservaRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        VehiculoRepository vehiculoRepository = mock(VehiculoRepository.class);
        EspacioRepository espacioRepository = mock(EspacioRepository.class);
        ReservaServiceImpl service = new ReservaServiceImpl(
                reservaRepository, usuarioRepository, vehiculoRepository, espacioRepository
        );

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(10L);
        vehiculo.setUsuario(usuario);

        Reserva reserva = new Reserva();
        reserva.setId(100L);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setUsuario(usuario);
        reserva.setVehiculo(vehiculo);

        when(reservaRepository.findById(100L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin@test.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );

        ReservaResponse response = service.rechazar(100L);
        assertEquals("ADMIN", response.getCanceladoPor());

        SecurityContextHolder.clearContext();
    }

    @Test
    void user_CancelarPropiaReserva_debeEstablecerCanceladoPorUsuario() {
        ReservaRepository reservaRepository = mock(ReservaRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        VehiculoRepository vehiculoRepository = mock(VehiculoRepository.class);
        EspacioRepository espacioRepository = mock(EspacioRepository.class);
        ReservaServiceImpl service = new ReservaServiceImpl(
                reservaRepository, usuarioRepository, vehiculoRepository, espacioRepository
        );

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("user@test.com");

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(10L);
        vehiculo.setUsuario(usuario);

        Reserva reserva = new Reserva();
        reserva.setId(100L);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setUsuario(usuario);
        reserva.setVehiculo(vehiculo);

        when(usuarioRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));
        when(reservaRepository.findById(100L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "user@test.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );

        ReservaResponse response = service.cancelarPorUsuario(100L);
        assertEquals("USUARIO", response.getCanceladoPor());

        SecurityContextHolder.clearContext();
    }

    @Test
    void dataIntegrityHandler_debeRetornar409GenericoParaOtrasViolaciones() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        String fkMessage = "Referential integrity constraint violation: \"FK_CONSTRAINT: PUBLIC.VEHICULO FOREIGN KEY(USUARIO_ID) REFERENCES PUBLIC.USUARIO(ID)\"";
        DataIntegrityViolationException ex = new DataIntegrityViolationException(fkMessage);

        ResponseEntity<ApiErrorResponse> response = handler.handleDataIntegrity(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Operacion no permitida", response.getBody().getError());
        assertEquals("No se puede realizar la operacion debido a restricciones de integridad en la base de datos", response.getBody().getMessage());
    }
}
