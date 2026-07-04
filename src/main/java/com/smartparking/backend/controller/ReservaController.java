package com.smartparking.backend.controller;

import com.smartparking.backend.dto.ReservaRequest;
import com.smartparking.backend.dto.ReservaResponse;
import com.smartparking.backend.service.ReservaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@CrossOrigin(origins = "http://localhost:4200")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    public List<ReservaResponse> listar(@RequestParam(required = false) Long usuarioId) {
        if (usuarioId == null) {
            return reservaService.listarTodos();
        }

        return reservaService.listarPorUsuario(usuarioId);
    }

    @GetMapping("/mis-reservas")
    public List<ReservaResponse> listarMisReservas() {
        return reservaService.listarMisReservas();
    }

    @GetMapping("/mis-reservas/ultima")
    public ReservaResponse obtenerUltimaReserva() {
        return reservaService.obtenerUltimaReserva();
    }

    @GetMapping("/pendientes")
    public List<ReservaResponse> listarPendientes() {
        return reservaService.listarPendientes();
    }

    @PostMapping
    public ReservaResponse crear(@RequestBody ReservaRequest request) {
        return reservaService.crear(request);
    }

    @PutMapping("/{id}/estado")
    public ReservaResponse cambiarEstado(@PathVariable Long id, @RequestBody ReservaRequest request) {
        return reservaService.cambiarEstado(id, request.getEstado());
    }

    @PostMapping("/{id}/aprobar")
    public ReservaResponse aprobar(@PathVariable Long id) {
        return reservaService.aprobar(id);
    }

    @PostMapping("/{id}/rechazar")
    public ReservaResponse rechazar(@PathVariable Long id) {
        return reservaService.rechazar(id);
    }

    @PostMapping("/{id}/cancelar")
    public ReservaResponse cancelar(@PathVariable Long id) {
        return reservaService.cancelarPorUsuario(id);
    }
}
