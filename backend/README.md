# Backend API - Dapp Unq Grupo R

Este backend expone la API de autenticación, perfil de usuario y catálogo
público de jugadores.

## Requisitos
- Java 25
- PostgreSQL local en `localhost:5432` con base `dappfc`
- Gradle 8.14.2 (uso del wrapper incluido)
- Docker, si se ejecutan las pruebas de integración con Testcontainers

## Ejecución local
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

## Endpoints principales
- `GET /users/{id}/`
- `POST /login`
- `POST /register`
- `GET /players`
- `GET /players/{id}`

Las rutas `GET /players` y `GET /players/{id}` son públicas y leen únicamente
el snapshot persistido en PostgreSQL. `POST /scrape` actualiza el catálogo desde
WhoScored y mantiene el snapshot anterior si el proveedor falla o responde sin
jugadores. Los valores estadísticos no disponibles se serializan como `null`,
mientras que el valor `0` se conserva.

## Tests
```bash
./gradlew :app:test
```
