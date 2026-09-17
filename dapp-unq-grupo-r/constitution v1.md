Vamos a arrancar un proyecto nuevo: **Valoración de Mercado de Jugadores de Fútbol**, una aplicación web.

## Stack Tecnológico
* **Backend:** Java 25, Spring Boot 4 (última versión)
* **Base de Datos:** PostgreSQL
* **Testing:** JUnit 5 + Testcontainers
* **Frontend:** React.js, Vite

---

## Principios del Proyecto

### 1. Monorepo
Dentro del proyecto convivirán el backend y el frontend, estructurados de forma independiente pero en el mismo repositorio.

### 2. Arquitectura en Capas
La arquitectura del proyecto utilizará una división en capas tradicional compuesta por: `controller`, `service`, `model` y `persistence`.

* **Controller:** Solo se comunica con el `service`.
* **Service:** Orquesta la lógica entre el `model` y la `persistence`.
* **Model:** No conoce ni interactúa con ninguna otra capa.
* **Persistence:** Conoce al modelo, pero no realiza llamadas sobre él.
* **Mapeo de Entidades:** Se deben generar entidades particulares que contengan las anotaciones de persistencia. El `service`, antes de pasar la información al repositorio de persistencia, debe realizar el mapeo del objeto de modelo al objeto de persistencia.
* **Aislamiento:** No mezclar capas entre `service` y el `controller`.


### 3. Modelo Rico
* La lógica de negocio reside dentro de los objetos del `model`.
* Cada método debe cumplir con una única responsabilidad.

### 4. Validación por Niveles
* **Forma y Tipos:** Las validaciones de formato y tipos del `request` se realizan en el DTO de request. El *trimming* y la sanitización de input también corresponden a esta capa.
* **Existencia y Viabilidad:** Validar que lo pedido exista y que la acción sea lógicamente posible (los IDs se resuelven, las entidades se encuentran) es responsabilidad del `service`.
* **Invariantes del Dominio:** Las invariantes del dominio se validan en los objetos del modelo, lanzando excepciones propias del dominio en caso de incumplimiento.

### 5. Estrategia de Testing
* **Tests Unitarios del Dominio:** Se ejecutan sin Spring y sin base de datos.
* **Tests de Integración (Services y Repositorios):** Se ejecutan contra una instancia real de PostgreSQL levantada mediante Testcontainers.
* **Tests End-to-End (E2E):** Se realizan con MockMvc. Deben ubicarse exclusivamente en su propio paquete y nunca dentro de un test de `service`.
* **Cobertura:** Siempre se deben cubrir casos felices y casos borde (*edge cases*).
* **Restricción de Modificación:** No se modifica ni se borra ningún test existente, bajo ninguna circunstancia o fase del flujo, sin solicitar permiso previo y recibir un "sí" explícito.

### 6. Definición de Terminado (DoD) y Entregables
Un requerimiento se considera terminado únicamente cuando cumple con lo siguiente:
1. Posee tests unitarios y de integración (tanto para casos felices como de borde) y todos pasan exitosamente.
2. La aplicación compila y levanta correctamente con la configuración local.
3. La colección de Postman del proyecto se encuentra actualizada con los nuevos endpoints incorporados.

### 7. Idioma y Convenciones
* **Documentación y Mensajes de error:** Los documentos y los mensajes de error deben estar redactados en español.
* **Dominio:** Los nombres del dominio deben estar en español, pero sin acentos ni la letra "ñ" en los identificadores de código.
* **Términos Técnicos:** Los términos técnicos, normativos y de arquitectura se mantienen en inglés.