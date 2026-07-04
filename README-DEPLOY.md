# Deploy — Smart Parking Backend (Railway)

## Variables de Entorno

| Variable | Obligatoria | Descripción | Ejemplo |
|---|---|---|---|
| `DATABASE_URL` | Sí | URL JDBC de la base de datos MySQL en Railway | `jdbc:mysql://<host>:<port>/<db>?useSSL=true&requireSSL=true` |
| `DATABASE_USERNAME` | Sí | Usuario de la base de datos | `root` |
| `DATABASE_PASSWORD` | Sí | Contraseña de la base de datos | |
| `JWT_SECRET` | Sí | Clave secreta para firmar tokens JWT (mínimo 256 bits / 32 caracteres) | `una-frase-segura-de-al-menos-32-caracteres` |
| `CORS_ALLOWED_ORIGINS` | Sí | Orígenes permitidos separados por coma (sin espacios) | `http://localhost:4200,https://tudominio.vercel.app` |
| `PORT` | No | Puerto del servidor (Railway lo asigna automáticamente; default 8080) | `8080` |

## Valores por Defecto (desarrollo local)

Si no se configuran las variables de entorno, el backend usa estos valores:

- `DATABASE_URL` → `jdbc:mysql://localhost:3307/smart_parking`
- `DATABASE_USERNAME` → `root`
- `DATABASE_PASSWORD` → *(vacío)*
- `JWT_SECRET` → `smart-parking-jwt-secret-key-for-university-demo-2026`
- `CORS_ALLOWED_ORIGINS` → `http://localhost:4200`
- `PORT` → `8080`

## Comandos

```bash
# Desarrollo local
.\mvnw.cmd spring-boot:run

# Tests
.\mvnw.cmd test

# Build
.\mvnw.cmd package -DskipTests
```

## Notas

- `ddl-auto=update`: Hibernate crea/actualiza las tablas automáticamente en el primer arranque. No se requiere Flyway/Liquibase.
- Los tests usan H2 en memoria (`src/test/resources/application.properties`), no afectan la base de datos de producción.
