# Profile Service

The Profile Service manages user profile data and acts as the source of truth for profile information used across the platform.

It stores profile data and profile statistics, manages profile pictures in Cloudflare R2, provides profile APIs to other services, and publishes profile changes through Kafka for asynchronous denormalization.

---

## Responsibilities

* Create profiles during user registration
* Update profile information
* Manage profile pictures
* Search users by username
* Provide profile data to other services
* Maintain profile counters
* Publish profile changes for downstream denormalization

---

## Technology Stack

* Java 17
* Spring Boot
* Spring Security
* MongoDB
* Apache Kafka
* Spring Kafka
* Cloudflare R2
* OpenFeign
* Spring AOP
* Micrometer
* Spring Boot Actuator
* OpenTelemetry

---

# Data Model

Each profile contains information such as:

* User ID
* Username
* Email
* Bio
* Profile picture URL
* Privacy settings
* Post count
* Reel count
* Followers count
* Following count
* Friends count

The profile document is identified by `userId`.

---

# Profile Creation

Profiles are created as part of the user registration flow.

The Auth Service communicates with Profile Service to create the profile.

Profile creation uses an idempotent create-or-fetch approach so that repeated requests for the same user do not result in duplicate profile documents.

The profile's `userId` is uniquely indexed in MongoDB.

---

# Profile Updates

Users can update information such as:

* Bio
* Privacy settings
* Profile picture

Profile picture updates follow this flow:

```text id="7w7h6c"
Client
   |
   v
Profile Service
   |
   v
Image processing
   |
   v
Cloudflare R2
   |
   v
MongoDB
   |
   v
Outbox
   |
   v
Kafka
   |
   +----------+-----------+-------------+
   |          |           |             |
   v          v           v             v
 Post       Reel        Likes      Interaction
Service    Service      Service       Service
```

The profile itself is updated synchronously, while propagation of the new avatar information to other services is handled asynchronously.

---

# Profile Picture Storage

Profile pictures are stored in **Cloudflare R2** rather than inside MongoDB.

Before upload, the image is compressed to reduce storage requirements and bandwidth usage.

Current avatar processing uses:

* Width: `150px`
* JPEG quality: `0.6`

The resulting image is uploaded to the Profile Service's R2 bucket and the public URL is stored with the profile.

When an old profile picture is replaced, deletion of the previous object can be handled asynchronously so that the user-facing update does not need to wait for storage cleanup.

---

# User Search

Profile Service provides username-based user search.

The search implementation supports:

* Case-insensitive matching
* Cursor-based pagination
* Configurable page sizes

The search API is used for user discovery and social interaction flows.

---

# Internal APIs

Profile Service exposes internal APIs used by other services for operations that require profile information.

These include profile lookup and bulk profile lookup operations.

### Profile Lookup

Internal consumers can retrieve profile information such as:

* User ID
* Username
* Avatar URL

This allows services to obtain profile information without directly accessing the Profile Service database.

### Bulk Profile Lookup

Multiple users can be resolved in a single request.

Bulk lookup is useful when a service needs profile information for a group of users and avoids making one network request per user.

---

# Profile Counters

Profile Service maintains denormalized counters including:

* Followers
* Following
* Friends
* Posts
* Reels

Counter updates use atomic MongoDB increment operations.

The surrounding operations are designed to avoid unnecessary synchronous work in user-facing requests.

---

# Kafka Profile Events

Profile Service is a **Kafka producer**.

The main asynchronous Kafka workflow is profile-data denormalization.

When profile information that is denormalized elsewhere changes, Profile Service creates events for the affected downstream services.

The current consumers are:

* Post Service
* Reel Service
* Likes Service
* Interaction Service

The consumers maintain their own local copies of the profile information they need, such as username and avatar URL.

This avoids repeatedly calling Profile Service during content and interaction operations.

---

# Transactional Outbox

Profile events are not sent directly to Kafka from the profile update request.

Instead, Profile Service uses a **Transactional Outbox-style workflow**.

The profile operation first creates outbox records in MongoDB.

The outbox contains information such as:

* Aggregate ID
* Aggregate type
* Event type
* Kafka topic
* Event payload
* Event status
* Retry count
* Creation time
* Trace context

The profile denormalization workflow currently creates four outbox entries:

```text id="7xmyjq"
Profile Update
      |
      v
    Outbox
      |
      +---- profile-post-events
      |
      +---- profile-reel-events
      |
      +---- profile-comment-events
      |
      +---- profile-interaction-events
```

A scheduled publisher periodically reads pending outbox records and publishes them to Kafka.

The database record is marked successful after the Kafka publish completes.

---

## Why the Outbox Is Used

Writing the event information to the database before attempting Kafka publication prevents the application from depending on a successful Kafka call at the exact moment a profile update occurs.

If Kafka is temporarily unavailable, the pending outbox record remains available for a later publishing attempt.

This separates the profile update from the Kafka delivery mechanism and provides a persistent record of events waiting to be published.

---

# Outbox Cleanup

Outbox records are temporary event-delivery records rather than permanent application data.

The system uses a **24-hour TTL** for outbox data so that successfully processed records do not accumulate indefinitely.

This keeps the outbox storage bounded while retaining recent event records for the delivery workflow.

---

# Kafka Producer Configuration

The Profile Service uses Kafka with:

```text id="h6k9t1"
bootstrap server: kafka:9092
```

The producer is configured with:

* `acks=all`
* Producer retries
* Kafka idempotence enabled
* Delivery timeout
* JSON serialization

Kafka producer idempotence is enabled so that producer-side retries are handled more safely.

The application also enables Kafka observation so Kafka operations participate in the application's observability pipeline.

---

# Idempotency

Profile creation uses an idempotent create-or-fetch strategy backed by a unique MongoDB index on `userId`.

This prevents concurrent or repeated profile-creation requests from producing duplicate profile documents.

The Kafka/outbox workflow is also designed with retry and repeated-processing scenarios in mind.

Idempotency is treated as a service-level requirement rather than relying solely on successful network delivery.

---

# Security

Authentication and external authorization are handled at the API Gateway.

Profile Service does not independently validate JWT tokens.

The Gateway authenticates the request and propagates the authenticated user's identity using:

```text id="q8b5ko"
X-User-Id
```

The Gateway also adds:

```text id="0c3zcc"
X-Gateway-Secret
```

Profile Service validates this Gateway secret for requests that require Gateway-originated access.

There is also a service-level secret mechanism for trusted internal service communication.

This provides an application-level protection against direct access to protected Profile Service endpoints.

In a production deployment, this type of application-level check would normally be complemented by network-level controls such as private networking, firewall rules, or service-network isolation. The Gateway filter here also makes the intended access boundary demonstrable in the application itself.

---

# Observability

Profile Service participates in the centralized observability stack.

There are two separate observability paths:

```text id="zz8u8r"
Tracing:

Profile Service
      |
      v
OpenTelemetry
      |
      v
OTel Collector
      |
      v
Jaeger
```

```text id="e2aqd7"
Metrics:

Profile Service
      |
      v
Actuator / Micrometer
      |
      v
Prometheus
      |
      v
Grafana
```

---

## Distributed Tracing

OpenTelemetry tracing is enabled with W3C Trace Context propagation.

The service exports traces to:

```text id="q6j4wy"
http://otel-collector:4318/v1/traces
```

The sampling probability is currently set to `1.0`, meaning traces are sampled for all requests in the development environment.

The Gateway and downstream services use the same W3C propagation mechanism, allowing trace context to continue across service boundaries.

The Kafka outbox workflow also preserves trace context when an event is written to the outbox and later published.

This allows asynchronous processing to retain the relationship with the originating operation when the event is published and consumed.

---

# Metrics

Spring Boot Actuator and Micrometer provide service-level metrics.

Prometheus metrics are exposed through:

```text id="w8hjvy"
/actuator/prometheus
```

Standard Spring Boot metrics include application, JVM, process, system, and HTTP-related measurements.

Profile Service also contains a method-level `MetricAspect` that records metrics around service-layer methods.

The custom metrics include:

```text id="jzj7c0"
http.api.latency
http.api.count
```

They are tagged by:

* Controller/service class
* Method
* Success or error status

Latency percentiles are published for:

* p50
* p95
* p99

The custom aspect complements the standard service-level metrics collected by Actuator and exported to Prometheus.

---

# Error Handling

The service uses domain-specific exceptions for application errors.

For example:

* `ProfileNotFound`

Global exception handling provides consistent API responses for service-level failures.

---

# Docker

Profile Service runs as an independent Docker service.

The Compose configuration uses:

```text id="nq9gpf"
profile-service:8081
```

Other containers can reach it using:

```text id="j4vq3q"
http://profile-service:8081
```

The service runs on the shared Docker network used by the backend.

Its configuration is supplied through environment variables, including:

```text id="n1w7mj"
MONGO_URI
SERVICE_SECRET
GATEWAY_SECRET
R2_ACCESS_KEY
R2_SECRET_KEY
R2_ACCOUNT_ID
R2_PUBLIC_BASE_URL
R2_ENDPOINT
```

---

# Design Decisions

### Profile as the Source of Truth

Profile data is owned by Profile Service rather than being directly shared between services.

### Denormalization

Services that frequently display profile information maintain their own copies of fields such as username and avatar URL.

This avoids repeated cross-service lookups on read-heavy paths.

### Kafka for Denormalization

Profile changes are propagated asynchronously through Kafka rather than synchronously calling every dependent service.

### Transactional Outbox

Events are first persisted in the outbox before being published to Kafka, providing a persistent delivery mechanism when Kafka is temporarily unavailable.

### Redis Is Not Used as a Cache

Profile Service does not use Redis as an application cache.

Its distributed coordination responsibilities are handled elsewhere in the platform where required.

### Centralized Authentication

JWT authentication is performed at the Gateway, while Profile Service validates trusted Gateway/service headers rather than implementing a second JWT authentication layer.

### Centralized Observability

Tracing is handled through OpenTelemetry and the OTel Collector, while metrics are exposed through Actuator/Micrometer and collected by Prometheus.

---

# Role in the Platform

Profile Service sits at the center of profile identity data.

```text id="p7tq47"
                  Profile Service
                  /      |       \
                 /       |        \
                v        v         v
             Post      Reel      Likes
             Service   Service   Service
                 \       |        /
                  \      |       /
                   v     v       v
                   Interaction
                     Service
```

Profile information is therefore owned in one place while frequently accessed profile fields are denormalized into the services that need them.

The result is a read-oriented design suitable for social-media workloads, with asynchronous propagation used to keep the copies synchronized.
