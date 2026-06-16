# Profile Service

## Overview

The Profile Service is responsible for managing user profile data across the platform.

It stores profile information such as username, bio, profile picture, privacy settings, and profile statistics. It also acts as a source of truth for user identity data that is denormalized into other services.

The service provides APIs for profile management, profile search, internal profile lookups, and counter updates.

---

## Responsibilities

* Create profiles during user registration
* Update profile information
* Manage profile pictures
* Search users by username
* Provide profile data to other services
* Maintain profile statistics
* Trigger avatar denormalization across services

---

## Technology Stack

* Java 17
* Spring Boot
* Spring Security
* MongoDB
* OpenFeign
* Resilience4j
* Cloudflare R2
* Async Processing
* Spring Aop

---

## Data Model

Each profile contains:

* User ID
* Username
* Email
* Bio
* Profile Picture URL
* Privacy Settings
* Post Count
* Reel Count
* Followers Count
* Following Count
* Friends Count

The profile document is uniquely identified by userId.

---

## Profile Creation

Profiles are automatically created when a user signs up through the Authentication Service.

The Auth Service calls the internal profile creation endpoint.

To prevent duplicate profiles during concurrent requests, profile creation uses an idempotent create-or-fetch strategy backed by a unique index on userId.

---

## Profile Updates

Users can update:

* Bio
* Privacy settings
* Profile picture

Profile picture updates follow the process:

1. Upload image
2. Compress image before storage
3. Store image in Cloudflare R2
4. Save new image URL
5. Trigger avatar denormalization
6. Delete old image asynchronously

This ensures storage usage remains small while keeping profile images synchronized across the platform.

---

## Image Compression

Before upload, profile pictures are compressed using ImgScalr.

Default settings:

* Width: 150px
* JPEG Quality: 0.6

This significantly reduces storage usage and bandwidth while maintaining acceptable visual quality for avatar images.

---

## User Search

The service provides username-based search functionality.

Features:

* Case-insensitive search
* Cursor-based pagination
* Configurable page size

The search endpoint is intended for user discovery and friend search features.

---

## Internal APIs

Several internal APIs are exposed for communication between microservices.

### Get Internal Profile

Returns:

* Username
* Avatar URL

Used by:

* Post Service
* Reel Service
* Comment Service
* Interaction Service

This endpoint allows services to denormalize profile information without directly accessing the Profile database.

### Bulk Profile Lookup

Returns profile data for multiple users in a single request.

Used primarily by the Interaction Service when multiple user records must be resolved at once.

This reduces network calls and improves performance.

---

## Counter Management

The Profile Service stores denormalized counters.

Supported counters:

* Followers
* Following
* Friends
* Posts
* Reels

Counter updates are performed using atomic MongoDB increment operations.

The update process is asynchronous to minimize request latency.

---

## Avatar Denormalization

Many services store username and avatar information alongside their own documents.

Examples:

* Posts
* Reels
* Comments
* Friend interactions

When a user changes their profile picture, the Profile Service propagates the update to all dependent services.

This avoids runtime joins and keeps read operations fast.

---

## Fault Tolerance

Inter-service communication uses:

* OpenFeign
* Resilience4j Retry
* Resilience4j Circuit Breaker

If a dependent service becomes unavailable:

1. Requests are retried automatically.
2. Circuit breakers prevent cascading failures.
3. Failures are logged.
4. Core profile functionality remains available.

This approach improves system resilience while maintaining eventual consistency.

---

## Async Processing

Several operations are executed asynchronously:

* Counter updates
* Avatar denormalization
* Old image deletion

Moving these operations to background threads reduces response time for user-facing requests.

---

## Error Handling

Custom exceptions are used for domain-specific failures.

Examples:

* ProfileNotFound

Global exception handling ensures consistent API responses across the service.

---

## Observability

The Profile Service includes request tracing, structured logging, and application metrics through Spring AOP and Micrometer.

### Request Tracing

A unique trace ID is generated for every incoming request and stored in MDC.

This allows logs generated during request processing to be correlated using a single identifier.

---

### API Logging

A logging aspect records:

* Controller name
* API method
* Request status
* Request latency

Both successful and failed requests are captured.

This helps identify slow endpoints and troubleshoot production issues.

---

### Metrics

Micrometer metrics are automatically collected for all controller endpoints.

Tracked metrics include:

* Request count
* Success count
* Error count
* Request latency
* p50 latency
* p95 latency
* p99 latency

Metrics are tagged with:

* Controller
* Method
* Status

These metrics can be exported to monitoring systems such as Prometheus and visualized in Grafana dashboards.

---

## Service Dependencies

### Incoming Consumers

Services that call Profile Service:

* Auth Service
* Post Service
* Reel Service
* Comment Service
* Interaction Service

### Outgoing Dependencies

Services called by Profile Service:

* Post Service
* Reel Service
* Comment Service
* Interaction Service
* Cloudflare R2

---

## Design Notes

The service intentionally denormalizes username and avatar data into downstream services.

Although this increases write complexity, it significantly improves read performance by eliminating cross-service lookups during feed generation, comment retrieval, and content rendering.

The design favors fast reads and eventual consistency, which is generally preferred for social media workloads.
