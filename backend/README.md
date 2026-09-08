# Backend API - Dapp Unq Grupo R

Este backend expone la API de autenticación y perfil de usuario para el feature `001-user-auth`.

## Requisitos
- Java 25
- PostgreSQL local en `localhost:5432` con base `dappfc`
- Gradle 8.14.2 (uso del wrapper incluido)

## Ejecución local
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

## Endpoints principales
- `GET /users/{id}/`
- `POST /login`
- `POST /register`

## Tests
```bash
./gradlew test
```
