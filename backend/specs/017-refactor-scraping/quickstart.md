# Validación rápida

## Requisitos

- Java 25.
- Docker disponible para Testcontainers PostgreSQL.
- Ejecutar los comandos desde `backend` en Windows PowerShell.

## Pruebas automatizadas

Ejecutar la cobertura focalizada del filtro, scraping E2E, endpoint protegido y contrato OpenAPI:

```powershell
.\gradlew.bat :app:test `
  --tests 'com.dappunq.security.ApiKeyAuthenticationFilterTest' `
  --tests 'com.dappunq.e2e.ScrapingApiKeyE2ETest' `
  --tests 'com.dappunq.e2e.QuoteRecalculateE2ETest' `
  --tests 'com.dappunq.contract.ScrapingOpenApiContractTest'
```

Las pruebas E2E deben usar MockMvc con Spring Security y el PostgreSQL de Testcontainers ya configurado. El proveedor de scraping se simula para no depender de servicios externos.

## Escenarios y resultados esperados

| Solicitud | Resultado esperado |
|---|---|
| `POST /quotes/scrape?numberOfPlayers=1` sin `X-API-Key` | Respuesta exitosa; se invoca el servicio de scraping. |
| La misma solicitud con `X-API-Key` válida, inválida, arbitraria o vacía | El encabezado se ignora; mismo resultado exitoso. |
| `POST /quotes/scrape` sin query parameter | Respuesta exitosa con el valor predeterminado 50. |
| `numberOfPlayers` no numérico o cero/negativo | `400 Bad Request`; no se invoca el scraping. |
| `POST /quotes/recalculate` sin `X-API-Key` | Conserva `401 Unauthorized`; no se invoca el flujo protegido. |
| OpenAPI para `POST /quotes/scrape` | La operación declara `security: []`; otras operaciones conservan su seguridad. |

## Verificación del contrato

El contrato de la operación, incluyendo seguridad, valores del query parameter y respuestas, se describe en [contracts/scrape.md](./contracts/scrape.md). La ausencia de `numberOfPlayers` conserva el comportamiento actual; no debe probarse como 400.
