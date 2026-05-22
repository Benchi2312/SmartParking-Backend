package com.smartparking.backend.controller;

import com.smartparking.backend.dto.ReservaRequest;
import com.smartparking.backend.model.Reserva;
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
    public List<Reserva> listar(@RequestParam(required = false) Long usuarioId) {
        if (usuarioId == null) {
            return reservaService.listarTodos();
        }

        return reservaService.listarPorUsuario(usuarioId);
    }

    @PostMapping
    public Reserva crear(@RequestBody ReservaRequest request) {
        return reservaService.crear(request);
    }
}
