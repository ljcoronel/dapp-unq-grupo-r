# Validacion rapida

## Requisitos

- Java 25.
- Docker disponible para Testcontainers PostgreSQL.
- Ejecutar los comandos desde `backend`.

## Pruebas automatizadas

En Windows PowerShell:

```powershell
.\gradlew.bat :app:test `
  --tests 'com.dappunq.security.ApiKeyAuthenticationFilterTest' `
  --tests 'com.dappunq.e2e.QuoteRecalculateE2ETest' `
  --tests 'com.dappunq.e2e.ValuationConfigurationE2ETest' `
  --tests 'com.dappunq.e2e.HoldingsInvariantE2ETest' `
  --tests 'com.dappunq.e2e.ScrapingApiKeyE2ETest'
```

Las pruebas deben provisionar la credencial activa con el flujo existente y usar MockMvc con Spring Security. El caso de configuración ausente debe retirar la credencial solo dentro del fixture de prueba.

## Escenarios y resultados esperados

| Solicitud de prueba | Resultado esperado |
|---|---:|
| API Key valida, sin JWT | 200; el recálculo conserva su respuesta funcional actual. |
| API Key valida, junto a JWT | 200; la autorización depende de la API Key. |
| Sin `X-API-Key` o con valor vacio, con credencial activa | 401; no se invoca el recálculo. |
| Solo `Authorization: Bearer <token>` | 401; JWT no reemplaza la API Key. |
| Clave incorrecta, malformada o ambigua | 403; no se invoca el recálculo. |
| No hay credencial activa configurada | 500; no se inicia el flujo de negocio. |
| Solicitud rechazada | El log incluye timestamp, ruta, resultado y Correlation ID, pero no contiene credenciales. |
| `POST /quotes/scrape` rechazado | Mantiene 401 y el contrato existente. |

## Verificacion manual

Con la aplicacion y su base de datos configuradas y una clave activa obtenida mediante el proceso operativo existente, el cliente autorizado puede ejecutar:

```powershell
curl.exe -i -X POST http://localhost:8080/quotes/recalculate `
  -H "X-API-Key: <API_KEY>"
```

Resultado esperado: `200 OK` con el esquema de respuesta actual del recálculo. No poner claves reales en el historial del shell, reportes o logs. Para validar las respuestas 401/403/500, preferir los casos automatizados con fixtures aislados en vez de eliminar credenciales de una base compartida.

## Contrato de referencia

Consultar [contrato de recálculo](./contracts/recalculate.md) y [modelo y estados](./data-model.md).
