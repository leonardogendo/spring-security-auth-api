# Changelog

All notable changes to this project are documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and releases follow [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0] - 2026-10-10

### Added

- Persisted user authentication using Spring Security.
- Password hashing using the configured password encoder.
- JWT access-token issuance with issuer, subject, audience issued-at and expiration claims.
- Spring Security OAuth2 Resource Server integration for bearer-token validation.
- Protected user profile endpoint at GET /api/users/me.
- PostgreSQL persistence and Flyway database migrations.
- Automated authentication and JWT validation tests.
- Stateless API security configuration and externalized security-sensitive properties.
