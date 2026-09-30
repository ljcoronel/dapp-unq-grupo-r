<!--
Sync Impact Report
Version change: 0.0.0 -> 1.0.0
Modified principles: None -> I. Monorepo, II. Arquitectura en Capas, III. Modelo Rico, IV. Validacion por Niveles, V. Testing y Calidad
Added sections: Stack Tecnologico y Entregables; Flujo de Trabajo y Calidad
Removed sections: None
Deferred TODOs: La definicion detallada del backlog inicial y la scaffolding del proyecto requiere una especificacion tecnica posterior.
-->

# Valoracion de Mercado de Jugadores de Futbol Constitution

## Core Principles

### I. Monorepo
El proyecto debe mantenerse como un monorepo donde el backend y el frontend convivan en el mismo repositorio, pero cada uno se organiza de forma independiente con sus propios archivos, configuraciones y responsabilidades. Esta separacion permite evolucionar cada parte sin acoplar la infraestructura del otro y facilita la gestion del proyecto como un todo coherente.

### II. Arquitectura en Capas
La aplicacion debe respetar una arquitectura en capas con division clara entre controller, service, model y persistence. El controller solo comunica entradas y salidas con el service; el service orquesta la logica entre model y persistence; el model encapsula la regla de negocio y no conoce otras capas; la persistence conoce al modelo, pero no le invoca logica de negocio. Antes de persistir, el service debe mapear el objeto de modelo al objeto de persistencia mediante entidades particulares con anotaciones. El aislamiento entre capas es obligatorio y no se permite mezclar responsabilidades entre service y controller.

### III. Modelo Rico
La logica de negocio debe residir en los objetos del model y cada metodo debe tener una unica responsabilidad. Los objetos del dominio deben validar sus invariantes internamente y lanzar excepciones propias del dominio cuando se incumple una regla. El model debe ser la fuente de verdad del comportamiento del negocio y no debe depender de frameworks ni de la capa de persistencia para definir la regla.

### IV. Validacion por Niveles
La validacion de formato, tipos y sanitizacion de la entrada debe ocurrir en los DTO de request, incluyendo trimming y limpieza de inputs. La verificacion de existencia, disponibilidad y viabilidad de la accion corresponde al service, que resuelve IDs, confirma entidades y valida que la operacion sea logica. Las invariantes del dominio y las reglas de negocio se validan dentro del model, con excepciones del dominio que reflejan el estado invalido del negocio.

### V. Testing y Calidad
El proyecto debe cubrir casos felices y de borde en tests unitarios del dominio, tests de integracion de services y repositories contra PostgreSQL real levantado con Testcontainers, y tests E2E con MockMvc ubicados exclusivamente en su propio paquete. No se modifica ni se elimina ningun test existente sin permiso previo y aprobacion explicita. La cobertura debe validar el comportamiento esperado y la robustez ante entradas invalidas, condiciones limite y errores de negocio.

## Stack Tecnologico y Entregables

- Backend: Java 25 y Spring Boot 4.
- Base de datos: PostgreSQL.
- Testing: JUnit 5 y Testcontainers.
- Frontend: React.js con Vite.
- Entregables de calidad: compilacion local exitosa, levantamiento correcto de la aplicacion, tests unitarios e integracion ejecutandose correctamente, y actualizacion de la collection de Postman para cada endpoint incorporado.
- Idioma: la documentacion, mensajes de error y descripciones deben estar redactados en espanol. Los nombres del dominio deben estar en espanol, pero sin acentos ni la letra "ñ" en los identificadores de codigo; los terminos tecnicos y arquitectonicos permanecen en ingles.

## Flujo de Trabajo y Calidad

- Cada requerimiento se considera terminado solo cuando cumple con el Definition of Done del proyecto: tests unitarios e integracion pasando, compilacion y levantamiento local exitosos, y collection de Postman actualizada.
- Los tests E2E deben ejecutarse con MockMvc y no pueden mezclarse con pruebas de servicio ni de dominio.
- La arquitectura y el diseño deben mantenerse consistentes con la separacion por capas y la responsabilidad unica de cada modulo.
- Las decisiones de implementacion deben priorizar la claridad, el aislamiento y la trazabilidad del comportamiento de negocio sobre soluciones ad hoc.

## Governance

Esta constitucion gobierna todas las decisiones de desarrollo, revision y entrega del proyecto. Su cumplimiento es obligatorio para cualquier cambio en el repositorio y prevalece sobre cualquier practica o convencion local que contradiga estas reglas.

Las enmiendas deben documentarse con un cambio de version, una justificacion explicita y una revision del impacto en reglas, pruebas y entregables. La version sigue la politica semver: cambios incompatibles en governance o principios implican un incremento de version mayor; agregados o ampliaciones materiales implican un incremento menor; correcciones, aclaraciones y ajustes no semanticos implican un incremento de parche.

La revision de cumplimiento debe verificarse antes de cada merge o entrega: se valida que el codigo respete la arquitectura en capas, el modelo rico, la validacion por niveles y la estrategia de testing definida en esta constitucion. Cualquier desviacion debe abordarse antes de aprobar la entrega.

**Version**: 1.0.0 | **Ratified**: 2026-09-05 | **Last Amended**: 2026-09-05
