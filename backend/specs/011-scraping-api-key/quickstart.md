# Quickstart de validación

## Prerrequisitos

- Java 25, Gradle wrapper y PostgreSQL disponible en la configuración del
  backend.
- Docker disponible para las pruebas con Testcontainers.
- Ejecutar desde `backend`.

## Validar la rotación y obtener la clave

```powershell
.\gradlew.bat :app:bootJar
.\scripts\rotate-scraping-api-key.ps1
```

La salida estándar debe contener exactamente el nuevo valor de la API key. Un
error de conexión o persistencia debe producir código distinto de cero, mensaje
explícito en español y ninguna credencial en la salida.

Ejecutar el comando nuevamente. La segunda clave debe ser distinta, la primera
debe dejar de autorizar y la base debe conservar exactamente una credencial
activa con permiso `scraping`.

## Validar `POST /scrape`

Con la aplicación iniciada y la clave obtenida:

```powershell
curl.exe -i -X POST "http://localhost:8080/scrape?numberOfPlayers=1" `
  -H "X-API-Key: <CLAVE_GENERADA>"
```

Debe responder exitosamente y ejecutar el scraping. Sin el header, con una clave
alterada, con espacios o con la clave anterior debe responder `401`, sin invocar
`WhoScoredScrapingService`.

## Pruebas automatizadas

```powershell
.\gradlew.bat :app:test
```

Las pruebas deben cubrir el contrato HTTP en
[`contracts/scrape.md`](contracts/scrape.md), el comando en
[`contracts/rotate-scraping-api-key.md`](contracts/rotate-scraping-api-key.md),
rollback de rotación, unicidad, permiso `scraping` y ausencia de exposición de
la clave en respuestas o logs normales.
