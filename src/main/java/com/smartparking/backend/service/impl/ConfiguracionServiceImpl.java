package com.smartparking.backend.service.impl;

import com.smartparking.backend.model.ConfiguracionSistema;
import com.smartparking.backend.repository.ConfiguracionRepository;
import com.smartparking.backend.service.ConfiguracionService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ConfiguracionServiceImpl implements ConfiguracionService {

    private final ConfiguracionRepository configuracionRepository;

    public ConfiguracionServiceImpl(ConfiguracionRepository configuracionRepository) {
        this.configuracionRepository = configuracionRepository;
    }

    @PostConstruct
    public void init() {
        if (configuracionRepository.count() == 0) {
            ConfiguracionSistema config = new ConfiguracionSistema(new BigDecimal("2.00"));
            configuracionRepository.save(config);
        }
    }

    @Override
    public BigDecimal getTarifaPorHora() {
        return configuracionRepository.findById(1L)
                .map(ConfiguracionSistema::getTarifaPorHora)
                .orElse(new BigDecimal("2.00"));
    }

    @Override
    @Transactional
    public BigDecimal actualizarTarifaPorHora(BigDecimal nuevaTarifa) {
        if (nuevaTarifa == null || nuevaTarifa.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La tarifa debe ser un valor positivo");
        }

        ConfiguracionSistema config = configuracionRepository.findById(1L)
                .orElseGet(() -> {
                    ConfiguracionSistema nueva = new ConfiguracionSistema();
                    nueva.setId(1L);
                    return nueva;
                });

        config.setTarifaPorHora(nuevaTarifa);
        configuracionRepository.save(config);
        return nuevaTarifa;
    }
}
