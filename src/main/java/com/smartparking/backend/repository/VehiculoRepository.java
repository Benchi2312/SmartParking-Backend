package com.smartparking.backend.repository;

import com.smartparking.backend.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    @Query("SELECT v FROM Vehiculo v JOIN FETCH v.usuario ORDER BY v.id DESC")
    List<Vehiculo> listarTodosConUsuario();

    @Query("SELECT v FROM Vehiculo v WHERE v.usuario.id = :usuarioId ORDER BY v.id DESC")
    List<Vehiculo> buscarPorUsuario(@Param("usuarioId") Long usuarioId);

    @Query("SELECT COUNT(v) > 0 FROM Vehiculo v WHERE LOWER(v.placa) = LOWER(:placa)")
    boolean existePlaca(@Param("placa") String placa);

    @Query("SELECT COUNT(v) > 0 FROM Vehiculo v WHERE LOWER(v.placa) = LOWER(:placa) AND v.id <> :id")
    boolean existePlacaEnOtroVehiculo(@Param("placa") String placa, @Param("id") Long id);
}
