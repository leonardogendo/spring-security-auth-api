# Spring Security Auth Starter

A security-focused, opinionated Spring Boot authentication and authorization foundation for REST APIs.

`spring-security-auth-starter` is a reusable Spring Boot starter for building authentication and authorization into modern applications without redesigning the same security primitives from scratch.

The project focuses not only on making authentication work, but on making the underlying **security decisions, trust boundaries, failure cases, and trade-offs explicit and testable**.

> **Status:** Under Active Development

> **Current Release**: `v0.1.0: Authentication Foundation`.

---

## Why This Project Exists

AI-assisted development has made functional application code increasingly easy to generate. Authentication and authorization remain security-sensitive engineering problems. An implementation can compile, pass the happy path, and appear correct while still containing weaknesses in token validation, session lifecycle, credential recovery, authorization boundaries, or abuse protection.

This project provides an intentionally designed baseline built around:

* secure and opinionated defaults
* explicit trust boundaries
* documented security decisions and threat assumptions
* Spring Security-native mechanisms
* minimal custom security infrastructure
* positive and negative security testing
* production-oriented configuration patterns

The goal is not to replace security review or engineering judgment. **It is to provide a stronger place to start.**

---

## Current Release

### `v0.1.0 — Authentication Foundation`

Establishes the initial authentication baseline for Spring Boot REST APIs. covering user authentication, JWT-based access, and protected resource access.

### Implemented

- User registration and login
- Password hashing and secure credential storage
- Spring Security authentication
- JWT-access token issuance and validation
- Protected user resource access

### Next Milestone

**v0.2.0 — Refresh Token & Session Security**

Introduce secure session lifecycle management through persisted refresh tokens, token rotation, replay detection, revocation, and logout with session invalidation.

See [`ROADMAP.md`](./ROADMAP.md) for complete development plan, and

[`CHANGELOG.md`]() for release history.

---

## Security Model

The starter uses Spring Security-native mechanisms to establish authentication and protect REST API resources.

**Authentication:** 
Spring Security's AuthenticationManager, DaoAuthenticationProvider, UserDetailsService, and PasswordEncoder provide the credential authentication flow.
Passwords are stored as hashes, never plaintext.

**Access Tokens:** 
JWT access tokens establish authenticated API requests. Token validation must verify signature and enforce the configured issuer, audience, and expiration requirements before accepting a token.

**Resource Protection:** 
Spring Security OAuth2 Resource Server handles bearer-token processing and JWT validation. Protected endpoints require valid authentication, with authorization policies enforced server-side. 

See [`docs/`]() for detailed security architecture, authentication behavior and design decisions.

---

## Tech Stack

**Core:** 

`Java`, `Spring Boot`, `Spring Security`, `Spring Security OAuth2 Resource Server`, `PostgreSQL`, `Flyway`, `Maven`

**Security:** 

`Spring Security` authentication, `JWT` access tokens, `Spring Security JOSE/JWT`, password hashing

---

## Getting Started

**Prerequisites:** Java 25, Maven, PostgreSQL

### Setup

```bash
git clone https://github.com/leonardogendo/spring-security-auth-starter.git
cd spring-security-auth-starter
```

### Configuration

Generate a secure JWT signing secret:

```bash
export JWT_SECRET="$(openssl rand -base64 32)"
```

Configure the database connection using `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`, or use the development defaults defined in `application-dev.yaml`.

### Run

Start the application using the development profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

For production, configure the required environment variables and activate the `prod` profile. Never commit real credentials or signing secrets to source control.

---

## Testing

The project uses layered automated testing to verify application behavior, component integration, and security controls.

- **Unit tests**: Verify individual units and security logic in isolation.
- **Slice tests**: Verify focused Spring application layers within a limited application context.
- **Integration tests**: Verify interactions across application components and security boundaries.

Security testing emphasizes both positive and negative behavior, including authentication failures, invalid or expired JWTs, signature validation, issuer and audience validation, and unauthorized access.

A security control is not complete simply because its happy path works.

---

## Documentation

Security and architecture documentation will evolve alongside implemented capabilities.

Planned documentation includes:

- `architecture.md` — System structure and component boundaries
- `authentication-model.md` — Authentication flow and credential handling
- `token-model.md` — JWT claims, validation, and token lifecycle
- `authorization-model.md` — Access-control policies and enforcement
- `threat-model.md` — Threat assumptions, attack surfaces, and mitigations
- `security-decisions.md` — Security decisions, alternatives, and trade-offs
- `deployment-security.md` — Deployment-specific security considerations

Documentation focuses on both **how** the system works and **why** its security decisions were made.

---

## Security Notice

This repository provides a security-focused starting point, not a universal authentication solution. 

Security requirements vary by application, client type, deployment environment, threat model, and regulatory context. 

Before production use: 
- Review threat model
- Configure secrets and key management appropriately
- Validate deployment-specific security controls
- Perform application-specific security testing

Cloning this repository does not remove the need for security review.

---

## References

Security decisions informed by: 
- Spring Security Reference Documentation
- OWASP Authentication/Password Storage/Session Management/Authorization Cheat Sheets

---

## Maintainer

**Leonard Ogendo** — Secure Software Engineer | Application Security
