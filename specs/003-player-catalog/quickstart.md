# Validación rápida

## Prerrequisitos

- Java 25, Docker (para Testcontainers), PostgreSQL local o la configuración
  existente del proyecto.
- Definir `FOOTBALL_DATA_AUTH_TOKEN` con un token válido antes de arrancar el
  backend. No registrar el valor en archivos versionados.

## Arranque

Desde `backend/`:

```powershell
.\gradlew.bat :app:bootRun
```

El arranque debe intentar una request por cada código (`PL`, `BL1`, `PD`, `SA`,
`FL1`). Cada respuesta exitosa no vacía debe reemplazar los jugadores
previamente almacenados para esa liga y persistir como máximo diez jugadores
válidos. Las respuestas vacías no modifican los datos existentes y los fallos
conservan el snapshot anterior.

## Validación del contrato

```powershell
curl.exe http://localhost:8080/players
```

La request no debe incluir el header `Authorization` ni un token JWT. El token
`FOOTBALL_DATA_AUTH_TOKEN` solo es necesario para la sincronización interna con
football-data durante el arranque.

La respuesta exitosa debe ser un array exterior de cinco arrays, en orden fijo,
con los datos del último refresh exitoso de cada liga.
Cada jugador solo contiene los siete campos definidos en
[contracts/players-api.yaml](contracts/players-api.yaml), y cada array interior
tiene como máximo diez elementos. Repetir el `curl` no debe generar requests al
proveedor externo.

## Validación automatizada

Desde `backend/`:

```powershell
.\gradlew.bat :app:test
```

La suite debe incluir: unit tests del mapping y reglas de límite, integración
con PostgreSQL Testcontainers para refresh/lectura, y tests MockMvc que
comprueben la respuesta de `GET /players`, ligas vacías y valores `null` frente
a cero. La colección Postman del proyecto debe contener la request `GET
/players`.
