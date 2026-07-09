# ============================================================
# STAGE 1: Build — compila el JAR con Maven Wrapper + JDK 21
# ============================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copia Maven Wrapper y pom.xml primero (para cachear dependencias)
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copia el código fuente y compila (skip tests por velocidad)
COPY src src
RUN ./mvnw package -DskipTests -B

# ============================================================
# STAGE 2: Runtime — JRE mínimo para ejecutar el JAR
# ============================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copia el JAR desde el builder
COPY --from=builder /app/target/*.jar app.jar

# Puerto que Render usará (o el que defina la variable PORT)
EXPOSE 8080

# Usuario no-root para seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]
