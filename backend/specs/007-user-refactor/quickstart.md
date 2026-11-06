# Quickstart: Validate User Domain and Persistence Separation

## Prerequisites

- Java 25 compatible JDK.
- Docker available to Testcontainers for PostgreSQL integration and MockMvc E2E
  scenarios.
- The approved constitution amendment permitting repository-internal mapping
  must be recorded before implementation starts.

Run commands from the `backend` directory using PowerShell.

## Focused checks

After implementation, run focused User and mapper unit coverage:

```powershell
.\gradlew.bat :app:test --tests "com.dappunq.unit.UserTest" --tests "com.dappunq.unit.UserMapperTest"
```

Run registration, login, and profile integration checks against PostgreSQL
Testcontainers:

```powershell
.\gradlew.bat :app:test --tests "com.dappunq.integration.AuthRegisterIntegrationTest" --tests "com.dappunq.integration.AuthLoginIntegrationTest" --tests "com.dappunq.integration.UserProfileIntegrationTest"
```

Run the user end-to-end scenario through MockMvc:

```powershell
.\gradlew.bat :app:test --tests "com.dappunq.e2e.UserAuthE2ETest"
```

Finally, compile and run the backend test suite:

```powershell
.\gradlew.bat :app:compileJava
.\gradlew.bat :app:test
```

## Expected results

- Domain tests accept valid users and reject null, blank, or whitespace-only
  names and credentials with domain-specific errors.
- Mapper and repository tests preserve the identity, normalized name, and hashed
  credential across save and retrieval; database-generated creation metadata
  remains on `UserEntity`.
- Integration and E2E tests preserve duplicate-name errors, login behavior,
  profile lookup behavior, Spanish errors, response bodies/statuses, and the
  authorization header.
- Stored credentials are hashes, `User` has no JPA/Spring Security dependency,
  and `UserEntity` does not escape persistence.
- The existing API compatibility contract in
  [`contracts/user-api.yaml`](./contracts/user-api.yaml) remains satisfied.
