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

Poblar o actualizar el catalogo por la ruta existente, antes de consultar el ranking:

```powershell
Invoke-WebRequest -Method Post -Uri "http://localhost:8080/quotes/scrape?numberOfPlayers=50"
```

El endpoint es publico y no requiere autenticacion. El contrato fuente esta en `docs/openapi.yaml`: `operationId: scrape`, parametro referenciado `NumberOfPlayers` (nombre wire `numberOfPlayers`) y respuestas 200/400. El modulo ranking no crea jugadores; lee los registros persistidos por el scraping.

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

- Ejecutar `POST /quotes/scrape` antes de las consultas; el catalogo del ranking procede solo de jugadores persistidos.
- Todos los jugadores elegibles ya persistidos aparecen exactamente una vez al recorrer todas las paginas.
- El valor mostrado es el mayor vigente y el origen coincide con Estrategia A/B.
- Sin cotizacion personalizada, se usa la cotizacion base del superusuario o `1.00`.
- Los filtros se aplican antes de ordenar, numerar y paginar.
- Los empates usan base, tenencias inversoras, nombre y playerId.
- Tras un recambio vigente el lider cambia en la siguiente consulta.
- Tras completar una nueva llamada de scraping, una consulta nueva refleja los jugadores/cotizaciones actualizados sin duplicados.
- Las cotizaciones historicas no participan.

## Flujo E2E

Las pruebas MockMvc + Testcontainers deben ejecutar la ruta HTTP real `POST /quotes/scrape?numberOfPlayers=N` con N positivo. Sustituir solo `ScrapingService` por un doble determinista que persista fixtures, evitando llamadas de red a WhoScored. Luego configurar estrategias/pesos sobre los usuarios y jugadores obtenidos, solicitar `GET /players/ranking` y comprobar valores, origen, filtros, orden y unicidad. Repetir scraping con cambios y verificar que una consulta posterior los refleja.
