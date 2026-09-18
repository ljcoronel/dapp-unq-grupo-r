# Feature Specification: Autenticación de usuario

**Feature Branch**: `[001-user-auth]`

**Created**: 2026-09-18

**Status**: Draft

**Input**: User description: "Crea las interfaces de usuario necesarias para que el usuario pueda crear una cuenta e iniciar sesión. Interfaz de creación de cuenta: 1) El usuario debe poder completar un formulario suministrando un nombre de usuario y una contraseña. 1.1) El nombre de usuario debe tener entre 4 y 16 caracteres de longitud y no debe contener ningún espacio. Se admiten caracteres alfanuméricos, acentuaciones y la letra ñ. 1.2) La contraseña debe tener entre 4 y 16 caracteres de longitud. Se admiten espacios, caracteres alfanuméricos, acentuaciones, la letra ñ. No se impone una restricción al tipo de caracteres que el usuario quiera ingresar. 2) Debe haber un botón que permita al usuario dirigirse a la pantalla para iniciar sesión. Esto en caso de que el usuario ya tenga una cuenta creada previamente. Interfaz de inicio de sesión: Esta interfaz es idéntica a la anterior y sigue sus mismas reglas, con la diferencia de que deberá haber un botón que permita al usuario dirigirse a la pantalla para crearse una cuenta, en caso de que el usuario todavía no haya creado su cuenta."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Crear una cuenta nueva (Priority: P1)

Un usuario sin cuenta desea registrarse completando un formulario con un nombre de usuario y una contraseña. La interfaz debe facilitar el ingreso de datos, validar los requisitos en todo momento, ofrecer una ruta clara para continuar si ya tiene una cuenta registrada y redirigirlo a la pantalla de inicio de sesión cuando la creación sea exitosa.

**Why this priority**: Este flujo es el punto de entrada principal para la creación de identidad dentro de la aplicación y permite que el usuario comience a usar el sistema de forma segura y con claridad.

**Independent Test**: Puede validarse completando el formulario con datos válidos, confirmando que las restricciones se muestran durante todo el proceso y verificando la redirección al inicio de sesión tras el alta exitosa.

**Acceptance Scenarios**:

1. **Given** el usuario está en la pantalla de creación de cuenta, **When** ingresa un nombre de usuario de entre 4 y 16 caracteres sin espacios y una contraseña de entre 4 y 16 caracteres, **Then** la información debe aceptarse como válida, mostrar las restricciones en todo momento y permitir completar la creación de la cuenta.
2. **Given** el usuario intenta completar el formulario con un nombre de usuario fuera de la longitud permitida o con espacios, **When** intenta continuar, **Then** la interfaz debe indicar que los datos ingresados no cumplen las reglas y no debe aceptarse el envío.
3. **Given** el usuario ya tiene una cuenta, **When** presiona el botón de acceso a iniciar sesión, **Then** debe dirigirse a la pantalla correspondiente.
4. **Given** el usuario completa la creación de cuenta con datos válidos, **When** confirma la operación, **Then** debe ser redirigido a la pantalla de inicio de sesión.

---

### User Story 2 - Iniciar sesión con una cuenta existente (Priority: P1)

Un usuario con una cuenta creada desea acceder a la aplicación con sus credenciales. La interfaz debe ser igual en estructura y validación a la de creación, mostrar las restricciones durante todo el proceso, incluir el botón de acción específico y ofrecer una opción para crear la cuenta si aún no existe.

**Why this priority**: El acceso es el flujo de acceso principal para usuarios recurrentes y debe ser equivalente en claridad y validación a la creación de cuenta para evitar errores y confusión.

**Independent Test**: Puede validarse completando los campos con un nombre de usuario y contraseña que cumplan las condiciones, verificando que el botón de iniciar sesión está presente y que el usuario puede dirigirse a la pantalla de registro si aún no tiene cuenta.

**Acceptance Scenarios**:

1. **Given** el usuario está en la pantalla de inicio de sesión, **When** ingresa un nombre de usuario y una contraseña que cumplen las reglas, **Then** la interfaz debe aceptar la información para continuar con el inicio de sesión.
2. **Given** el usuario intenta usar un nombre de usuario con espacios o longitud inválida, **When** intenta enviar el formulario, **Then** la interfaz debe mostrar un mensaje de validación y bloquear el envío.
3. **Given** el usuario todavía no tiene una cuenta, **When** presiona el botón para dirigirse a la pantalla de creación de cuenta, **Then** debe visualizar la pantalla correspondiente.
4. **Given** el usuario está en la pantalla de inicio de sesión, **When** observa el formulario, **Then** debe ver claramente el botón con la leyenda "iniciar sesión" y las restricciones de validación visibles durante todo el proceso.
5. **Given** el usuario intenta iniciar sesión con un nombre de usuario o contraseña inexistentes, **When** presiona el botón de iniciar sesión, **Then** debe mostrarse la leyenda "Nombre de usuario o contraseña incorrectos".

---

### User Story 3 - Navegación entre pantallas de acceso (Priority: P2)

Un usuario puede alternar entre la pantalla de creación de cuenta y la pantalla de inicio de sesión según si ya tiene una cuenta o necesita registrarse. La navegación debe mantenerse clara y consistente con los textos de acción de cada formulario.

**Why this priority**: La navegación entre pantallas reduce la fricción del flujo y mejora la entendibilidad del proceso de acceso.

**Independent Test**: Puede probarse desde cualquiera de las dos pantallas y verificando que el usuario puede cambiar de vista sin perder el contexto de la tarea, con los botones de acción claramente identificados.

**Acceptance Scenarios**:

1. **Given** el usuario se encuentra en la pantalla de creación de cuenta, **When** acciona el botón de iniciar sesión, **Then** debe mostrar la pantalla de acceso.
2. **Given** el usuario se encuentra en la pantalla de inicio de sesión, **When** acciona el botón de crear cuenta, **Then** debe mostrar la pantalla de registro.
3. **Given** el usuario está en la pantalla de creación de cuenta, **When** observa el formulario, **Then** debe ver claramente el botón con la leyenda "Crear cuenta".
4. **Given** el usuario está en la pantalla de inicio de sesión, **When** observa el formulario, **Then** debe ver claramente el botón con la leyenda "iniciar sesión".

### Edge Cases

- ¿Qué ocurre si el nombre de usuario tiene 3 caracteres o 17 caracteres? En el momento en el que el usuario presione el botón que le corresponda, se le resaltará en rojo todas las restricciones que su input debe cumplir.
- ¿Qué ocurre si el nombre de usuario incluye espacios al principio, en medio o al final? En el momento en el que el usuario presione el botón que le corresponda, se le resaltará en rojo todas las restricciones que su input debe cumplir.
- ¿Qué ocurre si la contraseña contiene espacios, acentos o caracteres no alfanuméricos? En el momento en el que el usuario presione el botón que le corresponda, se le resaltará en rojo todas las restricciones que su input debe cumplir.
- ¿Qué ocurre si el usuario intenta enviar el formulario vacío o incompleto? En el momento en el que el usuario presione el botón que le corresponda, se le resaltará en rojo todas las restricciones que su input debe cumplir.
- ¿Qué ocurre si el usuario modifica la vista desde una pantalla a otra sin haber finalizado la operación actual? No hacemos nada.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST mostrar una interfaz para crear una cuenta donde el usuario pueda completar un nombre de usuario y una contraseña.
- **FR-002**: La interfaz de creación de cuenta MUST incluir un botón con la leyenda "Crear cuenta" para confirmar la operación de registro.
- **FR-003**: La interfaz de inicio de sesión MUST incluir un botón con la leyenda "iniciar sesión" para confirmar el acceso al sistema.
- **FR-004**: El nombre de usuario MUST tener una longitud entre 4 y 16 caracteres inclusive.
- **FR-005**: El nombre de usuario MUST rechazar cualquier valor que contenga espacios.
- **FR-006**: El nombre de usuario MUST aceptar caracteres alfanuméricos, acentos y la letra ñ.
- **FR-007**: La contraseña MUST tener una longitud entre 4 y 16 caracteres inclusive.
- **FR-008**: La contraseña MUST aceptar espacios, caracteres alfanuméricos, acentos y la letra ñ, sin restringir el tipo de caracteres ingresados por el usuario.
- **FR-009**: La interfaz de creación de cuenta MUST incluir un botón que permita al usuario dirigirse a la pantalla de inicio de sesión cuando ya tiene una cuenta creada.
- **FR-010**: El sistema MUST ofrecer una interfaz de inicio de sesión idéntica en reglas y estructura a la de creación de cuenta.
- **FR-011**: La interfaz de inicio de sesión MUST incluir un botón que permita al usuario dirigirse a la pantalla de creación de cuenta cuando aún no tiene una cuenta.
- **FR-012**: El sistema MUST validar el contenido de los campos antes de aceptar la información ingresada en cualquiera de las dos pantallas.
- **FR-013**: Las restricciones de validación MUST estar presentes en todo momento durante el proceso de creación de cuenta o inicio de sesión.
- **FR-014**: El sistema MUST presentar una indicación clara cuando el usuario ingresa un valor que no cumple con la longitud o formato requerido.
- **FR-015**: Cuando un usuario crea su cuenta exitosamente, el sistema MUST redirigirlo a la pantalla de inicio de sesión.
- **FR-016**: Cuando el usuario intenta iniciar sesión con un nombre de usuario o contraseña inexistentes, el sistema MUST mostrar la leyenda "Nombre de usuario o contraseña incorrectos".
- **FR-017**: El sistema MUST permitir que el usuario acceda a la otra pantalla de autenticación sin perder el flujo principal de la tarea que desea realizar.

### Key Entities *(include if feature involves data)*

- **Cuenta de usuario**: Representa la identidad del usuario dentro de la aplicación, compuesta por un nombre de usuario y una contraseña.
- **Credenciales de acceso**: Conjunto de datos que el usuario ingresa para crear una cuenta o iniciar sesión, con validaciones de longitud y caracteres definidos por la regla del sistema.
- **Pantalla de acceso**: Vista funcional que presenta el formulario de autenticación y los controles de navegación entre registro e inicio de sesión.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El 95% de los usuarios que prueban la interfaz pueden completar la creación de cuenta o el inicio de sesión en menos de 2 minutos sin errores de validación evitables.
- **SC-002**: Al menos el 90% de los usuarios logran distinguir correctamente la pantalla de registro y la de inicio de sesión sin ayuda adicional.
- **SC-003**: El 100% de las entradas con nombre de usuario o contraseña fuera de rango reciben una retroalimentación clara y consistente dentro del flujo de autenticación.
- **SC-004**: La navegación entre pantallas permite completar el flujo principal de acceso con un número mínimo de errores por confusión entre las dos vistas.

## Assumptions

- Los usuarios cuentan con la capacidad de ingresar texto en un formulario sin restricciones de teclado ni dispositivos particulares.
- Las reglas de validación aplican a ambas pantallas de manera consistente para evitar discrepancias en la experiencia del usuario.
- La autenticación es un flujo de interfaz y validación de entrada, sin asumir un mecanismo de almacenamiento o backend específico.
- La navegación entre pantallas es parte del MVP y no requiere un proceso de recuperación de contraseña ni cambios de perfil.
