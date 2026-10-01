<!--
Sync Impact Report
Version change: 1.0.0 -> 1.1.0
Modified principles:
- I. Monorepo -> I. Monorepo (translated into English)
- II. Arquitectura en Capas -> II. Layered Architecture (translated; Service return and Controller mapping responsibilities made explicit)
- III. Modelo Rico -> III. Rich Model (translated; framework-free POJO requirement made explicit)
- IV. Validacion por Niveles -> IV. Layered Validation (translated into English)
- V. Testing y Calidad -> V. Testing and Quality (translated into English)
Added sections: None
Removed sections: None
Follow-up TODOs: None
-->

# Football Player Market Valuation Constitution

## Core Principles

### I. Monorepo
The project MUST remain a monorepo in which the backend and frontend coexist in the
same repository while each is organized independently with its own files,
configurations, and responsibilities. This separation allows each part to evolve
without coupling the other's infrastructure and keeps the project coherent as a whole.

### II. Layered Architecture
The application MUST follow a layered architecture with clear separation among
Controller, Service, Model, and Persistence. The Controller communicates request
inputs and response outputs through the Service. The Service coordinates business
operations between the Model and Persistence, and MUST return Model objects to the
Controller; it MUST NOT return Controller DTOs. The Controller, or its dedicated
mappers, is solely responsible for transforming returned Model objects into output
DTOs. Before persistence, the Service MUST map Model objects to separate persistence
entities that carry persistence annotations. The Model encapsulates business rules and
MUST NOT know about other layers. Persistence may access the Model but MUST NOT invoke
its business logic. Layer boundaries are mandatory; responsibilities MUST NOT be mixed
between the Service and Controller.

### III. Rich Model
Business logic MUST reside in Model objects, and each method MUST have a single
responsibility. Domain objects MUST validate their own invariants and throw
domain-specific exceptions when a rule is violated. The Model is the source of truth
for business behavior and MUST be composed of plain Java objects (POJOs), without
framework dependencies or annotations. In particular, Model objects MUST NOT use
persistence annotations such as `@Entity`, `@Table`, or `@Column`; persistence
annotations belong only to separate persistence entities. This keeps domain rules
independent of frameworks and persistence concerns.

### IV. Layered Validation
Request DTOs MUST validate input format, types, and sanitization, including trimming
and cleaning input values. The Service MUST validate existence, availability, and
whether an action can be performed; it resolves IDs, confirms entities, and checks
that an operation is viable. The Model MUST validate domain invariants and business
rules, using domain exceptions to represent invalid business states.

### V. Testing and Quality
The project MUST cover happy paths and edge cases with domain unit tests, Service and
Repository integration tests against a real PostgreSQL instance started with
Testcontainers, and end-to-end tests using MockMvc located exclusively in their own
package. Existing tests MUST NOT be modified or removed without prior explicit
permission. Test coverage MUST verify expected behavior and robustness against invalid
inputs, boundary conditions, and business errors.

## Technology Stack and Deliverables

- Backend: Java 25 and Spring Boot 4.
- Database: PostgreSQL.
- Testing: JUnit 5 and Testcontainers.
- Frontend: React.js with Vite.
- Quality deliverables: successful local compilation, successful application startup,
  passing unit and integration tests, and an updated Postman collection for every
  endpoint added.
- Application language: project documentation, error messages, and descriptions MUST
  be written in Spanish. Domain names MUST be in Spanish, without accents or the letter
  "ñ" in code identifiers; technical and architectural terms remain in English.

## Workflow and Quality

- A requirement is complete only when it meets the project's Definition of Done:
  unit and integration tests pass, compilation and local startup succeed, and the
  Postman collection is updated.
- End-to-end tests MUST use MockMvc and MUST NOT be mixed with Service or domain tests.
- Architecture and design MUST remain consistent with layer separation and the
  single-responsibility rule for each module.
- Implementation decisions MUST prioritize clarity, isolation, and traceability of
  business behavior over ad hoc solutions.

## Governance

This constitution governs all project development, review, and delivery decisions.
Compliance is mandatory for every repository change, and this constitution takes
precedence over any conflicting local practice or convention.

An amendment MUST be documented with its rationale, a version change, and a review of
its impact on rules, tests, and deliverables. Amendments MUST be reviewed and approved
through the project's normal review process before adoption. Versioning follows
Semantic Versioning: backward-incompatible governance or principle changes require a
major version increment; new or materially expanded sections or principles require a
minor increment; corrections, clarifications, and non-semantic refinements require a
patch increment.

Compliance MUST be reviewed before every merge or delivery. Reviewers MUST verify that
the code follows the layered architecture, rich-model, layered-validation, and testing
requirements in this constitution. Deviations MUST be addressed before the change is
approved for delivery.

**Version**: 1.1.0 | **Ratified**: 2026-09-05 | **Last Amended**: 2026-10-01
