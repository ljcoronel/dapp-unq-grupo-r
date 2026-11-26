# Quickstart: Validación de la Feature Mercado de Tokens

**Feature**: 003-mercado-tokens | **Phase**: 1 — Design | **Date**: 2026-11-16

Este documento describe los escenarios de validación end-to-end para verificar que la feature funciona correctamente. No incluye código de implementación; solo los pasos y resultados esperados.

---

## Prerrequisitos

1. **Backend corriendo** en `http://localhost:8080` con datos de jugadores y usuarios de prueba.
2. **Frontend corriendo** con `npm run dev` desde `frontend/` (el repositorio versiona `package-lock.json`).
3. **Dos usuarios de prueba** creados: `usuarioA` y `usuarioB` (con contraseña válida).
4. `recharts` instalado (`npm install` desde `frontend/`).

## Notas de contrato

- El JWT conserva el nombre de usuario en `sub` y agrega el identificador numérico en `userId`, que consume `AuthContext`.
- El backend serializa operaciones como `COMPRA` y `VENTA`; el frontend también acepta las etiquetas de negocio `BUY` y `SELL` para conservar compatibilidad.
- El saldo de créditos no está expuesto por la API. El backend valida los fondos al crear la orden y el frontend presenta el error recibido si no hay fondos suficientes.

---

## Escenario 1: Navegación con Sidebar (US-1)

**Setup**: Iniciar sesión como `usuarioA`.

**Pasos**:
1. Verificar que el sidebar es visible a la izquierda con las secciones: Catálogo, Comerciar, Portfolio, Estrategia.
2. Hacer click en "Catálogo" → el contenido principal muestra la tabla de jugadores.
3. Hacer click en "Comerciar" → el contenido muestra órdenes y botones.
4. Hacer click en "Portfolio" → el contenido muestra tenencias e historial.
5. Hacer click en "Estrategia" → el contenido muestra la estrategia actual o mensaje de configuración.
6. En cada paso, verificar que el sidebar permanece visible y que el ítem activo está resaltado.

**Resultado esperado**: El sidebar nunca desaparece; cada click cambia solo el contenido principal.

---

## Escenario 2: Catálogo Unificado con Filtros (US-2)

**Setup**: Estar en la sección Catálogo.

**Pasos**:
1. Verificar que la tabla tiene exactamente las columnas: Nombre, Equipo, Liga, Apariciones, Minutos Jugados, Goles, Asistencias, Tarjetas Amarillas, % Pases Exitosos.
2. Verificar que los jugadores de todas las ligas aparecen en una única tabla (sin agrupación).
3. Filtrar por una liga específica → solo se muestran jugadores de esa liga.
4. Filtrar por un equipo → solo se muestran jugadores de ese equipo.
5. Limpiar filtros → vuelven a mostrarse todos los jugadores.
6. Click en el encabezado "Goles" → la tabla se ordena ascendente por goles.
7. Click nuevamente en "Goles" → la tabla se ordena descendente por goles.
8. Aplicar filtro de liga + ordenamiento por asistencias → el ordenamiento aplica sobre los filtrados.

**Resultado esperado**: Filtros y ordenamiento funcionan independientemente y en combinación.

---

## Escenario 3: Compra de Tokens (US-3)

**Setup**: `usuarioA` con Estrategia de valuación configurada.

**Pasos**:
1. Ir a "Comerciar" → Click "Comprar" → se abre el modal de compra.
2. Seleccionar un jugador → el precio unitario se carga automáticamente.
3. Ingresar cantidad = 5, tolerancia = 10.
4. Verificar que se muestra: Precio total sin tolerancia = 5 × precio_unitario; Precio total con tolerancia = total + 10.
5. Ingresar cantidad = 101 → el botón "Confirmar" está deshabilitado.
6. Volver a cantidad = 5 → confirmar la orden.
7. Verificar que la orden aparece en la lista de órdenes activas de compra.

**Sin Estrategia configurada**: Click "Comprar" → aparece mensaje "Debe configurar una Estrategia antes de operar" (botón deshabilitado o mensaje de alerta).

---

## Escenario 4: Venta de Tokens (US-4)

**Setup**: `usuarioA` tiene al menos 5 tokens del Jugador X y Estrategia configurada.

**Pasos**:
1. Ir a "Comerciar" → Click "Vender" → se abre el modal de venta.
2. Seleccionar Jugador X → el modal muestra los tokens disponibles.
3. Ingresar cantidad = 3, tolerancia = 5.
4. Verificar precio total sin tolerancia = 3 × precio_unitario; con tolerancia = total − (3 × 5).
5. Intentar ingresar cantidad = 10 (más que los disponibles) → submit deshabilitado.
6. Confirmar con cantidad = 3 → la orden de venta queda activa.
7. Verificar que el modal de venta ahora muestra solo 2 tokens disponibles para Jugador X.

---

## Escenario 5: Portfolio y Gráfico (US-6, US-10)

**Setup**: `usuarioA` tiene tenencias de al menos 1 jugador.

**Pasos**:
1. Ir a "Portfolio" → verificar que se muestra la tabla de tenencias con: jugador, cantidad de tokens, precio promedio, valor actual.
2. Verificar que se muestra el historial de transacciones (compras en verde, ventas en rojo).
3. Verificar que el gráfico de torta muestra los jugadores con su valor actual.
4. Si hay > 10 jugadores, verificar que solo se muestran 10 segmentos individuales + "Otros".
5. Si no hay tenencias, verificar mensaje de portfolio vacío.

---

## Escenario 6: Configuración de Estrategia (US-7)

**Setup**: `usuarioA` sin estrategia configurada.

**Pasos**:
1. Ir a "Estrategia" → ver mensaje "Debe crear una Estrategia antes de poder operar".
2. Click "Crear Estrategia" → se abre modal.
3. Seleccionar Estrategia A → aparecen inputs: goals, assists, passSuccessPercentage, aerialsWon, timesManOfTheMatch.
4. Ingresar pesos que sumen 0.9 → botón Confirmar deshabilitado con mensaje de error.
5. Ingresar pesos que sumen 1 → Confirmar habilitado → confirmar.
6. Verificar que la vista de Estrategia muestra la estrategia configurada con sus pesos.
7. Click "Cambiar Estrategia" → seleccionar Estrategia B → inputs: passSuccessPercentage, aerialsWon, minutesPlayed, appearances.
8. Ingresar pesos que sumen 0.95 → confirmar → estrategia actualizada.

---

## Escenario 7: Perfil de Usuario (US-8)

**Setup**: `usuarioA` autenticado.

**Pasos**:
1. Click en el menú desplegable de perfil (esquina superior derecha).
2. Click "Ir a mi perfil" → la navegación lleva a `/app/perfil`.
3. Verificar que se muestra el nombre de usuario de `usuarioA`.

---

## Escenario 8: Cierre de Sesión

**Pasos**:
1. Click "Cerrar sesión" en el menú desplegable.
2. Verificar que `localStorage.getItem('authToken')` devuelve `null`.
3. Verificar que el usuario es redirigido a `/login`.
4. Verificar que las rutas protegidas (`/app/*`) redirigen a `/login` sin el token.

---

## Verificación de Validaciones (Edge Cases)

| Validación | Input | Resultado esperado |
|------------|-------|-------------------|
| Tokens compra > 100 | quantity=101 | Submit deshabilitado |
| Tokens compra = 0 | quantity=0 | Submit deshabilitado |
| Tokens venta > disponibles | quantity > disponibles | Submit deshabilitado |
| Tolerancia negativa | tolerance=-1 | Submit deshabilitado |
| Pesos Estrategia A ≠ 1 | sum=0.9 | Confirmar deshabilitado |
| Pesos Estrategia B ≠ 0.95 | sum=0.5 | Confirmar deshabilitado |
| Sin estrategia → Comprar | click Comprar | Mensaje bloqueante |
| Sin estrategia → Vender | click Vender | Mensaje bloqueante |
