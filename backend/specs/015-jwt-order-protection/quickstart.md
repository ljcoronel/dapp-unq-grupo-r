# Validación rápida

## Prerrequisitos

- Java 25 disponible mediante el toolchain de Gradle.
- Docker disponible para PostgreSQL Testcontainers.
- Ejecutar los comandos desde `backend`.

## Compilación y pruebas focalizadas

```powershell
.\gradlew.bat :app:compileTestJava
.\gradlew.bat :app:test --tests com.dappunq.e2e.OrderManagementControllerE2ETest
.\gradlew.bat :app:test --tests com.dappunq.security.JwtAuthenticationFilterTest
```

Resultado esperado: compilación exitosa y todos los escenarios E2E de órdenes pasan.
Los escenarios deben cubrir ausencia de header, header no Bearer, JWT inválido,
JWT vencido, usuario eliminado, JWT válido, mismatch de identidad, y errores
funcionales que siguen devolviendo sus statuses previos. En los rechazos se verifica
que el repositorio de órdenes y los saldos/reservas permanezcan sin cambios.

## Validación del contrato

```powershell
.\gradlew.bat :app:processResources
```

Verificar que `app/src/main/resources/static/openapi.yaml` contiene:

- `components.securitySchemes.bearerAuth` con `type: http`, `scheme: bearer` y
  `bearerFormat: JWT`.
- `POST /orders/buy` y `POST /orders/sell` con `security: bearerAuth`.
- Respuesta `401` en ambos paths.
- El esquema `CrearOrdenRequest` y las respuestas funcionales existentes.

La fuente de verdad debe seguir siendo
`specs/005-swagger/contracts/openapi.yaml`; el archivo estático se obtiene mediante
la tarea de copia de `app/build.gradle`.

## Validación manual de comportamiento

1. Obtener un JWT mediante `POST /login`.
2. Enviar una orden válida con `Authorization: Bearer <jwt>` y el `usuarioId` del
   usuario autenticado: esperar `201` y la respuesta funcional normal.
3. Repetir sin header, con un token aleatorio y con un token expirado: esperar `401`;
   confirmar que no aparece una nueva orden ni cambia el saldo.
4. Enviar un JWT válido con el `usuarioId` de otra cuenta: esperar el rechazo uniforme
   de autorización y confirmar que no se crea una orden atribuida a esa cuenta.
