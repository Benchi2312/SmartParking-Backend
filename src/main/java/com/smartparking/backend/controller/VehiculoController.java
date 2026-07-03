package com.smartparking.backend.controller;

import com.smartparking.backend.dto.VehiculoRequest;
import com.smartparking.backend.dto.VehiculoResponse;
import com.smartparking.backend.service.VehiculoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehiculos")
@CrossOrigin(origins = "http://localhost:4200")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping("/mis-vehiculos")
    public List<VehiculoResponse> listarMisVehiculos() {
        return vehiculoService.listarMisVehiculos();
    }

    @GetMapping
    public List<VehiculoResponse> listar(@RequestParam(required = false) Long usuarioId) {
        if (usuarioId == null) {
            return vehiculoService.listarTodos();
        }

        return vehiculoService.listarPorUsuario(usuarioId);
    }

    @PostMapping
    public VehiculoResponse crear(@RequestBody VehiculoRequest request) {
        return vehiculoService.crear(request);
    }

    @PutMapping("/{id}")
    public VehiculoResponse actualizar(@PathVariable Long id,
                                       @RequestBody VehiculoRequest request) {
        return vehiculoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        vehiculoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
