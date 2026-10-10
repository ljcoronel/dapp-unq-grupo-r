# Guia de validacion

## Prerrequisitos

- Java 25 y Gradle disponibles.
- Docker disponible para Testcontainers.
- Chromium de Playwright instalado (`gradlew :app:playwright install chromium`) para la prueba real.
- PostgreSQL de Testcontainers accesible.

## Validacion automatizada

Desde `backend/`:

```powershell
.\gradlew :app:test
```

La suite debe cubrir anotaciones y unicidad de `externalId`, relaciones unidireccionales, persistencia de lotes, rechazo de registros invalidos, conservacion del snapshot, ausencia de limite artificial y permisos publicos de `POST /scrape`.

## Caso feliz real

Ejecutar la prueba de integracion etiquetada o seleccionada para `WhoScoredScrapingService.scrape` con el argumento exacto `"1"`. La prueba debe acceder a la URL real de WhoScored, parsear un registro y comprobar que el lote normalizado puede persistirse sin FootballData.

## Validacion manual del endpoint

Con la aplicacion iniciada:

```powershell
curl.exe -i -X POST "http://localhost:8080/scrape?numberOfPlayers=1"
```

Debe responder sin token JWT y delegar al servicio con `numberOfPlayers=1`. Las respuestas vacias o invalidas no deben borrar datos existentes.

## Referencias

- Modelo y reglas: [`data-model.md`](data-model.md)
- Contrato HTTP: [`contracts/scrape.md`](contracts/scrape.md)
