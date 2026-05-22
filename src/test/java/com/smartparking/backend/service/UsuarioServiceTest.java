package com.smartparking.backend.service;

import com.smartparking.backend.dto.RegisterRequest;
import com.smartparking.backend.model.Usuario;
import com.smartparking.backend.repository.UsuarioRepository;
import com.smartparking.backend.service.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UsuarioServiceTest {

    @Test
    void registrarGuardaPasswordEncriptado() {
        UsuarioRepository repo = Mockito.mock(UsuarioRepository.class);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        RegisterRequest request = new RegisterRequest();
        request.setNombre("Usuario Test");
        request.setEmail("nuevo@test.com");
        request.setPassword("123456");

        Mockito.when(repo.findByEmail("nuevo@test.com"))
                .thenReturn(Optional.empty());
        Mockito.when(repo.save(Mockito.any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioServiceImpl service = new UsuarioServiceImpl(repo, encoder);

        Usuario resultado = service.registrar(request);

        assertNotNull(resultado.getPassword());
        assertNotEquals("123456", resultado.getPassword());
        assertTrue(encoder.matches("123456", resultado.getPassword()));
        assertEquals("USER", resultado.getRol());
    }

    @Test
    void loginCorrecto() {

        // Simular repositorio
        UsuarioRepository repo = Mockito.mock(UsuarioRepository.class);

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Usuario fake
        Usuario user = new Usuario();
        user.setEmail("test@test.com");
        user.setPassword(encoder.encode("123456"));

        // Simular respuesta del repo
        Mockito.when(repo.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        // Servicio real
        UsuarioServiceImpl service = new UsuarioServiceImpl(repo, encoder);

        // Ejecutar login
        Usuario resultado = service.login("test@test.com", "123456");

        // Validar
        assertNotNull(resultado);
        assertEquals("test@test.com", resultado.getEmail());
    }
}
