# Smart Parking - Backend

## 1. Descripción del Proyecto

Este proyecto corresponde al backend del sistema Smart Parking, desarrollado con Spring Boot.  
Permite la gestión de usuarios mediante autenticación, registro y control de roles (ADMIN y USER).

El sistema expone endpoints REST que son consumidos por un frontend desarrollado en Angular.

---

## 2. Tecnologías Utilizadas

- Java 17
- Spring Boot 4
- Spring Web (REST API)
- Spring Data JPA
- MySQL
- Spring Security (encriptación de contraseñas con BCrypt)
- Maven
- JUnit y Mockito (pruebas unitarias)

---

## 3. Estructura del Proyecto

El proyecto sigue una arquitectura por capas:

src/main/java/com/smartparking/backend/

- config/        → Configuración (SecurityConfig)
- controller/    → Controladores REST (AuthController)
- service/       → Interfaces de negocio
- service/impl/  → Implementaciones de servicios
- repository/    → Acceso a datos (JPA)
- model/         → Entidades (Usuario)
- dto/           → Objetos de transferencia (LoginRequest)

---

## 4. Endpoints REST

### 4.1 Registro de usuario

POST /api/auth/register

Request:
{
"nombre": "Juan",
"email": "juan@gmail.com",
"password": "123456"
}

Response:
{
"id": 1,
"nombre": "Juan",
"email": "juan@gmail.com",
"rol": "USER"
}

---

### 4.2 Login

POST /api/auth/login

Request:
{
"email": "admin",
"password": "admin123"
}

Response:
{
"id": 1,
"nombre": "Admin",
"email": "admin",
"rol": "ADMIN"
}

---

### 4.3 Listar usuarios

GET /api/usuarios

Response:
[
{
"id": 1,
"nombre": "Admin",
"email": "admin",
"rol": "ADMIN"
}
]

---

## 5. Configuración y Ejecución

### 5.1 Base de Datos

Configurar en application.properties:

spring.datasource.url=jdbc:mysql://localhost:3307/smart_parking  
spring.datasource.username=root  
spring.datasource.password=  
spring.jpa.hibernate.ddl-auto=update

---

### 5.2 Ejecutar el Proyecto

Desde IntelliJ:
- Ejecutar la clase BackendApplication

Desde consola:
mvn spring-boot:run

Servidor:
http://localhost:8080

---

## 6. Pruebas

Se implementaron pruebas unitarias usando JUnit y Mockito para validar:

- Registro de usuario
- Login correcto
- Validación de contraseña

Ejecución:
- Click derecho en la clase de test → Run

Resultado esperado:
BUILD SUCCESS

---

## 7. Evidencias del Funcionamiento

Para el informe se incluyen capturas de:

- Proyecto ejecutándose en IntelliJ (log de Spring Boot)
- Endpoints probados en Postman (respuestas 200 OK)
- Base de datos con registros creados
- Pruebas unitarias ejecutadas correctamente

---

## 8. Autor

Proyecto desarrollado como parte del curso de desarrollo backend con Spring Boot.