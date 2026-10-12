# Contrato del comando de rotación

## Invocación

```text
java -jar app.jar --spring.main.web-application-type=none --app.command=rotate-scraping-api-key
```

El wrapper operativo `backend/scripts/rotate-scraping-api-key.ps1` puede
encapsular esta invocación para el entorno local o de despliegue.

## Resultado exitoso

- Se crea o reemplaza la única API key activa con permiso `scraping`.
- La operación se confirma transaccionalmente.
- `stdout` contiene el valor plano de la nueva API key una sola vez.
- El proceso finaliza con código `0`.

## Resultado fallido

- La transacción se revierte; si había una clave vigente, continúa utilizable.
- No se imprime una credencial.
- Se informa el error en español por `stderr`.
- El proceso finaliza con código distinto de `0`.

El comando no devuelve hashes, permisos internos ni valores de la credencial
anterior, y no expone un endpoint HTTP administrativo.
