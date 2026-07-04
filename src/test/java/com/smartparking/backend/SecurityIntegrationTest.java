package com.smartparking.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartparking.backend.model.Espacio;
import com.smartparking.backend.model.EstadoEspacio;
import com.smartparking.backend.model.EstadoReserva;
import com.smartparking.backend.model.Reserva;
import com.smartparking.backend.model.Usuario;
import com.smartparking.backend.model.Vehiculo;
import com.smartparking.backend.repository.EspacioRepository;
import com.smartparking.backend.repository.ReservaRepository;
import com.smartparking.backend.repository.UsuarioRepository;
import com.smartparking.backend.repository.VehiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityIntegrationTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private EspacioRepository espacioRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        reservaRepository.deleteAll();
        vehiculoRepository.deleteAll();
        espacioRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario admin = new Usuario();
        admin.setNombre("Admin Test");
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRol("ADMIN");
        usuarioRepository.save(admin);

        Usuario user = new Usuario();
        user.setNombre("User Test");
        user.setEmail("user@test.com");
        user.setPassword(passwordEncoder.encode("user123"));
        user.setRol("USER");
        usuarioRepository.save(user);
    }

    private String login(String email, String password) {
        String url = "http://localhost:" + port + "/api/auth/login";
        ResponseEntity<String> response = restTemplate.postForEntity(
                url,
                Map.of("email", email, "password", password),
                String.class
        );
        try {
            JsonNode json = objectMapper.readTree(response.getBody());
            return json.get("token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse login response: " + response.getBody(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<String> requestAs(String method, String path, String token, String body) {
        String url = "http://localhost:" + port + path;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            return restTemplate.exchange(url, HttpMethod.valueOf(method.toUpperCase()), entity, String.class);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @Test
    void user_CrearEspacio_debeRetornar403() {
        String token = login("user@test.com", "user123");
        ResponseEntity<String> response = requestAs("POST", "/api/espacios", token, "{\"numero\":\"Z-99\"}");
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void admin_CrearEspacio_debeRetornar200() {
        String token = login("admin@test.com", "admin123");
        ResponseEntity<String> response = requestAs("POST", "/api/espacios", token, "{\"numero\":\"Z-99\"}");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void user_ActualizarEspacio_debeRetornar403() {
        String token = login("user@test.com", "user123");
        ResponseEntity<String> response = requestAs("PUT", "/api/espacios/1", token, "{\"numero\":\"Z-99\"}");
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void user_EliminarEspacio_debeRetornar403() {
        String token = login("user@test.com", "user123");
        ResponseEntity<String> response = requestAs("DELETE", "/api/espacios/1", token, null);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void user_ListarVehiculos_debeRetornar403() {
        String token = login("user@test.com", "user123");
        ResponseEntity<String> response = requestAs("GET", "/api/vehiculos?usuarioId=1", token, null);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void user_ListarMisVehiculos_debeRetornar200() {
        String token = login("user@test.com", "user123");
        ResponseEntity<String> response = requestAs("GET", "/api/vehiculos/mis-vehiculos", token, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void admin_ListarVehiculosConUsuarioId_debeRetornar200() {
        String token = login("admin@test.com", "admin123");
        ResponseEntity<String> response = requestAs("GET", "/api/vehiculos?usuarioId=1", token, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void user_ListarEspacios_debeRetornar200() {
        String token = login("user@test.com", "user123");
        ResponseEntity<String> response = requestAs("GET", "/api/espacios", token, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void user_ListarEspaciosDisponibles_debeRetornar200() {
        String token = login("user@test.com", "user123");
        ResponseEntity<String> response = requestAs("GET", "/api/espacios/disponibles", token, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void user_ListarReservasConUsuarioId_debeRetornar403() {
        String token = login("user@test.com", "user123");
        ResponseEntity<String> response = requestAs("GET", "/api/reservas?usuarioId=1", token, null);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void user_ListarMisReservas_debeRetornar200() {
        String token = login("user@test.com", "user123");
        ResponseEntity<String> response = requestAs("GET", "/api/reservas/mis-reservas", token, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void admin_ListarReservasConUsuarioId_debeRetornar200() {
        String token = login("admin@test.com", "admin123");
        ResponseEntity<String> response = requestAs("GET", "/api/reservas?usuarioId=1", token, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void admin_ListarAuthUsuarios_debeRetornar200() {
        String token = login("admin@test.com", "admin123");
        ResponseEntity<String> response = requestAs("GET", "/api/auth/usuarios", token, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void user_ListarAuthUsuarios_debeRetornar403() {
        String token = login("user@test.com", "user123");
        ResponseEntity<String> response = requestAs("GET", "/api/auth/usuarios", token, null);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void user_ListarReservasPendientes_debeRetornar403() {
        String token = login("user@test.com", "user123");
        ResponseEntity<String> response = requestAs("GET", "/api/reservas/pendientes", token, null);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void admin_ListarReservasPendientes_debeRetornar200() {
        String token = login("admin@test.com", "admin123");
        ResponseEntity<String> response = requestAs("GET", "/api/reservas/pendientes", token, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void user_CancelarPropiaReservaPendiente_debeRetornar200() {
        Usuario user = usuarioRepository.findByEmail("user@test.com").orElseThrow();

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca("CXL-123");
        vehiculo.setMarca("Test");
        vehiculo.setModelo("Car");
        vehiculo.setUsuario(user);
        vehiculo = vehiculoRepository.save(vehiculo);

        Espacio espacio = new Espacio();
        espacio.setNumero("T-99");
        espacio.setEstado(EstadoEspacio.LIBRE);
        espacio = espacioRepository.save(espacio);

        Reserva reserva = new Reserva();
        reserva.setFecha(LocalDate.now());
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setUsuario(user);
        reserva.setVehiculo(vehiculo);
        reserva.setEspacio(espacio);
        reserva = reservaRepository.save(reserva);

        String token = login("user@test.com", "user123");
        ResponseEntity<String> response = requestAs("POST", "/api/reservas/" + reserva.getId() + "/cancelar", token, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("CANCELADA"), "Deberia responder con estado CANCELADA");
    }

    @Test
    void user_CancelarReservaDeOtroUsuario_debeRetornarError() {
        Usuario owner = usuarioRepository.findByEmail("admin@test.com").orElseThrow();

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca("CXL-124");
        vehiculo.setMarca("Test");
        vehiculo.setModelo("Car");
        vehiculo.setUsuario(owner);
        vehiculo = vehiculoRepository.save(vehiculo);

        Espacio espacio = new Espacio();
        espacio.setNumero("T-98");
        espacio.setEstado(EstadoEspacio.LIBRE);
        espacio = espacioRepository.save(espacio);

        Reserva reserva = new Reserva();
        reserva.setFecha(LocalDate.now());
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setUsuario(owner);
        reserva.setVehiculo(vehiculo);
        reserva.setEspacio(espacio);
        reserva = reservaRepository.save(reserva);

        String token = login("user@test.com", "user123");
        ResponseEntity<String> response = requestAs("POST", "/api/reservas/" + reserva.getId() + "/cancelar", token, null);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
