# Feature Specification: Scoped API Key Access

**Feature Branch**: `006-apikey`

**Created**: 2026-09-27

**Status**: Draft

**Input**: User description: "Generate and authorize API Keys as an alternative authentication method to the standard Bearer token, granting access to protected endpoints while excluding authentication routes."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Access protected resources with an API Key (Priority: P1)

As an authenticated API consumer, I want to use an API Key instead of a Bearer token so that I can access protected resources through an alternative authentication method.

**Why this priority**: This is the core value of the feature and enables clients that cannot conveniently use the standard token flow.

**Independent Test**: Send a request with a valid API Key to each representative protected resource and confirm that the request is authenticated as the API Key owner and receives the same authorized access available to a Bearer-token-authenticated user.

**Acceptance Scenarios**:

1. **Given** a valid API Key belonging to an existing user, **When** the user sends it in the `x-api-key` header to a protected endpoint such as `GET /players`, **Then** the system authenticates the user and permits the request according to the endpoint's normal protected access rules.
2. **Given** a request without an API Key, **When** the user sends a valid Bearer token to a protected endpoint, **Then** the existing Bearer-token authentication behavior remains unchanged.

---

### User Story 2 - Reject invalid API Keys (Priority: P1)

As a system owner, I want invalid API Keys to be rejected so that unauthorized clients cannot access protected resources.

**Why this priority**: Rejecting unknown or unusable credentials is essential to preserve the security boundary of every protected endpoint.

**Independent Test**: Send protected requests with missing, malformed, unknown, or revoked API Keys and confirm that no protected resource is returned and the response uses an unauthorized status.

**Acceptance Scenarios**:

1. **Given** an unknown or invalid API Key, **When** the client sends it to a protected endpoint, **Then** the system rejects the request with `401 Unauthorized` or `403 Forbidden`.
2. **Given** a request that contains an API Key and a Bearer token with conflicting identities, **When** the request reaches a protected endpoint, **Then** the API Key identity is used consistently according to the API Key authentication rule and the request is not silently attributed to the other identity.

---

### User Story 3 - Prevent API Key use on authentication routes (Priority: P1)

As a system owner, I want API Key-authenticated requests blocked from authentication routes so that API Keys cannot be used to trigger login or registration flows.

**Why this priority**: Authentication endpoints have distinct public or password-based semantics and must not be invoked through the alternative credential.

**Independent Test**: Send API Key-authenticated requests to `POST /login` and `POST /register` and confirm that both are rejected without executing the authentication operation.

**Acceptance Scenarios**:

1. **Given** a valid API Key, **When** the client sends it to `POST /login`, **Then** the system rejects the request with `401 Unauthorized` or `403 Forbidden` and does not log the user in.
2. **Given** a valid API Key, **When** the client sends it to `POST /register`, **Then** the system rejects the request with `401 Unauthorized` or `403 Forbidden` and does not create a user.
3. **Given** a request to `POST /login` or `POST /register` without an API Key, **When** the request follows the existing public or password-based flow, **Then** that existing flow remains available.

---

### Edge Cases

- A request with an empty, whitespace-only, malformed, expired, revoked, or otherwise unusable API Key is rejected and does not reach the protected endpoint.
- A request includes the API Key header more than once; the system rejects the ambiguous credential rather than selecting one silently.
- A request uses an API Key with an unsupported header value or casing; the system applies the same documented header parsing and validation rules consistently.
- A valid API Key is presented to `POST /login` or `POST /register`; the request is rejected before either operation executes.
- API Key management is attempted with an API Key rather than a Bearer token; the request is rejected to prevent a key from creating or revoking credentials.
- API Key authentication is attempted for a user that no longer exists or is no longer eligible to authenticate; the request is rejected without exposing user details.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST generate an API Key associated with an existing eligible user and make the key available through authenticated API Key management operations.
- **FR-002**: The system MUST accept an API Key supplied in the `x-api-key` header as an alternative to a Bearer token for protected endpoints.
- **FR-003**: The system MUST resolve a valid API Key to its associated user before allowing access to a protected endpoint.
- **FR-004**: The system MUST grant a successfully authenticated API Key the same protected-endpoint access scope as the associated user's standard authentication, except for the exclusions in FR-007 and FR-008.
- **FR-005**: The system MUST reject missing, malformed, unknown, expired, revoked, or otherwise invalid API Keys when the request relies on API Key authentication.
- **FR-006**: The system MUST return `401 Unauthorized` or `403 Forbidden` for a rejected API Key without exposing the key value or sensitive details about the associated user.
- **FR-007**: The system MUST reject any API Key-authenticated request to `POST /login` with `401 Unauthorized` or `403 Forbidden`, and MUST NOT execute the login operation.
- **FR-008**: The system MUST reject any API Key-authenticated request to `POST /register` with `401 Unauthorized` or `403 Forbidden`, and MUST NOT execute the registration operation.
- **FR-009**: The system MUST preserve the existing behavior of `POST /login` and `POST /register` when no API Key is supplied.
- **FR-010**: The system MUST apply one consistent precedence rule when a request contains both an API Key and a Bearer token, and MUST prevent credentials from being combined to obtain access beyond the authenticated user's permissions.
- **FR-011**: The system MUST ensure that API Key authentication does not bypass the authorization rules that apply to the associated user on protected endpoints.
- **FR-012**: The system MUST record security-relevant API Key events, including generation and rejected authentication attempts, without recording full secret key values.
- **FR-013**: The system MUST require authentication for every route except `POST /login` and `POST /register`; protected routes MUST accept either a valid Bearer token or a valid API Key, while API Key management routes MUST require a Bearer token.

### Key Entities

- **API Key**: A secret credential associated with one eligible user, with a lifecycle state that determines whether it can authenticate requests. It has a non-secret identifier or metadata, creation information, and optional expiration or revocation state.
- **User**: The existing account that owns an API Key and whose identity and permissions are used for protected access.
- **Authentication Request**: An incoming request containing credentials and a target route, evaluated to determine whether the request may proceed.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: In acceptance testing, 100% of representative protected endpoints accept a valid API Key for the associated user without requiring a Bearer token.
- **SC-002**: In acceptance testing, 100% of requests using invalid, revoked, expired, or unknown API Keys are rejected before the protected operation is performed.
- **SC-003**: In acceptance testing, 100% of API Key requests to `POST /login` and `POST /register` are rejected, and zero login or registration side effects are created.
- **SC-004**: At least 95% of valid API Key requests complete within the same response-time target as equivalent authenticated requests using the standard token.
- **SC-005**: Security review confirms that no full API Key value appears in user-facing responses or security records.
- **SC-006**: Requests without JWT or API Key credentials are rejected on every route except `POST /login` and `POST /register`.

## Assumptions

- API Keys are generated only for users who already exist and are eligible for authenticated access.
- The existing Bearer-token authentication remains supported on all protected routes.
- `POST /login` and `POST /register` are the only routes that do not require authentication; documentation and API Key management routes are protected.
- API Key management operations are available only to Bearer-authenticated users, and each operation is restricted to that user's own keys.
- If a request supplies both a Bearer token and an API Key, the system rejects the ambiguous credentials rather than choosing one.
- `401 Unauthorized` and `403 Forbidden` are both acceptable rejection statuses where the existing API convention does not prescribe one.
- The API Key value is treated as a secret and is shown to the user only in the successful creation response.
- API Key generation, storage, expiration, and revocation follow the project's established security and credential-lifecycle policies.
- Key rotation uses create, migrate, and revoke; it does not replace an active secret in place.
