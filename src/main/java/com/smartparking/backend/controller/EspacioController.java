package com.smartparking.backend.controller;

import com.smartparking.backend.dto.EspacioRequest;
import com.smartparking.backend.dto.EspacioResponse;
import com.smartparking.backend.service.EspacioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/espacios")
@CrossOrigin(origins = "http://localhost:4200")
public class EspacioController {

    private final EspacioService espacioService;

    public EspacioController(EspacioService espacioService) {
        this.espacioService = espacioService;
    }

    @GetMapping
    public List<EspacioResponse> listarTodos() {
        return espacioService.listarTodos();
    }

    @PostMapping
    public EspacioResponse crear(@RequestBody EspacioRequest request) {
        return espacioService.crear(request);
    }

    @PutMapping("/{id}")
    public EspacioResponse actualizar(@PathVariable Long id,
                                      @RequestBody EspacioRequest request) {
        return espacioService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        espacioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
