# Research: Protección del scraping con API key

## Decisiones

### Transporte

La credencial viajará exclusivamente en `X-API-Key`. No se usará query string,
body ni `Authorization: Bearer`, porque las URLs pueden quedar registradas y
`Authorization` ya pertenece al flujo JWT existente. El filtro rechazará valores
ausentes, vacíos, con espacios externos o con formato inválido antes de continuar
la cadena.

### Persistencia y secreto

Se agregará una entidad `ApiKeyEntity` con una fila única para el permiso
`scraping`, hash BCrypt, estado activo y timestamps. La clave plana no se
persistirá ni se podrá recuperar desde la base; solo existirá en memoria durante
la generación y se imprimirá una vez después del commit. Una restricción única
sobre `permission` evita credenciales duplicadas.

Se elige BCrypt porque ya existe `PasswordEncoder` en la aplicación. Cifrar el
valor para recuperarlo no aporta valor a este requisito y aumenta el impacto de
una filtración.

### Autorización

Un `ApiKeyAuthenticationFilter` leerá `X-API-Key`, consultará la credencial
activa de permiso `scraping` y comparará el valor con `PasswordEncoder.matches`.
Ante éxito creará una autenticación con la autoridad `scraping`. La regla de
Spring Security cambiará `POST /scrape` de `permitAll` a
`hasAuthority("scraping")`. La ausencia o invalidez responderá `401` con un
mensaje genérico en español y detendrá la cadena antes del controlador.

### Rotación y comando

La rotación se implementará en un servicio transaccional. Primero se genera la
nueva clave y su hash; luego se bloquea la fila singleton y se actualiza o crea
la credencial. Si falla el hash, guardado o commit, la transacción revierte y la
credencial anterior continúa vigente. El valor se imprime fuera de la
transacción solo cuando el servicio retorna correctamente.

El comando se activará mediante `--app.command=rotate-scraping-api-key`, con
`web-application-type=none`. Un wrapper PowerShell invocará el JAR o Gradle,
imprimirá el valor en `stdout`, enviará diagnósticos a `stderr` y devolverá
código distinto de cero ante errores. No se agregará un endpoint público de
rotación.

### Alternativas consideradas

- `Authorization: Bearer`: descartado por conflicto con JWT.
- Query parameter: descartado por exposición en logs, historial y proxies.
- Hash SHA-256 directo: BCrypt reutiliza el mecanismo de contraseñas existente.
- Tabla histórica de versiones: innecesaria para una única credencial y más
  compleja de hacer atómica con el esquema actual.
- Endpoint HTTP administrativo: aumenta la superficie de ataque y requiere
  definir roles administrativos fuera del alcance.

### Riesgos operativos

La credencial compartida no permite atribuir solicitudes a usuarios individuales
y cada rotación invalida inmediatamente el valor anterior. Debe redactarse
`X-API-Key` en proxies y observabilidad. Si se pierde la clave, debe ejecutarse
nuevamente el comando de rotación.
