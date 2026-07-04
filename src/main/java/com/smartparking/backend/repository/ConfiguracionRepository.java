package com.smartparking.backend.repository;

import com.smartparking.backend.model.ConfiguracionSistema;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracionRepository extends JpaRepository<ConfiguracionSistema, Long> {
}
