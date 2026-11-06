# Data Model: User Domain and Persistence Separation

## Domain: `User`

Framework-independent domain object in `com.dappunq.model`.

| Field | Type | Domain meaning | Invariant / mapping |
|---|---|---|---|
| `id` | `Long` (nullable before persistence) | User identity used by profile lookup | Set from the persistence-generated identity after save; no JPA annotation. |
| `nombre` | `String` | Normalized user name | Required and nonblank; trim surrounding whitespace when constructed. |
| `passwordHash` | `String` | Encoded credential used for authentication | Required and nonblank; contains an encoded hash, never a plaintext password. |

`User` owns its invariants and raises a domain-specific error for invalid state.
It has no `jakarta.persistence` or `org.springframework.security` dependencies.

## Persistence: `UserEntity`

JPA entity in `com.dappunq.persistence`; the only user object carrying persistence
annotations and lifecycle behavior.

| Field | Existing database mapping | Constraints / lifecycle |
|---|---|---|
| `id` | `usuarios.id` | Database-generated identity (`IDENTITY`). |
| `nombre` | `usuarios.nombre` | Required and unique; preserve existing case-insensitive lookup behavior. |
| `passwordHash` | `usuarios.password_hash` | Required; persist only the password encoder's hash. |
| `createdAt` | `usuarios.created_at` | Required, non-updatable; initialized by persistence lifecycle logic. |

No schema migration is intended. The entity owns creation metadata; `createdAt`
is not required in the domain model because it is persistence lifecycle data.

## Mapping

`UserMapper` converts in both directions:

- Domain to entity: copy the existing identity when present, normalized name, and
  encoded credential; do not create or transform plaintext credentials.
- Entity to domain: copy identity, name, and credential hash. Domain validation
  applies to loaded state; malformed persisted state must not silently produce a
  valid-looking user.
- After a save, map the persisted entity back to `User` so generated identity is
  available to callers.

`UserRepositoryImpl` owns calls to the mapper and delegates to a Spring Data JPA
repository whose aggregate type is `UserEntity`. Services see only the
domain-facing `UserRepository` and `User` values.

## Authentication adapter

A security principal wraps a domain `User` and implements Spring Security's
`UserDetails` contract. It exposes the name and password hash needed by existing
authentication and token flows, without moving security state or annotations
into `User` or `UserEntity`.

## Lifecycle and flow

1. Registration validates/normalizes the request, hashes the password, and
   creates a valid domain `User`.
2. `UserRepositoryImpl` maps the domain value to `UserEntity`, persists it, then
   maps the stored entity back to a domain value carrying the generated ID.
3. Login/profile repository reads map the entity into a validated domain `User`.
4. Authentication adapts the domain value to the security principal; controller
   code maps service results to the existing response DTOs.

The external API contract remains unchanged: registration/login return the
existing name response; login retains the authorization token header; profile
lookup returns the same name response and existing not-found behavior.
