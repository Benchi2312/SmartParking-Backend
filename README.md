# Smart Parking - Backend

## 1. Descripción del Proyecto

API REST del sistema Smart Parking, desarrollada con Spring Boot. Gestiona la lógica de negocio para estacionamientos inteligentes con autenticación JWT, roles y un flujo completo de reservas.

### Funcionalidades principales

- Autenticación y registro de usuarios con JWT + BCrypt
- Roles ADMIN y USER
- CRUD de vehículos con validación de propietario
- CRUD de espacios de estacionamiento
- Asignación y liberación de espacios
- Sistema de reservas (crear, aprobar, rechazar)
- Dashboard con métricas

---

## 2. Tecnologías Utilizadas

| Tecnología | Versión |
|---|---|
| Java | 24 |
| Spring Boot | 4 |
| Spring Web MVC | - |
| Spring Data JPA / Hibernate | - |
| Spring Security | - |
| JWT (jjwt) | 0.12 |
| MySQL | 8+ |
| BCrypt | - |
| Maven | - |
| JUnit 5 + Mockito | - |

---

## 3. Arquitectura del Proyecto

Arquitectura por capas:

```
src/main/java/com/smartparking/backend/
├── config/           → SecurityConfig, CorsConfig
├── controller/       → AuthController, VehiculoController, EspacioController, ReservaController
├── dto/              → Request/Response objects (AuthResponse, VehiculoRequest, etc.)
├── exception/        → ApiExceptionHandler, ApiErrorResponse
├── model/            → Entidades JPA (Usuario, Vehiculo, Espacio, Reserva, EstadoEspacio)
├── repository/       → Repositorios JPA
├── security/jwt/     → JwtService, JwtAuthenticationFilter, JwtAuthenticationEntryPoint
└── service/
    ├── UsuarioService, VehiculoService, EspacioService, ReservaService (interfaces)
    └── impl/         → Implementaciones con lógica de negocio
```

---

## 4. Endpoints REST

### Autenticación

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| POST | `/api/auth/register` | No | Registro de usuario |
| POST | `/api/auth/login` | No | Inicio de sesión |
| GET | `/api/auth/usuarios` | JWT | Listar usuarios (admin) |

### Vehículos

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| GET | `/api/vehiculos` | JWT | Listar vehículos (filtro por `?usuarioId=`) |
| POST | `/api/vehiculos` | JWT | Crear vehículo |
| PUT | `/api/vehiculos/{id}` | JWT | Actualizar vehículo (solo dueño o admin) |
| DELETE | `/api/vehiculos/{id}` | JWT | Eliminar vehículo (solo dueño o admin) |

### Espacios

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| GET | `/api/espacios` | JWT | Listar todos los espacios |
| GET | `/api/espacios/disponibles` | JWT | Listar espacios libres |
| POST | `/api/espacios` | JWT | Crear espacio |
| PUT | `/api/espacios/{id}` | JWT | Actualizar espacio |
| DELETE | `/api/espacios/{id}` | JWT | Eliminar espacio |
| POST | `/api/espacios/{id}/liberar` | JWT | Liberar espacio |

### Reservas

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| GET | `/api/reservas` | JWT | Listar reservas (filtro por `?usuarioId=`) |
| GET | `/api/reservas/mis-reservas` | JWT | Reservas del usuario autenticado |
| GET | `/api/reservas/pendientes` | JWT (ADMIN) | Reservas pendientes de aprobación |
| POST | `/api/reservas` | JWT | Crear reserva |
| POST | `/api/reservas/{id}/aprobar` | JWT (ADMIN) | Aprobar reserva |
| POST | `/api/reservas/{id}/rechazar` | JWT (ADMIN) | Rechazar reserva |

---

## 5. Configuración y Ejecución

### Requisitos

- Java 24
- MySQL 8+
- Maven (incluye wrapper)

### Base de datos

```sql
CREATE DATABASE smart_parking;
```

Configurar conexión en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3307/smart_parking
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
server.port=8080
```

> Hibernate crea las tablas automáticamente. No requiere scripts SQL.

### Ejecutar

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

Servidor en: `http://localhost:8080`

---

## 6. Pruebas

Se implementaron pruebas unitarias con JUnit 5 y Mockito:

```
5 tests ejecutados (BUILD SUCCESS)
```

### Casos de prueba

| Clase | Prueba | Descripción |
|---|---|---|
| `BackendApplicationTests` | `contextLoads` | Contexto Spring Boot carga correctamente |
| `UsuarioServiceTest` | `registrarGuardaPasswordEncriptado` | Password se guarda con BCrypt, rol asignado es USER |
| `UsuarioServiceTest` | `loginCorrecto` | Login con credenciales válidas retorna usuario |
| `BusinessRulesTest` | `actualizar_debeRechazarVehiculoQueNoPerteneceAlUsuarioAutenticado` | Usuario no puede editar vehículo ajeno |
| `BusinessRulesTest` | `crearReserva_debeRechazarDuplicadosParaElMismoVehiculoYFecha` | No se permiten reservas duplicadas |

### Ejecutar tests

```bash
# Windows
.\mvnw.cmd test

# Linux/Mac
./mvnw test
```

---

## 7. Seguridad

- Autenticación mediante JWT con 8 horas de expiración
- Filtro `JwtAuthenticationFilter` que valida token en cada request
- Contraseñas encriptadas con BCrypt
- Normalización de roles (`ADMIN`, `USER`, `ROLE_ADMIN`, `ROLE_USER`)
- Validación de propietario en servicios de vehículos y reservas
- Entry point personalizado para errores 401

---

## 8. Base de Datos

### Entidades JPA

| Entidad | Tabla | Relaciones |
|---|---|---|
| `Usuario` | `usuario` | - |
| `Vehiculo` | `vehiculo` | `@ManyToOne` → Usuario |
| `Espacio` | `espacio` | `@ManyToOne` → Vehiculo |
| `Reserva` | `reserva` | `@ManyToOne` → Usuario, Vehiculo, Espacio |

### Estados de Espacio

- `LIBRE` — Disponible para asignación
- `OCUPADO` — Asignado a un vehículo
- `RESERVADO` — Reservado pendiente de confirmación

---

## 9. Integrantes

- Benjamin Correa
- Jaime Guevara
- Gustavo Asencios

---

## 10. Estado Actual

- Autenticación JWT funcional con registro y login
- Roles ADMIN/USER operativos
- CRUD completo de vehículos con validación de propietario
- CRUD completo de espacios
- Asignación de espacios con validación anti-duplicados
- Sistema de reservas: creación, aprobación y rechazo
- DTOs para todas las entidades principales
- 5 tests unitarios ejecutándose correctamente
- Integración total con frontend Angular
