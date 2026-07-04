package com.smartparking.backend.config;

import com.smartparking.backend.security.jwt.JwtAuthenticationEntryPoint;
import com.smartparking.backend.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // En APIs REST con JWT no usamos formularios ni cookies de sesion, por eso CSRF se deshabilita.
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        // Se permiten ambas rutas para cumplir el requerimiento y no romper la API actual /api/auth.
                        .requestMatchers("/auth/login", "/auth/register").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        // Administracion de espacios: solo ADMIN puede crear, editar o eliminar
                        .requestMatchers(HttpMethod.POST, "/api/espacios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/espacios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/espacios/**").hasRole("ADMIN")
                        .requestMatchers("/api/espacios/*/liberar").hasRole("ADMIN")
                        // Gestion de usuarios: solo ADMIN puede listar todos
                        .requestMatchers("/api/auth/usuarios").hasRole("ADMIN")
                        // Vehiculos: solo ADMIN puede listar todos los vehiculos (USER usa /mis-vehiculos)
                        .requestMatchers(HttpMethod.GET, "/api/vehiculos").hasRole("ADMIN")
                        // Reservas: solo ADMIN puede listar todas las reservas (USER usa /mis-reservas)
                        .requestMatchers(HttpMethod.GET, "/api/reservas").hasRole("ADMIN")
                        // Reservas: solo ADMIN puede ver pendientes, aprobar o rechazar
                        .requestMatchers("/api/reservas/pendientes").hasRole("ADMIN")
                        .requestMatchers("/api/reservas/*/aprobar").hasRole("ADMIN")
                        .requestMatchers("/api/reservas/*/rechazar").hasRole("ADMIN")
                        // Configuracion: solo ADMIN puede actualizar la tarifa
                        .requestMatchers(HttpMethod.PUT, "/api/configuracion/tarifa").hasRole("ADMIN")
                        .requestMatchers("/auth/**").authenticated()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                // El filtro JWT se ejecuta antes del filtro estandar de usuario/password de Spring Security.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
