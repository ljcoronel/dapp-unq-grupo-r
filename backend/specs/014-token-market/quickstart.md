# Quickstart de validación

## Prerrequisitos

- Java 25 y el wrapper Gradle del backend.
- Docker Desktop iniciado para Testcontainers.
- Ejecutar comandos desde `backend`.

## Compilar y ejecutar tests

```powershell
.\gradlew.bat :app:test
```

La suite debe compilar y pasar los tests unitarios, de integración PostgreSQL
con Testcontainers y E2E MockMvc. Si Docker no está disponible, los tests de
integración no cumplen el prerrequisito de esta validación.

## Escenarios mínimos

1. Registrar/autenticar dos usuarios y preparar una tenencia de jugador.
2. Ejecutar `POST /orders/sell` con una cantidad válida; verificar estado
   `PENDIENTE`, fecha de emisión y tokens bloqueados.
3. Ejecutar `POST /orders/buy` con rango compatible; verificar que ambas órdenes
   pasan a `EJECUTADA`, tienen fecha de ejecución, se transfieren tokens/créditos
   y se libera cualquier reserva excedente.
4. Crear una orden sin contraparte y consultar
   `GET /users/{id}/orders`; verificar que sólo aparece esa orden pendiente.
5. Consultar `GET /users/{id}/transactions`; verificar que sólo aparecen
   órdenes ejecutadas propias con precio liquidado y contraparte.
6. Crear una compra abastecible por el Superusuario; verificar ejecución
   prioritaria. Repetir con inventario insuficiente y verificar que queda
   pendiente.
7. Probar cantidades cero/fraccionarias, importes negativos o con más de dos
   decimales, saldo/tokens insuficientes, usuario ajeno y rangos incompatibles;
   verificar rechazo y ausencia de cambios parciales.

La definición exacta de requests y respuestas está en
[contracts/orders-api.md](./contracts/orders-api.md); las invariantes y
transiciones están en [data-model.md](./data-model.md).
