# Quickstart: Validación del frontend de autenticación

## Prerrequisitos

- Node.js y npm instalados.
- Backend levantado en `http://localhost:8080`.
- Un usuario existente para probar login, o posibilidad de registrarlo en el backend.

## Instalación y ejecución

Desde `frontend/`:

```powershell
npm install
npm run dev
```

Abrir la URL que informa Vite.

## Validación funcional

1. Visitar `/register`; confirmar que se muestran las reglas de nombre y contraseña de forma permanente.
2. Intentar enviar campos vacíos, un nombre con espacios, o valores de longitud menor a 4 o mayor a 16; verificar que se bloquea el request y se muestran errores claros.
3. Registrar credenciales válidas; verificar que el request sea `POST http://localhost:8080/register` y que la aplicación navegue a `/login`.
4. Desde `/login`, usar el enlace de registro y volver sin recarga completa.
5. Iniciar sesión con credenciales válidas; verificar `POST http://localhost:8080/login`, header `Authorization: Bearer ...`, existencia de `localStorage.authToken` y navegación a Home.
6. Recargar Home; confirmar que la sesión persiste y la ruta continúa protegida.
7. Abrir el menú del perfil; verificar "Ir a mi Perfil" y "Cerrar sesión".
8. Cerrar sesión; confirmar eliminación de `localStorage.authToken`, redirección a `/login` y bloqueo de acceso directo a Home.
9. Probar credenciales inválidas, usuario duplicado y backend detenido; verificar mensajes visibles en español y finalización del estado de carga.

## Validación técnica

```powershell
npm run lint
npm run build
```

Los comandos deben finalizar correctamente. El contrato detallado de requests y responses está en [`contracts/auth-api.yaml`](contracts/auth-api.yaml).
