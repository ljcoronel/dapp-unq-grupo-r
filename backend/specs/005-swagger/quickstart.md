# Quickstart: documentacion interactiva y contrato OpenAPI

## Requisitos previos

- Java 25 y Gradle Wrapper incluido en `backend`.
- PostgreSQL local disponible en `localhost:5432`, base `dappfc`, para ejecutar la aplicacion con perfil `local`.
- Docker si se ejecutan pruebas de integracion/E2E con Testcontainers.

## Compilar y probar

Desde `backend`:

```powershell
.\gradlew.bat clean test
```

La verificacion de contrato debe fallar si el YAML no es OpenAPI valido, si una ruta implementada no tiene operacion correspondiente o si una operacion documentada no existe. Las pruebas de endpoint revisan cuerpos, cabeceras y estado de seguridad contra la aplicacion.

## Ejecutar localmente

Desde `backend`:

```powershell
.\gradlew.bat :app:bootRun --args="--spring.profiles.active=local"
```

Abrir `http://localhost:8080/swagger-ui.html`. La pagina debe cargar el contrato versionado disponible en `http://localhost:8080/openapi.yaml`; no debe mostrar una especificacion generada distinta.

## Escenarios de verificacion manual

1. Comprobar que la UI aparece en espanol, puede recorrerse solo con teclado y anuncia operaciones y modelos a tecnologias de asistencia.
2. Inspeccionar `Autenticacion`, `Usuarios` y `Jugadores`; confirmar las cinco rutas, parametros, schemas, respuestas y errores del [contrato](contracts/openapi.yaml).
3. Probar `POST /login` con una cuenta valida de desarrollo; revisar el JSON y la cabecera `Authorization`. No guardar el JWT como ejemplo versionado.
4. Probar `POST /register`; confirmar que la UI advierte que la operacion crea una cuenta y exige aceptar antes de enviar.
5. Consultar operaciones de usuarios y jugadores sin autorizar; todas son publicas segun la politica actual del backend. El boton Authorize permite configurar un bearer JWT temporal, pero no debe implicar que estas operaciones lo exigen.
6. Confirmar que respuestas de error conservan los esquemas actuales: `message` para autenticacion/perfil y `mensaje` para jugadores.

## Criterios de resultado

- `/swagger-ui.html` y sus recursos cargan anonimamente.
- La UI muestra la especificacion canonica desde `/openapi.yaml`; un fallo o YAML invalido debe producir un error visible, no una documentacion vacia.
- Las rutas publicadas coinciden con Spring MVC y las credenciales introducidas en UI no aparecen en el YAML ni en recursos versionados.
