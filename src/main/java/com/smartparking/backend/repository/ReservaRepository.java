package com.smartparking.backend.repository;

import com.smartparking.backend.model.EstadoReserva;
import com.smartparking.backend.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByUsuarioId(Long usuarioId);
    List<Reserva> findByEstadoOrderByFechaAscIdAsc(EstadoReserva estado);
    boolean existsByVehiculoIdAndFecha(Long vehiculoId, LocalDate fecha);

    boolean existsByVehiculoIdAndEstadoIn(Long vehiculoId, List<EstadoReserva> estados);

    @Query("SELECT COUNT(r) > 0 FROM Reserva r WHERE r.vehiculo.id = :vehiculoId AND r.estado IN :estados")
    boolean existsByVehiculoActivo(@Param("vehiculoId") Long vehiculoId, @Param("estados") List<EstadoReserva> estados);

    List<Reserva> findByVehiculoIdAndEspacioIdAndEstado(Long vehiculoId, Long espacioId, EstadoReserva estado);

    Optional<Reserva> findFirstByUsuarioIdOrderByIdDesc(Long usuarioId);
}
