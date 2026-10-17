# Quickstart de validacion

## Prerrequisitos

- Java 25 y Gradle disponibles.
- PostgreSQL local o Docker, con las propiedades del backend configuradas.
- Chromium instalado para Playwright si se prueba el scraping real.

## Validacion automatizada

Desde `backend`:

```powershell
.\gradlew.bat :app:test
```

La suite debe cubrir invariantes de pesos y normalizacion, redondeo a dos decimales,
reemplazo de una estrategia pendiente, unicidad de tenencia, rollback ante scrape
fallido, ejecucion del scheduler con ventana fija y endpoints MockMvc. Las pruebas de
persistencia usan Testcontainers PostgreSQL y las E2E permanecen en `com.dappunq.e2e`.

## Escenarios manuales

1. Autenticar un Usuario y configurar Estrategia A con pesos 30, 25, 15, 15, 15.
   Consultar la estrategia y verificar que la primera configuracion es `ACTUAL`.
   Para Estrategia B, enviar pesos solo para `passSuccessPercentage`, `aerialsWon`,
   `minutesPlayed` y `appearances`, con suma 95%, y verificar que `position` se aplica
   automaticamente con 5% y valor 1 únicamente para `Defender`.
2. Consultar tenencias y comprobar que `valorUnitario` tiene como maximo dos decimales
   y que `valorTotal = tokens * valorUnitario`.
   Verificar tambien que la ejecucion creo un snapshot historico con la cantidad de
   Token poseida y el valor unitario individual por Jugador.
3. Enviar una nueva configuracion valida. Verificar que queda `PENDIENTE` y que el
   valor actual no cambia antes de la ventana dominical.
4. Ejecutar el servicio de ventana con un reloj controlado. Verificar el orden
   `scrape("50")`, aplicacion de pendiente y valuacion, y que una segunda ejecucion de la
   misma ventana no duplica filas ni reaplica el cambio.
5. Forzar un error de scraping. Verificar que se informa el fallo y se conserva la
   ultima valuacion valida.
6. Invocar `POST /quotes/recalculate` sin cabecera `Authorization`. Verificar que la
   solicitud es aceptada sin autenticacion, ejecuta `scrape("50")` antes del calculo y
   devuelve el resultado o el error operativo, agrega un nuevo snapshot historico y no
   crea tokens duplicados.

El contrato de endpoints y esquemas se encuentra en
[`contracts/openapi.yaml`](contracts/openapi.yaml). El modelo persistente y sus
restricciones se describe en [`data-model.md`](data-model.md).
