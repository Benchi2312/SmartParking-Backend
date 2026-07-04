package com.smartparking.backend.service;

import java.math.BigDecimal;

public interface ConfiguracionService {
    BigDecimal getTarifaPorHora();
    BigDecimal actualizarTarifaPorHora(BigDecimal nuevaTarifa);
}
