# Quickstart: Validacion del frontend de autenticacion

## Prerrequisitos

- Node.js y npm instalados.
- Backend levantado en `http://localhost:8080`.
- Un usuario existente para probar login, o posibilidad de registrarlo en el backend.

## Instalacion y ejecucion

Desde `frontend/`:

```powershell
npm install
npm run dev
```

Abrir la URL que informa Vite.

## Validacion funcional

1. Visitar `/register`; confirmar que se muestran las reglas de nombre y contrasena de forma permanente.
2. Intentar enviar campos vacios, un nombre con espacios, o valores de longitud menor a 4 o mayor a 16; verificar que se bloquea el request y se muestran errores claros.
3. Registrar credenciales validas; verificar que el request sea `POST http://localhost:8080/register` y que la aplicacion navegue a `/login`.
4. Desde `/login`, usar el enlace de registro y volver sin recarga completa.
5. Iniciar sesion con credenciales validas; verificar `POST http://localhost:8080/login`, que la respuesta incluya un header `Authorization` con un token valido, que ese valor se guarde unicamente en `localStorage.authToken` y que la aplicacion navegue a `/home`.
6. Recargar `/home`; confirmar que la sesion se rehidrata desde `localStorage` y la ruta continua protegida.
7. Abrir el menu del perfil; verificar `Ir a mi Perfil` y `Cerrar sesion`, y confirmar que la primera opcion es intencionalmente inerte.
8. Cerrar sesion; confirmar eliminacion de `localStorage.authToken`, limpieza del estado en memoria, redireccion a `/login` y bloqueo de acceso directo a `/home`.
9. Probar credenciales invalidas, usuario duplicado y backend detenido; verificar, respectivamente, el mensaje `Nombre de usuario o contrasena incorrectos`, un error de registro accionable y el mensaje de conexion fallida, siempre con finalizacion del estado de carga.
10. Eliminar manualmente `localStorage.authToken` e intentar abrir `/home`; confirmar la redireccion automatica a `/login` sin mostrar contenido protegido.

## Validacion tecnica

```powershell
npm run lint
npm run build
```

Los comandos deben finalizar correctamente. El contrato detallado de requests y responses esta en [`contracts/auth-api.yaml`](contracts/auth-api.yaml).
