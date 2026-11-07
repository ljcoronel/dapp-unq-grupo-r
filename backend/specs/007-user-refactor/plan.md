# Implementation Plan: User Domain and Persistence Separation

**Branch**: `refactor/user` | **Date**: 2026-10-01 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification at `backend/specs/007-user-refactor/spec.md`

## Summary

Separate the framework-independent `User` domain model from JPA and Spring
Security by introducing a persistence-only `UserEntity`, a mapper, and a
domain-facing repository adapter. Keep the existing user repository operations
available to services while `UserRepositoryImpl` maps between `User` and
`UserEntity` and delegates storage to a Spring Data JPA repository. Adapt
authentication through a security principal that wraps `User`, and keep API
responses and database schema unchanged.

The selected repository-internal mapping conflicts with the current Constitution
Principle II, which assigns model-to-entity mapping to the Service. An approved
constitution amendment is therefore a prerequisite to implementation; this plan
does not silently waive or change that rule.

## Technical Context

**Language/Version**: Java 25  
**Primary Dependencies**: Spring Boot 4, Spring Data JPA, Spring Security, Gradle  
**Storage**: PostgreSQL  
**Testing**: JUnit 5, Spring Boot Test, Testcontainers PostgreSQL, MockMvc  
**Target Platform**: JVM backend service  
**Project Type**: Web service in the backend of a monorepo  
**Performance Goals**: Preserve current request behavior without adding database round trips  
**Constraints**: Preserve existing schema, authentication behavior, Spanish errors, and API contracts; do not expose `UserEntity` beyond persistence  
**Scale/Scope**: User domain, persistence adapter, security adaptation, related services/controllers, tests, and feature design docs

## Constitution Check

*Gate: reviewed before design and re-evaluated after the design.*

| Principle | Check |
|---|---|
| I. Monorepo | Pass. Changes remain within the backend feature and do not couple frontend infrastructure. |
| II. Layered Architecture | **Conditional / blocked for implementation.** The requested `UserRepositoryImpl` maps internally, conflicting with the current mandatory Service-mapping rule. Obtain and record an approved amendment before implementation. Service and controller responsibilities remain separated; services work with domain values and controllers create response DTOs. |
| III. Rich Model | Pass by design. `User` will be a POJO with domain invariants and no persistence or security annotations/contracts. |
| IV. Layered Validation | Pass by design. Request format stays at DTOs, orchestration/existence checks stay in services, and User invariants stay in the model. |
| V. Testing and Quality | Pass by design. Add unit and PostgreSQL Testcontainers coverage, retain MockMvc E2E tests in their own package, and preserve existing coverage. The request explicitly includes updating directly affected tests; no tests are to be removed. |
| Technology and language | Pass. Use the existing Java 25, Spring Boot 4, PostgreSQL, JUnit 5, and Gradle stack. Keep user-facing messages in Spanish. |

**Post-design gate**: The adapter structure meets the selected design only after
the Principle II amendment is approved through the constitution's normal review
process. The gate remains blocked until then. No schema/API change or additional
complexity is introduced.

## Design Decisions

1. Keep `UserRepository` as a domain-facing port with operations that accept and
   return `User`. Add `UserRepositoryImpl` as its implementation and a separate
   Spring Data repository for `UserEntity`. The persistence adapter owns the
   mapping and delegates database operations.
2. Put JPA annotations and lifecycle handling on `UserEntity`, retaining the
   existing `usuarios` table and `id`, `nombre`, `password_hash`, and `created_at`
   column behavior. Do not change the schema.
3. Add a persistence-boundary `UserMapper` with explicit conversions in both
   directions. Preserve the generated user identity, normalized name, and hashed
   credential across save/retrieval. Keep persistence lifecycle timestamp
   handling on the entity.
4. Remove JPA and `UserDetails` dependencies from `User`. Add a security principal
   adapter around domain `User` for `UserDetailsService` and JWT integration.
5. Keep services operating on domain objects. Registration and profile flows
   return domain values for controller mapping; login returns a domain user plus
   the token. Controllers map users to the existing response DTO.
6. Preserve case-insensitive duplicate lookup, surrounding-whitespace
   normalization, password hashing, endpoint statuses, response content,
   authorization header behavior, and Spanish errors.
7. Preserve the public API. The feature contract records the existing registration,
   login, and profile endpoints; the existing
   `backend/specs/001-user-auth/contracts/user-auth-api.yaml` is the compatibility
   baseline.

## Project Structure

### Documentation (this feature)

```text
backend/specs/007-user-refactor/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/
    └── user-api.yaml
```

### Source Code

```text
backend/app/src/main/java/com/dappunq/
├── model/User.java
├── persistence/
│   ├── UserEntity.java
│   ├── UserMapper.java
│   ├── UserRepository.java
│   ├── UserRepositoryImpl.java
│   └── JpaUserEntityRepository.java
├── security/  # domain UserDetails adapter
├── service/   # domain-facing orchestration
└── controller/  # response DTO mapping

backend/app/src/test/java/com/dappunq/
├── unit/
├── integration/
└── e2e/
```

**Structure Decision**: Follow the existing Gradle backend module and Java
packages. Persistence implementation stays in `com.dappunq.persistence`;
authentication adaptation stays in `com.dappunq.security`. Keep integration and
MockMvc end-to-end tests in their existing separate packages.

## Implementation Sequence

1. Obtain formal approval for a Principle II amendment that allows the
   domain-facing repository implementation to map to persistence entities; update
   the constitution version and impact report through the normal governance
   process. Do not begin code implementation before this gate passes.
2. Create `UserEntity` with the existing table/column mappings and move JPA
   lifecycle concerns off `User`.
3. Refactor `User` into a framework-independent domain model; preserve domain
   identity, normalized name, hashed credential, and domain-specific validation.
4. Implement bidirectional `UserMapper`, a Spring Data repository for
   `UserEntity`, and `UserRepositoryImpl` behind the domain-facing repository.
5. Add the security principal adapter and update `UserDetailsService`/JWT usage
   without changing authentication behavior.
6. Update service and controller boundaries to exchange domain results and map
   response DTOs in controllers; preserve established API behavior.
7. Add focused domain and mapper unit tests. Adapt directly affected integration
   fixtures and add repository save/retrieve coverage against PostgreSQL
   Testcontainers. Verify registration, login, and profile through existing
   MockMvc E2E package.
8. Run focused tests, then backend compile and test tasks. Confirm no plaintext
   credentials are stored, no entity escapes persistence, and all existing
   external contracts remain unchanged.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|---|---|---|
| Repository implementation performs entity mapping instead of the Service | Explicitly selected for this refactor so services and the domain-facing repository contract operate only on `User`. Requires an approved amendment to Principle II before implementation. | Service-side mapping would contradict the requested `UserRepositoryImpl` behavior and make the service depend on the persistence entity. |
