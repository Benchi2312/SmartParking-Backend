package com.smartparking.backend.controller;

import com.smartparking.backend.service.ConfiguracionService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/configuracion")
@CrossOrigin(origins = "http://localhost:4200")
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    public ConfiguracionController(ConfiguracionService configuracionService) {
        this.configuracionService = configuracionService;
    }

    @GetMapping("/tarifa")
    public Map<String, BigDecimal> getTarifa() {
        return Map.of("tarifaPorHora", configuracionService.getTarifaPorHora());
    }

    @PutMapping("/tarifa")
    public Map<String, BigDecimal> actualizarTarifa(@RequestBody Map<String, BigDecimal> body) {
        BigDecimal nuevaTarifa = body.get("tarifaPorHora");
        return Map.of("tarifaPorHora", configuracionService.actualizarTarifaPorHora(nuevaTarifa));
    }
}
