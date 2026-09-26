# Validacion rapida: CI del backend

## Prerrequisitos

- JDK 25 para la ejecucion local.
- Git y Docker disponibles si se van a ejecutar localmente las pruebas que usan Testcontainers.
- Acceso al repositorio y GitHub Actions para validar los triggers y el reporte publicado.

## Validacion local

Desde la raiz del repositorio, compilar produccion y codigo de pruebas:

```bash
cd backend
./gradlew clean compileTestJava
```

Resultado esperado: compilacion exitosa de las tareas aplicables al proyecto y `app`; si falla, la suite no debe ejecutarse en CI.

Ejecutar todas las pruebas configuradas:

```bash
./gradlew test
```

Resultado esperado: ejecucion de las pruebas unitarias, de integracion y E2E de `app`. Las pruebas Testcontainers inician su propio PostgreSQL por Docker. Los resultados XML se escriben bajo `app/build/test-results/test/`.

En Windows, ejecutar los equivalentes mediante `.\gradlew.bat clean compileTestJava` y `.\gradlew.bat test` desde `backend`.

## Validacion en GitHub Actions

1. Enviar un cambio a `main` que modifique `backend/` o `.github/workflows/ci-backend.yml`, o abrir un pull request cuyo destino sea `main` con dichos cambios.
2. Confirmar que aparece el workflow `CI Backend`, que el job se llama `build-and-test` y que sus pasos siguen el orden de checkout, Java, compilacion, pruebas e informes.
3. Confirmar que un cambio solo en `frontend/` no inicia el workflow y que modificar solo el workflow si lo inicia.
4. En una ejecucion exitosa, revisar el reporte JUnit publicado y los XML en `backend/app/build/test-results/test/`.
5. Introducir temporalmente un fallo de compilacion y luego uno de prueba en cambios de validacion separados. El primer caso debe detenerse antes de ejecutar `test`; el segundo debe dejar la ejecucion fallida. En ambos casos se debe intentar la publicacion de resultados y nunca convertir la ejecucion fallida en exitosa.
6. Confirmar que las pruebas de integracion levantan PostgreSQL mediante Testcontainers sin un servicio `postgres` declarado en el workflow.

Si se valida un pull request desde un fork, el token de GitHub no puede crear el Check Run de `dorny/test-reporter`; consultar el artefacto de resultados XML que se carga incluso en ese contexto. No elevar permisos ni usar `pull_request_target` para ejecutar el codigo del pull request.

Para las entidades y sus reglas, consultar [data-model.md](data-model.md).
