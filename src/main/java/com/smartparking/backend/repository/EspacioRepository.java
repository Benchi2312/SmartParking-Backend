package com.smartparking.backend.repository;

import com.smartparking.backend.model.Espacio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EspacioRepository extends JpaRepository<Espacio, Long> {

    @Query("SELECT e FROM Espacio e LEFT JOIN FETCH e.vehiculo v LEFT JOIN FETCH v.usuario ORDER BY e.numero ASC")
    List<Espacio> listarTodosConVehiculo();

    @Query("SELECT COUNT(e) > 0 FROM Espacio e WHERE LOWER(e.numero) = LOWER(:numero)")
    boolean existeNumero(@Param("numero") String numero);

    @Query("SELECT COUNT(e) > 0 FROM Espacio e WHERE LOWER(e.numero) = LOWER(:numero) AND e.id <> :id")
    boolean existeNumeroEnOtroEspacio(@Param("numero") String numero, @Param("id") Long id);
}
