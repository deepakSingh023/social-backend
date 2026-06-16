# Auth Service

## Overview

The Auth Service is responsible for user authentication and account creation.

It handles:

* User registration
* User login
* Password hashing
* JWT generation
* Profile initialization

The service acts as the entry point for user authentication while delegating profile management to the Profile Service.

---

## Technology Stack

* Java 21
* Spring Boot
* Spring Security
* Spring Data MongoDB
* MongoDB
* JWT Authentication
* OpenFeign
* Resilience4j
* Async Processing

---

## Responsibilities

### User Registration

During signup the service:

1. Validates email uniqueness
2. Validates username uniqueness
3. Hashes the password using BCrypt
4. Creates the user record
5. Triggers profile creation in the Profile Service

A successful signup returns the created user information along with a Location header pointing to the newly created resource.

---

### User Login

During login the service:

1. Finds the user by email
2. Verifies the password
3. Generates a JWT token
4. Returns user details and authentication token

The JWT contains:

* User ID
* User Role
* User Email

This token is later used by downstream services for authorization.

---

## Data Model

Each user contains:

* User ID
* Username
* Email
* Password (hashed)
* Role
* Provider

Passwords are never stored in plain text.

---

## Password Security

Passwords are encoded using Spring Security's PasswordEncoder implementation.

Only hashed passwords are stored in the database.

During login, incoming credentials are verified against the stored hash.

---

## JWT Authentication

After successful login, the service generates a JWT token.

The token contains user identity information and is used by other services to authenticate incoming requests.

This allows services to verify user identity without repeatedly querying the Auth database.

---

## Profile Initialization

After a user account is created, the Auth Service triggers profile creation in the Profile Service.

Profile creation is executed asynchronously using Spring's `@Async` support.

This means user registration does not wait for profile creation to finish before returning a successful response.

The Profile Service receives:

* User ID
* Username
* Email

and creates the initial profile document.

---

## Service Communication

The service communicates with the Profile Service using OpenFeign.

This keeps inter-service communication simple while maintaining clear service boundaries.

---

## Fault Tolerance

Profile creation is considered an important operation but should not prevent successful user registration.

To improve reliability, the integration uses:

### Retry

Transient failures are automatically retried using Resilience4j Retry.

This helps recover from temporary network issues or service startup delays.

### Circuit Breaker

Resilience4j Circuit Breaker prevents repeated calls to an unhealthy downstream service.

This avoids unnecessary resource consumption during outages.

### Fallback Handling

If profile creation continues to fail after all retry attempts, a fallback method logs the failure for investigation.

The user account remains successfully created even if profile initialization fails.

This design prioritizes account creation availability while handling dependent service failures gracefully.

---

## Async Processing

Profile initialization runs in a separate thread pool using Spring Async.

Benefits:

* Faster signup response times
* Reduced request latency
* Better user experience during service-to-service communication

Current implementation uses thread-based asynchronous processing rather than a message queue.

---

## Error Handling

Custom exceptions are used for common authentication failures.

Examples:

* EmailAlreadyInUse
* UsernameAlreadyInUse
* InvalidCredentials
* UserNotFound

These exceptions are translated into appropriate API responses.

---

## Observability

The service includes lightweight observability features using Spring AOP, Micrometer, and SLF4J.

### Request Tracing

Every incoming API request is assigned a unique trace ID.

The trace ID is stored in MDC and automatically appears in application logs, making it easier to follow a request through the service.

Features:

* Unique trace ID per request
* MDC-based log correlation
* Easier debugging and troubleshooting

---

### API Logging

Controller endpoints are wrapped with a logging aspect.

For every request the service records:

* Controller name
* API method
* Request status
* Execution latency

Example log:

controller=AuthController api=login status=SUCCESS latencyMs=42

Failed requests are logged with the exception message and latency information.

---

### Metrics Collection

Micrometer is used to collect API metrics.

Captured metrics include:

* Request count
* Success count
* Error count
* Request latency
* p50 latency
* p95 latency
* p99 latency

Metrics are tagged by:

* Controller
* API
* Status

This allows integration with monitoring tools such as Prometheus and Grafana.


## Service Dependencies

### Outgoing Dependencies

* Profile Service

### Internal Components

* JWT Utility
* Password Encoder
* Async Profile Creation Service

---

## Design Notes

Authentication responsibilities are intentionally separated from profile management.

The Auth Service owns user credentials and identity, while the Profile Service owns user profile information.

This separation keeps each service focused on a single responsibility and allows independent scaling and development of authentication and profile-related features.

Profile creation is eventually consistent because it occurs asynchronously after account creation. This tradeoff improves signup performance while keeping the overall system reliable through retries and circuit breakers.
