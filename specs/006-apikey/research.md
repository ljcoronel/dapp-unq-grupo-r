# Investigacion: API Keys seguras en Spring Security

## Decision 1: Mantener autenticacion stateless con token/provider dedicado

**Decision**: Usar un `OncePerRequestFilter` para extraer `x-api-key`, crear un `ApiKeyAuthenticationToken` no autenticado y delegar al `AuthenticationManager`/`AuthenticationProvider` la busqueda, verificacion y autenticacion del usuario. Insertar el filtro antes del filtro JWT.

**Rationale**: Separa adaptacion HTTP de validacion de credenciales y reutiliza el modelo de autenticacion de Spring Security. El provider puede probarse sin acoplar la logica a servlet. Al ejecutar el filtro API Key antes del filtro JWT se pueden rechazar credenciales dobles antes de que uno de los filtros atribuya la solicitud a una identidad.

**Alternatives considered**: Validar completamente dentro del filtro es mas corto, pero mezcla protocolo HTTP y validacion de dominio y dificulta reuso/pruebas. No se cambia a sesiones ni se reemplaza la autenticacion JWT existente.

**References**:
- Spring Security, Authentication Architecture: https://docs.spring.io/spring-security/reference/7.0/servlet/authentication/architecture.html
- Spring Security, Servlet Architecture: https://docs.spring.io/spring-security/reference/7.0/servlet/architecture.html
- Spring Security, Authorize HTTP Requests: https://docs.spring.io/spring-security/reference/7.0/servlet/authorization/authorize-http-requests.html
- Spring Framework, `OncePerRequestFilter`: https://docs.spring.io/spring-framework/docs/7.0.0/javadoc-api/org/springframework/web/filter/OncePerRequestFilter.html

## Decision 2: Claves individuales de alta entropia, hash y selector

**Decision**: Generar 256 bits con `SecureRandom`; incluir un selector aleatorio publico en el formato transportado y almacenar el selector con SHA-256 del secreto. Comparar los hashes en tiempo constante. Mostrar el secreto solo en la respuesta de creacion.

**Rationale**: El secreto uniforme de 256 bits no se comporta como una contrasena elegida por una persona y permite verificacion eficiente con SHA-256. El selector permite busqueda indexada sin almacenar una copia del secreto ni recorrer claves. La exposicion de una clave se limita al instante de creacion.

**Alternatives considered**: Una clave compartida en configuracion no identifica al propietario ni permite revocacion individual y contradice el requisito de generacion por usuario. Guardar secretos en claro facilita filtraciones; un KDF lento no es necesario para secretos aleatorios de alta entropia.

**References**:
- Java 25 `SecureRandom`: https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/security/SecureRandom.html
- Java 25 `MessageDigest`: https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/security/MessageDigest.html
- OWASP REST Security Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html
- PostgreSQL indexes: https://www.postgresql.org/docs/current/indexes-intro.html

## Decision 3: Rechazar credenciales ambiguas y aislar la gestion de claves

**Decision**: Rechazar con error generico toda solicitud que incluya simultaneamente `Authorization: Bearer` y `x-api-key`. Rechazar cualquier `x-api-key` en `POST /login` y `POST /register` antes de ejecutar el controller. Exigir JWT en los endpoints de crear/listar/revocar claves.

**Rationale**: Una unica credencial por solicitud impide mezclar identidades y hace la politica observable y comprobable. Restringir la administracion al JWT impide que una clave robada se use para crear persistencia adicional o impedir su revocacion.

**Alternatives considered**: Dar prioridad a un header sobre otro permitiria que distintos proxies/filtros interpreten una identidad distinta. Permitir una API Key para crear otra clave ampliaria privilegios sin una necesidad definida.

**References**:
- Spring Security, authorization rules: https://docs.spring.io/spring-security/reference/7.0/servlet/authorization/authorize-http-requests.html
- OWASP REST Security Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html

## Decision 4: Validacion en capas y pruebas con infraestructura existente

**Decision**: Mantener controller/DTO, service, modelo de dominio y persistencia separados; usar pruebas unitarias para hash/modelo/provider, Testcontainers para persistencia PostgreSQL y MockMvc en el paquete E2E para validar la cadena de filtros completa.

**Rationale**: Alinea el nuevo almacenamiento y las reglas de ciclo de vida con la constitucion. La politica de seguridad solo puede validarse completamente desde MockMvc integrado con la cadena real, mientras que consultas e indices se prueban contra PostgreSQL.

**Alternatives considered**: H2 no garantiza paridad de tipos, indices ni comportamiento con PostgreSQL. No se agregan frameworks de test porque JUnit, Spring Security Test, MockMvc y Testcontainers ya estan configurados.

**References**:
- Spring Security, MockMvc authentication testing: https://docs.spring.io/spring-security/reference/7.0/servlet/test/mockmvc/authentication.html
- Testcontainers JUnit 5: https://java.testcontainers.org/test_framework_integration/junit_5/
