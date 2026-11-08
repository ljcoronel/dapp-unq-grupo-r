<!--
Sync Impact Report
- Version change: 1.0.0 -> 2.0.0
- Modified principles:
  - Responsabilidad Unica por Componente -> Single Responsibility per Component
  - Modularizacion del Consumo de API -> Modular API Consumption
  - Robustez de Requests y Manejo de Fallos -> Request Robustness and Failure Handling
  - Feedback de Estado para el Usuario -> User State Feedback
  - Estandares de Codigo y Estilo -> Code and Style Standards
- Added sections:
  - Responsive Web Application (new principle)
- Removed sections:
  - None
- Follow-up TODOs:
  - TODO(RATIFICATION_DATE): confirm initial ratification date if it differs from the current date.
-->
# Frontend Constitution

## Core Principles

### Responsive Web Application
The frontend MUST be a responsive web application that adapts its layout and
interactions to all common screen sizes and device types (desktop, tablet, and
mobile). Every view and component MUST render correctly and remain fully usable
across viewport widths from 320px to wide-screen monitors. Responsive behavior
MUST be implemented using TailwindCSS responsive utilities (breakpoint prefixes)
rather than separate mobile-specific codebases or third-party responsive
frameworks.
Rationale: ensures accessibility and usability for the widest possible audience
without maintaining parallel implementations.

### Single Responsibility per Component
Each UI component MUST have a single, clearly defined functional responsibility.
Composition between components MUST be performed without mixing domain logic,
rendering, and side effects in the same unit, unless explicitly justified during
review.
Rationale: improves maintainability, testability, and incremental evolution of
the frontend.

### Modular API Consumption
All calls to external services MUST be extracted into dedicated API modules.
Components MUST consume functions from those modules and MUST NOT construct HTTP
requests directly in the presentation layer.
Rationale: centralizes network contracts, reduces duplication, and improves
change traceability.

### Request Robustness and Failure Handling
All API requests MUST be made using Axios. Each request MUST include explicit
handling of network errors, failed responses, and null-data scenarios according
to the expected contract.
Rationale: prevents silent failures and establishes consistent behavior during
incidents.

### User State Feedback
The interface MUST communicate loading, error, and null-data states in a
user-friendly and visible manner throughout the lifecycle of a request. The user
MUST receive actionable and understandable messages instead of raw technical
failures without context.
Rationale: improves the user experience and reduces ambiguity in critical flows.

### Code and Style Standards
Styling MUST be implemented with TailwindCSS. Indentation MUST be 2 spaces.
Frontend JavaScript/TypeScript code MUST favor arrow functions, object
destructuring, and async/await where applicable.
Rationale: standardizes readability and reduces collaboration friction within
the team.

## Technology Stack

The official frontend of this project MUST be built with React.js. Routing MUST
be implemented with React Router. The HTTP client MUST be Axios and the styling
system MUST be TailwindCSS.

Introducing alternative libraries for these responsibilities is not permitted
without an explicit amendment to this constitution.

## Development Workflow and Compliance

Every frontend change proposal MUST demonstrate conformity with the principles
above during review. Exceptions MUST document technical justification, scope,
and a normalization plan.

Component architecture and API consumption decisions MUST be evaluated against
this constitution before merging relevant changes.

## Governance

This constitution supersedes ad-hoc frontend practices. Every amendment MUST be
recorded in this document and MUST include the rationale, impact, and a
semantically correct version update.

Constitutional versioning policy:
- MAJOR: backward-incompatible removal or redefinition of principles or
  governance rules.
- MINOR: addition of new principles/sections or material expansion of
  guidelines.
- PATCH: non-semantic clarifications, wording, or editorial corrections.

Compliance:
- Every frontend change review MUST verify constitutional compliance.
- Non-compliances MUST be corrected before merge or approved as a documented
  exception.
- A periodic compliance review at release milestones is recommended.

**Version**: 2.0.0 | **Ratified**: TODO(RATIFICATION_DATE): confirm initial date | **Last Amended**: 2026-11-08
