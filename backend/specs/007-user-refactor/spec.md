# Feature Specification: User Domain and Persistence Separation

**Feature Branch**: `007-user-refactor`

**Created**: 2026-10-01

**Status**: Draft

**Input**: User description: "Refactor the User model to comply with our domain rules. Extract all persistence concerns into a new UserEntity class in the persistence layer."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Enforce User domain rules (Priority: P1)

As a maintainer, I need User to represent valid user-domain state independently of storage and framework behavior, so that business rules remain consistent regardless of how users are persisted or authenticated.

**Why this priority**: A framework-independent model is the foundation for the requested separation and for enforcing domain invariants in one place.

**Independent Test**: Exercise User creation and its domain behavior without starting persistence or application infrastructure.

**Acceptance Scenarios**:

1. **Given** a nonblank user name and credential value, **When** a User is created, **Then** its name is trimmed and its valid domain state is available without persistence or framework services.
2. **Given** a blank or missing user name or credential value, **When** a User is created, **Then** the applicable domain invariant rejects the state with a domain-specific error.
3. **Given** any User domain behavior, **When** it is exercised, **Then** it does not require persistence annotations or framework-specific contracts.

---

### User Story 2 - Store and retrieve users without leaking persistence details (Priority: P1)

As a maintainer, I need user records to be stored through a persistence-specific representation and mapped at the application boundary, so that database structure and lifecycle concerns do not become part of the domain model.

**Why this priority**: Separating the persistence representation from User directly fulfills the architecture rule and allows storage details to evolve without changing domain behavior.

**Independent Test**: Save and retrieve a user through the persistence boundary, then verify the domain-relevant identity, name, and credential state are preserved.

**Acceptance Scenarios**:

1. **Given** a valid User, **When** the application persists and retrieves it, **Then** the resulting domain User preserves the values needed for profile lookup and authentication.
2. **Given** persistence-only mapping or lifecycle data, **When** a User is loaded or saved, **Then** that data is handled by the persistence representation and does not require persistence annotations or lifecycle callbacks on User.
3. **Given** a persistence operation, **When** its result crosses into the service or controller flow, **Then** persistence entities are not exposed as domain or API response objects.

---

### User Story 3 - Preserve existing user and authentication flows (Priority: P1)

As a registered user, I need profile lookup, registration, and login to continue working as before the internal refactor, so that separating domain and persistence responsibilities does not disrupt existing clients.

**Why this priority**: The change is an internal refactor; preserving the established behavior is necessary for users and API clients.

**Independent Test**: Run the existing profile, registration, and login scenarios against the refactored persistence boundary and compare their externally observable results with the current behavior.

**Acceptance Scenarios**:

1. **Given** an existing user, **When** the profile is requested by identifier, **Then** the request returns the same successful response with the user's name.
2. **Given** a new name and valid password, **When** registration succeeds, **Then** the response and stored credential behavior remain unchanged; the stored password remains hashed.
3. **Given** a name already registered, **When** registration is attempted using a name that differs only by letter case or surrounding whitespace, **Then** the existing duplicate-user behavior is preserved.
4. **Given** valid credentials, **When** login is performed, **Then** the response and authorization token behavior remain unchanged.
5. **Given** invalid credentials or an unknown profile identifier, **When** login or profile lookup is attempted, **Then** the existing error status and Spanish message behavior is preserved.

### Edge Cases

- A null, empty, or whitespace-only name or credential value is rejected as an invalid domain state and is not persisted.
- Leading or trailing whitespace in a user name is normalized consistently for registration and login; case-insensitive duplicate detection remains effective.
- A user record loaded from persistence must satisfy the domain invariants; invalid persisted state must not be silently treated as a valid User.
- Mapping a saved user back to the domain must not lose identity needed for profile lookup or credential data needed for authentication.
- Database-generated identity and persistence lifecycle values remain available to persistence operations without adding persistence annotations or lifecycle callbacks to User.
- Failed registration, login, or profile lookup retains existing externally visible behavior and does not return a success-shaped fallback.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST represent user business state and behavior in a plain domain User model that has no persistence annotations and does not depend on persistence or framework-specific contracts.
- **FR-002**: The User model MUST enforce its own domain invariants, including a nonblank normalized name and a nonblank credential value, and MUST signal invariant violations with domain-specific errors.
- **FR-003**: The persistence layer MUST provide a separate UserEntity class for persistence mapping, annotations, database identity mapping, and persistence lifecycle concerns.
- **FR-004**: The application MUST map between User and UserEntity at the service/persistence boundary so persistence entities do not become the domain model or API response model.
- **FR-005**: Services MUST coordinate persistence using domain User values and MUST return domain models to controllers; controllers or their dedicated mappers MUST create response DTOs.
- **FR-006**: Persistence and authentication adaptations MUST allow the domain User model to remain independent of framework-specific security contracts while keeping authentication operational.
- **FR-007**: Registration MUST continue to normalize names by trimming surrounding whitespace and reject duplicate names case-insensitively with the established error behavior.
- **FR-008**: Login MUST continue to authenticate using the existing name and password behavior and return the established user response and authorization token on success.
- **FR-009**: User profile lookup MUST continue to return the established user-name response for an existing identifier and the established not-found response for an unknown identifier.
- **FR-010**: Persisted credentials MUST remain hashed; the refactor MUST NOT persist plaintext passwords.
- **FR-011**: The refactor MUST preserve the existing externally observable registration, login, and profile response contracts, including their established status codes, response content, authorization header, and Spanish error messages.
- **FR-012**: Persistence mapping MUST preserve all domain-relevant values needed for user identity, profile lookup, and authentication across save and retrieval.
- **FR-013**: Each domain behavior MUST have a single responsibility, and persistence MUST NOT invoke User business logic.

### Key Entities *(include if data involved)*

- **User**: The framework-independent domain representation of a user, containing valid business state and enforcing its domain invariants.
- **UserEntity**: The persistence-layer representation of a user, responsible for database mapping, identity mapping, and persistence lifecycle concerns; it is not exposed as the domain or API response object.
- **User response**: The existing API representation containing the user's public name and preserving established endpoint contracts.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of the defined valid User states are accepted and 100% of the defined invalid User states are rejected.
- **SC-002**: Every user save-and-retrieve scenario preserves the domain-relevant identity, normalized name, and hashed credential needed by current flows.
- **SC-003**: All 3 existing user journeys—registration, login, and profile lookup—retain their established successful response contracts.
- **SC-004**: All defined duplicate-user, invalid-credential, and unknown-profile scenarios retain their established failure status and Spanish error behavior.
- **SC-005**: 0 registration or persistence scenarios store a plaintext password.

## Assumptions

- The existing registration, login, and profile endpoint contracts are compatibility requirements for this internal refactor.
- Existing name normalization (trim surrounding whitespace) and case-insensitive duplicate detection are intended user-domain behavior.
- The credential value held by User for persistence and authentication is a password hash; password encoding remains part of the authentication/application flow.
- Persistence-generated identifiers and lifecycle metadata may be mapped as required by the persistence boundary, but do not make User a persistence object.
- English is used for this feature specification as requested; existing user-facing messages remain in Spanish.
