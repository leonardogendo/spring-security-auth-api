# Spring Security Auth Starter — Roadmap

> A reusable, security-focused Spring Boot authentication and authorization foundation for REST APIs.

This roadmap defines the planned evolution of `spring-security-auth-starter` from a minimal authentication foundation into a reusable, security-reviewed starter for modern Spring Boot applications.

The project is intentionally developed in **security-focused milestones** rather than as a large feature dump. Each milestone should leave the repository in a stable, demonstrable, and documented state.

---

## Project Direction

The starter provides developers with a secure and opinionated foundation for authentication and authorization without requiring every application to redesign the same security primitives from scratch.

As AI-assisted development makes functional code increasingly easy to generate, the harder problem remains unchanged: ensuring that security decisions, trust boundaries, and failure cases are sound. Authentication can work perfectly on the happy path while still containing weaknesses in token validation, session lifecycle, credential recovery, authorization, or abuse protection.

This project therefore provides more than authentication boilerplate. It establishes an intentionally designed security baseline built around:

* secure and opinionated defaults
* explicit trust boundaries
* documented security decisions and threat assumptions
* Spring Security-native mechanisms
* minimal custom security infrastructure
* positive and negative security testing
* production-oriented configuration patterns

It is designed for both traditional and AI-assisted development, giving developers a reviewed foundation to extend rather than reinventing authentication and authorization security for every application.

The goal is not to replace security review or engineering judgment — **it is to provide a stronger place to start.**

The initial architecture assumes a **modular monolith REST API**.

Authentication and resource protection therefore live within the same Spring Boot application while still preserving clean boundaries between authentication, token issuance, token validation, and protected resources.

---

# Roadmap Overview

```text
Phase 0 — Security Foundation & Project Baseline
        ↓
Phase 1 — Authentication Foundation
        ↓
Phase 2 — Refresh Token & Session Security
        ↓
Phase 3 — Registration & Account Verification
        ↓
Phase 4 — Authorization Model
        ↓
Phase 5 — Password Recovery & Reauthentication
        ↓
Phase 6 — Abuse Protection & Operational Security
        ↓
Phase 7 — Security Testing & CI Hardening
        ↓
Phase 8 — Starter Experience & Reference Documentation
```

---

# Phase 0 — Security Foundation & Project Baseline

**Goal:** Establish project structure, persistence layer, configuration model, and security boundaries.

**Scope:**
- Spring Boot baseline with PostgreSQL + Flyway
- Environment-based configuration with validation
- Security foundation: DelegatingPasswordEncoder, JWT configuration properties
- Externalized signing secret/key material setup

**Exit Criteria:**
- Application starts successfully
- PostgreSQL migrations run cleanly
- Security configuration loads from externalized config
- No hard-coded secrets

---

# Phase 1 — Authentication Foundation

**Goal:** Deliver complete authentication flow with JWT access tokens and basic user registration.

**Scope:**
- User model: id, email, password_hash, enabled, timestamps
- Spring Security: UserDetails, UserDetailsService, DaoAuthenticationProvider, AuthenticationManager
- JWT issuance: short-lived access tokens with iss, sub, aud, iat, exp claims
- Resource Server: OAuth2 bearer-token validation
- Protected endpoint: GET /api/users/me
- Authentication failure responses
- User registration with email normalization
- Password policy validation

**Security Requirements:**
- Passwords never stored in plaintext
- Signing secrets never committed to source control
- JWT signature, issuer, audience validation enforced
- Stateless sessions, explicit CORS
- Prevent account enumeration in registration responses

**Tests:**
- Unit: token construction, expiration, auth service behavior
- Integration: valid/invalid credentials, malformed/invalid/expired JWT, wrong issuer/audience
- Registration: valid/invalid email formats, password policy violations

**Release:** v0.1.0 — Authentication Foundation

---

# Phase 2 — Refresh Token & Session Security

**Goal:** Long-lived sessions without long-lived access tokens.

**Scope:**
- Refresh tokens: server-side persistence, rotation, replay detection, revocation
- Logout with session invalidation
- Session/device metadata

**Security Requirements:**
- Hash refresh tokens in database
- Rotate on use, invalidate superseded tokens
- Detect reuse and revoke token family
- Document browser storage strategy

**Release:** v0.2.0

---

# Phase 3 — Account Verification

**Goal:** Secure account lifecycle with email verification.

**Scope:**
- Email verification tokens: random, scoped, expiring, single-use
- Account enablement rules
- Email verification flow

**Security Requirements:**
- Prevent account enumeration in verification responses
- Plan registration abuse controls

**Release:** v0.3.0

---

# Phase 4 — Authorization Model

**Goal:** Explicit application authorization beyond authentication.

**Scope:**
- Roles and authorities/permissions
- JWT authority claims
- Method-level authorization with @PreAuthorize
- Resource ownership checks
- Default-deny policy

**Example Model:**
- ROLE_ADMIN: user:read, user:create, user:update
- ROLE_USER: profile:read

**Release:** v0.4.0

---

# Phase 5 — Password Recovery & Reauthentication

**Goal:** Secure account recovery path.

**Scope:**
- Forgot-password flow with reset tokens
- Password change with session revocation
- Optional reauthentication for sensitive operations

**Security Requirements:**
- Generic responses, no account enumeration
- High-entropy reset tokens, short expiry, single-use
- Audit security events

**Release:** v0.5.0

---

# Phase 6 — Abuse Protection & Operational Security

**Goal:** Protect against automated and operational threats.

**Scope:**
- Rate limiting: login, registration, password-reset throttling
- Security headers configuration
- Explicit CORS policy
- Audit events: LOGIN_SUCCESS, LOGIN_FAILURE, TOKEN_REFRESH, LOGOUT, etc.

**Release:** v0.6.0

---

# Phase 7 — Security Testing & CI Hardening

**Goal:** Continuously verifiable security behavior.

**Scope:**
- Automated tests: unit, integration, security tests
- CI checks: dependency scanning, SAST, secret scanning
- Negative testing: tampered/expired JWT, reused tokens, invalid permissions

**Release:** v0.7.0

---

# Phase 8 — Starter Experience & Reference Documentation

**Goal:** Repository reusable by other developers.

**Scope:**
- Developer experience: clone-and-run instructions, .env.example, Docker Compose, API examples
- Security documentation: architecture.md, authentication-model.md, threat-model.md, security-decisions.md
- Document security decisions with context, threat addressed, alternatives, trade-offs

**Release:** v1.0.0 — Stable Starter Foundation

---

# Future Extensions

Features deferred until foundation is mature:
- Asymmetric JWT signing, JWKS, key rotation
- OAuth2 Authorization Server, OpenID Connect
- MFA/TOTP, WebAuthn/passkeys
- Account lockout policies, administrative revocation
- External identity providers

---

# Release Strategy

Milestone-based semantic releases:
- v0.1.0 — Authentication Foundation
- v0.2.0 — Refresh Token & Session Security
- v0.3.0 — Registration & Email Verification
- v0.4.0 — Authorization Model
- v0.5.0 — Password Recovery & Reauthentication
- v0.6.0 — Abuse Protection & Audit Security
- v0.7.0 — Security Testing & CI Hardening
- v1.0.0 — Stable Starter Foundation

---

# Definition of Done

Each milestone must satisfy:
1. **Functional:** Intended user flow works
2. **Security:** Misuse, tampering, unauthorized behavior rejected
3. **Testing:** Positive and negative behaviors automatically verified
4. **Documentation:** What, how, why, threats addressed, what's not supported

---

# Current Focus

**v0.1.0 — Authentication Foundation**

Implement persisted user authentication → Spring Security login → JWT access token → Resource Server validation → protected resource access → basic user registration.

Narrow: no refresh tokens, RBAC, email verification, or password reset until foundation is stable.
