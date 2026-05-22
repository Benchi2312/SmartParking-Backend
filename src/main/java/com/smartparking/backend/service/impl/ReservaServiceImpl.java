package com.smartparking.backend.service.impl;

import com.smartparking.backend.dto.ReservaRequest;
import com.smartparking.backend.model.Reserva;
import com.smartparking.backend.model.Usuario;
import com.smartparking.backend.model.Vehiculo;
import com.smartparking.backend.repository.ReservaRepository;
import com.smartparking.backend.repository.UsuarioRepository;
import com.smartparking.backend.repository.VehiculoRepository;
import com.smartparking.backend.service.ReservaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;

    public ReservaServiceImpl(ReservaRepository reservaRepository,
                              UsuarioRepository usuarioRepository,
                              VehiculoRepository vehiculoRepository) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.vehiculoRepository = vehiculoRepository;
    }

    @Override
    public List<Reserva> listarTodos() {
        return reservaRepository.findAll();
    }

    @Override
    public List<Reserva> listarPorUsuario(Long usuarioId) {
        return reservaRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public Reserva crear(ReservaRequest request) {
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Vehiculo vehiculo = vehiculoRepository.findById(request.getVehiculoId())
                .orElseThrow(() -> new RuntimeException("Vehiculo no encontrado"));

        Reserva reserva = new Reserva();
        reserva.setFecha(request.getFecha());
        reserva.setEstado(request.getEstado());
        reserva.setUsuario(usuario);
        reserva.setVehiculo(vehiculo);

        return reservaRepository.save(reserva);
    }
}
