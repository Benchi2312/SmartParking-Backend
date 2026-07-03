package com.smartparking.backend.service;

import com.smartparking.backend.dto.ReservaRequest;
import com.smartparking.backend.dto.VehiculoRequest;
import com.smartparking.backend.model.Espacio;
import com.smartparking.backend.model.EstadoEspacio;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
}
