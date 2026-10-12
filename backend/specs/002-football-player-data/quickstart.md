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

1. Verificar que Docker Desktop esté en ejecución.
2. Confirmar que la base `dappfc` exista en PostgreSQL local.
3. Ejecutar el backend con Spring Boot:

```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

4. Validar que la API haya levantado y esté disponible en el puerto por defecto del servicio REST.

## Validación funcional

### GET /players

```bash
curl -i "http://localhost:8080/players?liga=La%20Liga&equipo=Real%20Madrid&posicion=Delantero"
```

Resultado esperado:
- 200 OK con un arreglo JSON de jugadores que cumplen los filtros
- Si no hay coincidencias, retornar lista vacía con 200

### GET /players/{id}

```bash
curl -i http://localhost:8080/players/123
```

Resultado esperado:
- 200 OK con el detalle del jugador
- 404 si el id no existe

### Casos de validación de integridad

```bash
curl -i http://localhost:8080/players/999999
```

Resultado esperado:
- 404 con un mensaje claro indicando que el jugador no existe

## Validación de pruebas

```bash
cd backend
./gradlew test
```

La suite debe usar Testcontainers y PostgreSQL real para pruebas de integración y E2E, sin depender del perfil local del entorno de desarrollo.
