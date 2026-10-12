# Research: User Domain and Persistence Separation

## Current backend patterns

- The backend is the `backend/app` Gradle module, using Java 25, Spring Boot 4,
  Spring Data JPA, Spring Security, and PostgreSQL.
- Persistence entities such as `PlayerEntity` live in
  `com.dappunq.persistence`. `PlayerMapper` already demonstrates explicit
  conversion between an entity and a domain object.
- `UserRepository` currently extends `JpaRepository<User, Long>` and therefore
  makes the domain `User` the JPA entity. User persistence must move to a Spring
  Data interface parameterized by `UserEntity`.
- `User` currently implements `UserDetails` and carries JPA annotations. The
  existing `UserService` provides `UserDetailsService`, and `JwtService` accepts
  `UserDetails`; a security adapter can preserve those framework contracts
  without retaining them on the domain model.
- User registration, login, and profile use MockMvc with PostgreSQL
  Testcontainers. E2E tests are already in `com.dappunq.e2e`, separate from
  integration tests.
- The existing user-auth OpenAPI contract is
  `../001-user-auth/contracts/user-auth-api.yaml`; it describes registration,
  login, profile lookup, current response shapes, and status codes.

## Decisions

### Mapping boundary

- **Decision**: Keep a domain-facing `UserRepository` and implement it with
  `UserRepositoryImpl`; the implementation maps `User` and `UserEntity` and
  delegates persistence to a separate Spring Data JPA repository.
- **Rationale**: This is the explicit requested design and keeps `UserEntity`
  invisible to services and controllers.
- **Alternatives considered**: Map in the Service before calling a repository
  that operates on `UserEntity`, as required by the current Constitution
  Principle II. This is not selected because it conflicts with the user's
  repository-adapter choice.
- **Governance**: Repository-internal mapping conflicts with the constitution as
  currently written. Obtain and document an approved amendment before
  implementing this design; do not treat this research decision as approval.

### Persistence mapping

- **Decision**: Keep the current table and column layout on `UserEntity`, and
  map the generated identity and all authentication/profile domain values
  explicitly.
- **Rationale**: The feature is an internal refactor, and the existing schema
  and public user flows are compatibility requirements.
- **Alternatives considered**: Change schema or use the persistence entity as a
  domain model; both add migration or framework coupling without satisfying the
  separation requirements.

### Authentication integration

- **Decision**: Adapt the domain `User` to `UserDetails` with a dedicated
  principal object; retain Spring Security interfaces at the security boundary.
- **Rationale**: `UserDetailsService` and JWT generation can continue operating
  while `User` loses framework dependencies.
- **Alternatives considered**: Have `User` continue implementing `UserDetails`
  or make `UserEntity` the security principal; each leaks framework
  responsibilities into a model that should be independent.

### Validation strategy

- **Decision**: Add focused domain and mapper unit tests, database-backed
  repository integration tests with PostgreSQL Testcontainers, and maintain
  MockMvc E2E tests in `com.dappunq.e2e`.
- **Rationale**: This follows the constitution's testing rules and the existing
  test layout.
- **Alternatives considered**: Mock-only persistence verification is insufficient
  to prove schema mapping and save/retrieve behavior.

## Resolved unknowns

Java/Spring/Gradle/PostgreSQL/test versions and the existing endpoint contract
were found in the backend project files and feature specification. No technical
unknown remains that blocks the plan. The constitution amendment is an approval
gate, not an unresolved design choice.
