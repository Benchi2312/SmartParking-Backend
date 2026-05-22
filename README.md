# Smart Parking - Backend

## 1. Descripción del Proyecto

Este proyecto corresponde al backend del sistema Smart Parking, desarrollado con Spring Boot.

Permite la gestión de:

- autenticación de usuarios
- registro
- control de roles (ADMIN y USER)
- gestión de vehículos
- gestión de espacios de estacionamiento
- reservas
- dashboard administrativo

El sistema expone endpoints REST que son consumidos por un frontend desarrollado en Angular.

---

# 2. Tecnologías Utilizadas

- Java 24
- Spring Boot 4
- Spring Web (REST API)
- Spring Data JPA / Hibernate
- Spring Security
- JWT Authentication
- MySQL
- BCrypt Password Encoder
- Maven
- JUnit y Mockito (pruebas unitarias)

---

# 3. Arquitectura del Proyecto

El proyecto sigue una arquitectura por capas:

```text
src/main/java/com/smartparking/backend/
```

## Estructura

```text
config/          -> Configuración general y seguridad
controller/      -> Controladores REST
service/         -> Interfaces de negocio
service/impl/    -> Implementaciones de servicios
repository/      -> Acceso a datos con JPA
model/           -> Entidades JPA
dto/             -> Objetos de transferencia
security/        -> JWT y autenticación
exception/       -> Manejo global de errores
```

---

# 4. Funcionalidades Implementadas

## Autenticación y Seguridad

- Registro de usuarios
- Login con JWT
- Contraseñas encriptadas con BCrypt
- Roles ADMIN y USER
- Protección de rutas

---

## Gestión de Vehículos

El usuario puede:

- registrar vehículos
- editar vehículos
- eliminar vehículos
- visualizar sus vehículos

---

## Gestión de Espacios

El administrador puede:

- crear espacios
- editar espacios
- eliminar espacios
- asignar espacios a vehículos
- cambiar estados:
  - LIBRE
  - OCUPADO
  - RESERVADO

---

## Reservas

- creación de reservas
- historial de reservas
- visualización de espacios asignados

---

# 5. Endpoints REST Principales

## 5.1 Registro de Usuario

```http
POST /api/auth/register
```

### Request

```json
{
  "nombre": "Juan",
  "email": "juan@gmail.com",
  "password": "123456"
}
```

### Response

```json
{
  "id": 1,
  "nombre": "Juan",
  "email": "juan@gmail.com",
  "rol": "USER"
}
```

---

## 5.2 Login

```http
POST /api/auth/login
```

### Request

```json
{
  "email": "admin@gmail.com",
  "password": "admin123"
}
```

### Response

```json
{
  "token": "jwt_token",
  "usuario": {
    "id": 1,
    "nombre": "Admin",
    "email": "admin@gmail.com",
    "rol": "ADMIN"
  }
}
```

---

## 5.3 Vehículos

### Listar vehículos

```http
GET /api/vehiculos
```

### Crear vehículo

```http
POST /api/vehiculos
```

---

## 5.4 Espacios

### Listar espacios

```http
GET /api/espacios
```

### Crear espacio

```http
POST /api/espacios
```

---

## 5.5 Reservas

### Listar reservas

```http
GET /api/reservas
```

### Crear reserva

```http
POST /api/reservas
```

---

# 6. Configuración y Ejecución

## 6.1 Base de Datos

Crear base de datos:

```sql
CREATE DATABASE smart_parking;
```

---

## 6.2 Configuración application.properties

```properties
spring.datasource.url=jdbc:mysql://localhost:3307/smart_parking
spring.datasource.username=root
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

server.port=8080
```

IMPORTANTE:

Si MySQL usa otro puerto cambiar:

```properties
3307
```

por:

```properties
3306
```

---

# 7. Ejecutar el Proyecto

## Desde IntelliJ

Ejecutar:

```text
BackendApplication.java
```

---

## Desde consola

Windows:

```bash
.\mvnw.cmd spring-boot:run
```

Linux/Mac:

```bash
./mvnw spring-boot:run
```

Servidor:

```text
http://localhost:8080
```

---

# 8. Pruebas

Se implementaron pruebas unitarias usando:

- JUnit
- Mockito

Se validó:

- registro de usuario
- login correcto
- validación de contraseña
- generación de password BCrypt

---

## Ejecutar tests

Windows:

```bash
.\mvnw.cmd test
```

Linux/Mac:

```bash
./mvnw test
```

Resultado esperado:

```text
BUILD SUCCESS
```

---

# 9. Evidencias del Funcionamiento



- Backend ejecutándose en IntelliJ
- Endpoints probados en Postman
- Base de datos MySQL con registros creados
- Login y registro funcionando
- CRUD de vehículos
- CRUD de espacios
- Dashboard admin y user
- Tests ejecutados correctamente

---

# 10. Base de Datos

Se incluye archivo:

```text
smart_parking.sql
```

Importar en MySQL antes de ejecutar el backend.

---

# 11. Integrantes

- Benjamin Correa
- Jaime Guevara
- Gustavo Asencios

---

# 12. Estado Actual del Proyecto

Actualmente el sistema cuenta con:

- autenticación JWT funcional
- roles ADMIN y USER
- persistencia con JPA/Hibernate
- CRUD de vehículos
- CRUD de espacios
- reservas básicas
- dashboard administrativo
- dashboard de usuario
- seguridad con Spring Security