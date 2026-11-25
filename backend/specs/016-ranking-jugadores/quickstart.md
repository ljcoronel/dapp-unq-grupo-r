# Quickstart de validacion

## Pre-requisitos

- Docker disponible para PostgreSQL/Testcontainers.
- JDK 25 y Gradle del wrapper del backend.

## Construccion y pruebas

Desde `backend`:

```powershell
.\gradlew.bat test
.\gradlew.bat bootRun
```

## Consultas REST

Ranking completo:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/players/ranking?page=1&limit=20"
```

Filtros combinados:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/players/ranking?page=1&limit=10&league=Premier%20League&team=Arsenal&position=Forward"
```

Sin coincidencias:

```powershell
$r = Invoke-RestMethod -Uri "http://localhost:8080/players/ranking?page=1&limit=20&league=NoExiste"
$r.total
$r.items.Count
```

Parametros invalidos:

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/players/ranking?page=0&limit=101"
```

## Validaciones esperadas

- Todos los jugadores activos aparecen exactamente una vez al recorrer todas las paginas.
- El valor mostrado es el mayor vigente y el origen coincide con Estrategia A/B.
- Sin cotizacion personalizada, se usa la cotizacion base del superusuario o `1.00`.
- Los filtros se aplican antes de ordenar, numerar y paginar.
- Los empates usan base, tenencias inversoras, nombre y playerId.
- Tras un recambio vigente el lider cambia en la siguiente consulta.
- Las cotizaciones historicas no participan.
