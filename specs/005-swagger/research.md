# Investigacion: documentacion interactiva y contrato de API

## Dependencia OpenAPI para Spring Boot 4

**Decision**: Usar `org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1` en `backend/app/build.gradle`.

**Rationale**: El proyecto usa Spring Boot 4.0.0, Java 25, Gradle 9.1.0 y Spring MVC. La documentacion de Springdoc dirige los proyectos Spring Boot 4 a Springdoc 3.x; la linea 2.x es para Boot 3. La variante WebMVC incluye la UI y no requiere plugin Gradle independiente. Fijar la version evita actualizaciones flotantes.

**Alternatives considered**:
- Springdoc 2.x / starter descrito para Boot 3: no corresponde a Spring Boot 4.
- Swagger UI separada o una UI nueva completa: agrega otro runtime/superficie y no es necesaria para servir una UI integrada.
- Anotaciones de Springdoc o bean `OpenAPI` como definicion completa: generarian otra fuente mantenida en paralelo al contrato versionado, en conflicto con RF-012/RF-013.

**Sources**:
- [Springdoc OpenAPI README v3.1.1](https://github.com/springdoc/springdoc-openapi/tree/v3.1.1)
- [Springdoc 3.1.1 release](https://github.com/springdoc/springdoc-openapi/releases/tag/v3.1.1)
- [Springdoc Maven metadata](https://repo.maven.apache.org/maven2/org/springdoc/springdoc-openapi-starter-webmvc-ui/maven-metadata.xml)
- [Spring Boot 4.0 system requirements](https://docs.spring.io/spring-boot/4.0/system-requirements.html)
- [Gradle Java compatibility](https://docs.gradle.org/current/userguide/compatibility.html)

## Fuente canonica y publicacion del YAML

**Decision**: Mantener un OpenAPI YAML versionado en `specs/005-swagger/contracts/openapi.yaml`; configurarlo como URL de Swagger UI (`/openapi.yaml`) y copiarlo al recurso `static` empaquetado por Gradle.

**Rationale**: La UI puede cargar un archivo OpenAPI personalizado mediante `springdoc.swagger-ui.url`. El YAML en `specs/005-swagger` permite revisar cambios de contrato con la especificacion, mientras que la tarea de recursos publica ese mismo archivo sin copiarlo manualmente. Tags, descripciones, modelos y seguridad no se vuelven a declarar con anotaciones/beans. El starter debe configurarse para que la interfaz presente el archivo versionado y no el documento autogenerado.

**Alternatives considered**:
- OpenAPI generado unicamente desde anotaciones: describe la implementacion, pero no conserva el artefacto de contrato versionado como unica especificacion canonica solicitada.
- YAML solo en `src/main/resources/static`: facil de publicar, pero separa el contrato de los artefactos de especificacion y su trazabilidad.
- Dos copias del YAML (una en `specs`, otra en `resources`): facilita edicion local de recursos, pero permite divergencia; se rechaza.

**Implementation constraint**: Verificar que el UI inicialice correctamente con `springdoc.swagger-ui.url` cuando el endpoint autogenerado se deshabilita. Si el UI necesita el endpoint de configuracion de Springdoc, permitir solo la configuracion necesaria, sin usar el OpenAPI autogenerado como documento publicado. No se considerara completa la configuracion hasta comprobar en una prueba HTTP que el UI carga `/openapi.yaml`.

**Sources**:
- [Springdoc properties: Swagger UI](https://springdoc.org/properties.html)
- [Swagger UI configuration](https://swagger.io/docs/open-source-tools/swagger-ui/usage/configuration/)
- [OpenAPI 3.0 bearer authentication](https://swagger.io/docs/specification/v3_0/authentication/bearer-authentication/)

## Localizacion, confirmacion y accesibilidad de Swagger UI

**Decision**: Mantener Springdoc/Swagger UI como base, y servir una personalizacion de inicializacion/plugin versionada para traducir el chrome visible al espanol y requerir confirmacion antes de ejecutar `POST /register`. Fijar y probar la version de Swagger UI compatible con el starter.

**Rationale**: Swagger UI no documenta una propiedad de idioma y no dispone de confirmacion nativa por operacion. Deshabilitar Try it out por metodo no permite una advertencia selectiva para una sola ruta. Un componente/plugin personalizado puede interceptar la ejecucion; un dialogo accesible debe anunciar proposito, conservar/gestionar foco, permitir teclado y devolver foco al control que lo invoco. El mecanismo es UX, no control de autorizacion.

**Alternatives considered**:
- Solo redactar descripciones OpenAPI en espanol: no traduce controles y textos propios de la interfaz.
- Deshabilitar toda ejecucion `POST`: evita el registro de prueba, pero incumple la posibilidad de probar operaciones habilitadas.
- Interceptor de red: no proporciona una forma documentada de cancelar limpiamente una solicitud basada en una decision del usuario.
- Fork completo de Swagger UI: ofrece control de traduccion, pero aumenta el mantenimiento; reservar como alternativa si la personalizacion acotada no resulta sostenible.

**Sources**:
- [Swagger UI configuration](https://swagger.io/docs/open-source-tools/swagger-ui/usage/configuration/)
- [Swagger UI plugin API](https://swagger.io/docs/open-source-tools/swagger-ui/customization/plugin-api/)
- [Swagger UI localization issue](https://github.com/swagger-api/swagger-ui/issues/2488)
- [WAI-ARIA alert dialog pattern](https://www.w3.org/WAI/ARIA/apg/patterns/alertdialog/)

## Seguridad y estado real del backend

**Decision**: Documentar las operaciones como publicas y declarar un componente HTTP bearer/JWT disponible para el boton Authorize, sin aplicar `security` a las operaciones actuales ni cambiar `SecurityConfig`.

**Rationale**: El `SecurityConfig` actual permite anonimamente `POST /login`, `POST /register`, `GET /users/**` y `GET /players/**`. El login entrega JWT en la cabecera de respuesta `Authorization`, pero ninguna de las rutas documentadas exige actualmente el token. Marcar el endpoint de perfil como protegido, como se proponia inicialmente, describiria un comportamiento falso. Un cambio real de politica de seguridad requiere alcance separado.

**Alternatives considered**:
- Marcar `GET /users/{id}/` con `bearerAuth`: contradice los permisos ejecutables y las pruebas actuales.
- Retirar el esquema bearer: ocultaria el formato de JWT emitido por login y no ofreceria Authorize para futuras operaciones protegidas.

## Fuentes y paridad del contrato

**Decision**: Consolidar las rutas y modelos actuales en un contrato 1.0.0, añadir prueba de paridad contra mappings MVC y validacion OpenAPI en CI; incluir los cambios a `specs/005-swagger/**` en filtros de CI.

**Rationale**: Los contratos previos de autenticacion y jugadores describen versiones divergentes del backend actual. La revision de controladores, DTOs, excepciones y pruebas actuales es la fuente para construir una linea base verdadera. CI debe ejecutarse cuando cambie el archivo canónico aunque no haya cambios bajo `backend/`.

**Alternatives considered**:
- Conservar contratos anteriores como varios documentos publicados: crea definiciones conflictivas para las mismas rutas.
- Revisión manual sin chequeos de CI: no detecta de manera repetible endpoints omitidos ni rutas sin contrato.

**Sources**:
- [Redocly CLI: lint](https://redocly.com/docs/cli/commands/lint)
- Contratos actuales a reconciliar: `specs/001-user-auth/contracts/user-auth-api.yaml`, `specs/002-football-player-data/contracts/players-api.yaml` y `specs/003-player-catalog/contracts/players-api.yaml`.

## Observaciones locales

- El backend es `backend/app`, un proyecto Gradle Spring Boot 4.0.0/Java 25; el wrapper es Gradle 9.1.0.
- Las operaciones actuales son `POST /login`, `POST /register`, `GET /users/{id}/`, `GET /players` y `GET /players/{id}`.
- `GET /users/{id}/` devuelve errores con la propiedad `message`; las operaciones de jugadores devuelven `mensaje`. No unificar esos esquemas en esta funcionalidad.
- `POST /register` crea un usuario; por lo tanto requiere una advertencia previa a su ejecucion desde Swagger UI. `POST /login` no se documenta como mutacion persistente.
- El CI existente ejecuta compilacion y pruebas Gradle y solo se activa hoy por cambios bajo `backend/**` o el workflow. Debe incluir los cambios del contrato.
