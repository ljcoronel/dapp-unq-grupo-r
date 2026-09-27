# Quickstart: Catálogo de jugadores por liga

## Prerrequisitos

- Node.js y npm instalados.
- API disponible en `http://localhost:8080` y respondiendo a `GET /players`.
- Dependencias del frontend instaladas (`npm install` si aún no existe `node_modules`).

## Ejecución

Desde el directorio `frontend`:

```powershell
npm run dev
```

Iniciar sesión con una cuenta existente y abrir `/home`. Home consulta el endpoint una vez al montarse. En las herramientas de red del navegador, verificar que la solicitud es `GET http://localhost:8080/players` y que no contiene encabezado `Authorization`.

## Escenarios de validación

1. Con cinco grupos de diez jugadores, abrir Home y comprobar que aparecen las cinco tablas expandidas, con diez filas y las siete columnas en español.
2. Contraer y volver a expandir cada encabezado de liga; comprobar que solo cambia la tabla seleccionada.
3. Probar un jugador con `asistencias: null` o `penaltis: null`; comprobar que la celda correspondiente muestra `-` y que otros valores, incluidos `0`, no cambian.
4. Simular una respuesta de error o desconectar la API; comprobar que se muestra un mensaje de error en español, no una tabla vacía que parezca exitosa.
5. Probar una respuesta vacía válida y comprobar que el estado vacío se distingue del estado de error.

## Validación disponible

```powershell
npm run lint
npm run build
```

El proyecto no define actualmente un script de pruebas automatizadas. Para el contrato de respuesta, consultar [contracts/players-api.yaml](contracts/players-api.yaml); para los campos y valores visibles, consultar [data-model.md](data-model.md).
