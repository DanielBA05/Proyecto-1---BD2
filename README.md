# Proyecto Clínica - Spring Boot + Oracle + Thymeleaf

Este proyecto implementa:
- Login con Spring Security por roles `DOCTOR` y `PACIENTE`
- Registro de pacientes con `username` único
- Reserva de bloques horarios por doctor sin solapamientos
- Creación manual de citas dentro de bloques reservados
- Reserva concurrente de citas por pacientes con bloqueo pesimista
- Interfaz simple con Thymeleaf

## Stack
- Java 17
- Spring Boot 3
- Spring Security
- Spring Data JPA / Hibernate
- Oracle
- Thymeleaf

## Estructura
- `src/main/java/com/clinica/...`
- `src/main/resources/templates/...`
- `sql/01_schema_updates.sql`
- `sql/02_triggers.sql`

## Cómo correr
1. Crear / ajustar la base con los scripts de `sql/`.
2. Insertar manualmente los doctores en `USUARIO` y `DOCTOR`.
3. Verificar `application.properties`.
4. Ejecutar:
   ```bash
   mvn spring-boot:run
   ```
5. Abrir `http://localhost:8080/login`

## Inserción manual de doctor (ejemplo)
```sql
INSERT INTO USUARIO (ID_USUARIO, USERNAME, PASSWORD_HASH, CORREO, TELEFONO, NOMBRE, APELLIDO1, APELLIDO2, ROL)
VALUES (
  100,
  'doctor1',
  '$2a$10$7T7vA4nhW7A7zN4Jg7zY8eB8fR7z5vW1Q8sR0n0C2WgLr5P9f0xQK',
  'doctor1@clinica.com',
  '8888-8888',
  'Ana',
  'Ramirez',
  'Lopez',
  'DOCTOR'
);

INSERT INTO DOCTOR (ID_DOCTOR, CODIGO_PROFESIONAL, ESPECIALIDAD)
VALUES (100, 'MED-001', 'Medicina General');
COMMIT;
```
> El password del ejemplo debes reemplazarlo por un BCrypt real generado por la app o por una utilidad externa.

## Sobre concurrencia
Se usó:
- `@Transactional`
- `PESSIMISTIC_WRITE` para reservar cita o bloquear bloque
- validación de solapamientos antes de persistir

## Nota importante
Para el caso "username tomado al confirmar primero" y "cita tomada por otro al confirmar primero", la protección real está en:
- validación previa
- transacción
- bloqueo pesimista / excepción por integridad

## Mejoras futuras
- editar datos de doctor y paciente
- vista calendario más visual
- tests de integración con Oracle
- manejo más fino de mensajes de concurrencia
