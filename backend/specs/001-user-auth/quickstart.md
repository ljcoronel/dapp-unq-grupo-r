# Quickstart: ejecución local y validación del feature

## Requisitos previos

- Docker Desktop ejecutándose
- PostgreSQL corriendo en `localhost:5432`
- Base de datos `dappfc` creada
- Perfil local de la app configurado con:
  - usuario: `postgres`
  - password: `root`
  - URL: `jdbc:postgresql://localhost:5432/dappfc`

## Arranque local

1. Iniciar PostgreSQL local o verificar que la base `dappfc` exista.
2. Configurar el perfil `local` en la aplicación.
3. Ejecutar el backend con Spring Boot:

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

4. Verificar que la app levanta en el puerto por defecto del servicio REST.

## Validación funcional

### GET /users/{id}/

```bash
curl -i http://localhost:8080/users/1/
```

Resultado esperado:
- 200 si existe el usuario
- 404 con "Usuario no encontrado" si no existe

### POST /login

```bash
curl -i -X POST http://localhost:8080/login \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"usuario1","password":"secret"}'
```

Resultado esperado:
- 200 con cuerpo `{"nombre":"usuario1"}` y header `Authorization: Bearer <token>`
- 400 con "Credenciales inválidas" si falla

### POST /register

```bash
curl -i -X POST http://localhost:8080/register \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"nuevoUsuario","password":"secret"}'
```

Resultado esperado:
- 200 con `{"nombre":"nuevoUsuario"}` para usuarios nuevos
- 400 con "Usuario existente" si el nombre ya existe

## Validación de tests

```bash
./gradlew test
```

La suite debe utilizar Testcontainers para DB real, sin depender del perfil local para pruebas automatizadas.
