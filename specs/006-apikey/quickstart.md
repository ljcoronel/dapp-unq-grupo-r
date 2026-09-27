# Quickstart: validar autenticacion por API Key

## Requisitos previos

- Java 25 y el Gradle Wrapper del repositorio.
- PostgreSQL disponible para levantar la aplicacion local.
- Docker disponible para las pruebas de integracion con Testcontainers.
- La aplicacion backend ejecutandose en `http://localhost:8080`.

## Ejecutar pruebas

Desde `backend` en PowerShell:

```powershell
.\gradlew.bat :app:test
```

Para ejecutar solo la prueba E2E de autenticacion (una vez implementada):

```powershell
.\gradlew.bat :app:test --tests "com.dappunq.e2e.ApiKeyAuthenticationE2ETest"
```

Las pruebas deben demostrar acceso `401` sin credenciales a rutas protegidas, acceso con JWT o API Key valida, rechazo de claves vacias, malformadas, duplicadas, invalidas, vencidas o revocadas, rechazo de ambos mecanismos de credencial juntos y rechazo de API Key en login/registro antes de producir efectos. Las pruebas de persistencia deben ejecutarse contra PostgreSQL de Testcontainers.

## Escenario manual de aceptacion

1. Registrar un usuario con `POST /register` y autenticarlo con `POST /login`; conservar el token de respuesta solo en el entorno local.
2. Crear una clave con `POST /api-keys` y `Authorization: Bearer <token>`, enviando un `label` y, opcionalmente, `expiresAt`. Guardar la clave devuelta de forma temporal: se muestra una sola vez.
3. Llamar `GET /players` con `x-api-key: <clave-devuelta>` y comprobar que se obtiene `200`.
4. Repetir la consulta sin credenciales y comprobar `401`; usar una clave invalida, vencida o revocada y comprobar que tambien se rechaza.
5. Enviar la clave en `POST /login` o `POST /register` y comprobar rechazo sin cambio de estado; enviar ambos headers a una ruta protegida y comprobar rechazo generico.
6. Listar `GET /api-keys` y comprobar que aparecen metadatos sin secreto; revocar la clave propia con `DELETE /api-keys/{id}` y comprobar que el siguiente acceso con esa clave se rechaza.
7. Repetir una ruta con JWT valido y comprobar que el flujo Bearer preexistente sigue funcionando. Confirmar que `/swagger-ui.html` y `/openapi.yaml` tambien requieren autenticacion segun el alcance aprobado.

La especificacion de endpoints, campos y esquemas esta en [contracts/api-keys.yaml](contracts/api-keys.yaml); las reglas de persistencia y estados estan en [data-model.md](data-model.md).
