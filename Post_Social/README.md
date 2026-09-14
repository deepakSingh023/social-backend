# Post Service

## Overview

The Post Service is responsible for managing user posts within the platform.

It handles:

* Post creation
* Media uploads
* Post deletion
* User post retrieval
* Feed integration
* Like and comment count denormalization
* Avatar denormalization
* Media storage in Cloudflare R2
* Kafka-based feed events
* Kafka-based profile update events

The service stores post content and acts as the source of truth for post-related data.

---

## Technology Stack

* Java 17
* Spring Boot
* Spring Data MongoDB
* MongoDB
* OpenFeign
* Apache Kafka
* Spring Kafka
* Redis
* Cloudflare R2
* AWS S3 SDK
* Spring Async
* Micrometer
* Spring AOP
* Spring Boot Actuator
* OpenTelemetry

---

## Responsibilities

### Post Creation

The service supports two post creation flows.

#### Legacy Upload Flow

The backend receives media files directly.

During creation:

1. Images are validated and compressed
2. Videos are validated and compressed
3. Media is uploaded to Cloudflare R2
4. Profile data is fetched from Profile Service
5. Post is stored in MongoDB
6. Feed creation is triggered asynchronously
7. User post count is updated

---

#### Presigned Upload Flow

A newer upload flow designed to reduce backend load.

Process:

1. Client requests a presigned upload URL
2. Client uploads media directly to Cloudflare R2
3. Client submits media URLs and metadata
4. Post is persisted without passing media through the backend

Benefits:

* Reduced server bandwidth usage
* Faster uploads
* Better scalability
* Lower backend resource consumption

---

## Media Handling

### Images

Images are:

* Validated
* Compressed before upload
* Uploaded asynchronously

Maximum allowed:

* 7 images per post

---

### Videos

Videos are:

* Validated
* Duration checked
* Compressed before upload

Current limit:

* Maximum 40 seconds

Video duration is extracted using Apache Tika metadata parsing.

---

## Post Retrieval

### User Posts

The service supports fetching posts for:

* Profile owners
* Other users viewing a profile

Returned data includes:

* Post details
* Like status
* Ownership status

Ownership information allows the frontend to determine whether actions such as deletion should be available.

---

### Individual Post

Fetches a single post and returns:

* Post metadata
* Like state
* Ownership information

If the Like Service is unavailable, the request still succeeds and defaults the like state to false.

---

## Feed Integration

The platform uses a Fan-Out-On-Write feed architecture.

When a post is created, the Post Service does not directly call Feed Service to create the feed entries. Instead, it produces an asynchronous Kafka event.

The flow is:

1. Post Service stores the post
2. An asynchronous feed operation creates an Outbox record
3. A scheduled Kafka publisher reads pending Outbox records
4. The event is published to Kafka
5. Feed Service consumes the event
6. Feed entries are generated for the relevant friends/followers

This provides the same logical operation as the previous synchronous Feed Service call, but communication between the services is asynchronous.

When a post is deleted, the same pattern is used:

1. Post is removed
2. A delete event is stored in the Outbox
3. The scheduled publisher sends the event to Kafka
4. Feed Service consumes the event
5. Associated feed entries are removed

---

## Kafka Integration

The Post Service acts as both a Kafka producer and consumer.

### Feed Creation Events

Post creation produces events for Feed Service through Kafka.

Topic:

```text
post-feed-events-create
```

The event contains the information required by Feed Service to generate feed entries.

### Feed Deletion Events

Post deletion produces events for Feed Service.

Topic:

```text
post-feed-events-delete
```

Feed Service consumes these events to remove feed entries associated with deleted posts.

---

## Outbox Pattern

Kafka events are persisted using an Outbox pattern.

Instead of publishing directly to Kafka during the main operation:

1. The service creates an Outbox record in MongoDB
2. The record is marked as `PENDING`
3. A scheduled publisher checks for pending events
4. The event is published to Kafka
5. The Outbox record is marked as successful after successful publishing
6. Failed events remain available for another publishing attempt

The publisher processes pending events in batches and restores the trace context stored with the event before publishing.

This prevents a temporary Kafka failure from causing the original database operation to lose its associated event.

Outbox records are also cleaned up using a 24-hour TTL to prevent indefinite accumulation.

---

## Profile Event Listener

The Post Service consumes profile denormalization events from Profile Service.

Topic:

```text
profile-post-events
```

These events are generated when profile information relevant to posts changes, such as a user's avatar.

When an avatar update event is received:

1. Post Service receives the Kafka event
2. Redis is checked using the event ID
3. Already-processed events are ignored
4. The avatar is updated for the user's posts
5. The event ID is stored in Redis
6. The Redis idempotency key expires after 24 hours

The avatar update itself is naturally idempotent because the stored value is overwritten with the latest avatar. Redis idempotency additionally prevents unnecessary MongoDB writes when Kafka delivers a duplicate event.

---

## Feed APIs

The service exposes internal APIs used by Feed Service.

### Batch Post Fetch

Used when Feed Service already knows a list of post IDs and needs full post data.

### Author Post Fetch

Used when Feed Service needs posts from a specific author during feed generation.

Supports cursor-based pagination.

These APIs are used for synchronous post retrieval, while feed creation and deletion are handled asynchronously through Kafka.

---

## Denormalization

To reduce expensive cross-service lookups, selected profile information is denormalized into posts.

Stored fields include:

* Username
* Avatar

Profile updates are propagated asynchronously through Kafka.

When a user updates their profile picture:

1. Profile Service publishes a profile denormalization event
2. Post Service consumes the event
3. Redis idempotency prevents duplicate processing
4. Matching posts are updated asynchronously

This allows post retrieval without repeatedly calling Profile Service for avatar information.

---

## Like and Comment Counters

The service stores:

* Like count
* Comment count

Counts are updated through internal APIs using atomic MongoDB increment operations.

This avoids expensive aggregation queries during reads.

---

## Storage Layer

Media files are stored in Cloudflare R2.

Supported operations:

* Upload media
* Delete media
* Generate presigned upload URLs

The service stores only media URLs inside MongoDB.

Actual file content remains in object storage.

---

## Security

Authentication and browser-level CORS are handled by the API Gateway.

The Post Service does not independently validate JWTs or provide browser CORS handling.

The service includes application-level filters for protecting its internal boundary:

### Gateway Header Filter

Requests routed through the API Gateway contain the expected `X-Gateway-Secret`.

The filter rejects requests that do not contain the correct gateway secret.

### Internal Filter

An additional service-level filter is used for internal service protection using the configured service secret.

These filters demonstrate application-level protection against direct downstream access.

In a deployment requiring stronger isolation, network-level controls such as private networking, firewall rules, or service-network restrictions should be used alongside application-level checks.

---

## Observability

The service uses Spring Boot Actuator, Micrometer, OpenTelemetry, Prometheus, Grafana, and Jaeger for observability.

### Distributed Tracing

Tracing is handled through OpenTelemetry.

The service:

* Uses W3C trace context propagation
* Samples traces according to the configured sampling probability
* Exports traces to the OpenTelemetry Collector

The OpenTelemetry Collector forwards traces to Jaeger for trace visualization.

Trace context is also stored with Outbox events so asynchronous Kafka processing can continue the original trace.

---

### Metrics

Spring Boot Actuator and Micrometer provide the main application and JVM metrics.

Metrics are exposed through the Actuator Prometheus endpoint and collected by Prometheus.

Grafana can then be used to visualize the collected metrics.

The service exposes:

* Health metrics
* JVM metrics
* Process/system metrics
* HTTP/application metrics
* Custom application metrics

---

### Custom Method-Level Metrics

A Spring AOP metric aspect additionally measures service-layer methods.

The custom metrics include:

* `http.api.latency`
* `http.api.count`

The latency metric records:

* Controller/class
* Method
* Success or error status
* p50 latency
* p95 latency
* p99 latency
* Histogram data

These are additional method-level metrics. Distributed tracing and the main service metrics are handled separately through Spring Boot/OpenTelemetry/Micrometer.

---

## Error Handling

Custom exceptions are used for business-level failures.

Example:

* `PostNotFound`

Global exception handlers translate these exceptions into consistent API responses.

Non-critical dependencies can also be handled without failing the complete post retrieval operation.

For example, if Like Service is unavailable, post retrieval can still succeed with the like state defaulting to false.

---

## Data Model

Each post contains:

* Post ID
* User ID
* Username
* Avatar
* Images
* Video
* Caption
* Song metadata
* Tags
* Privacy flag
* Creation timestamp
* Like count
* Comment count

---

## Service Dependencies

### Outgoing Dependencies

* Profile Service
* Feed Service
* Like Service
* Interaction Service
* Cloudflare R2
* MongoDB
* Redis
* Kafka

### Communication

The service uses different communication patterns depending on the operation.

**Synchronous:**

* OpenFeign for required service-to-service lookups
* Internal REST APIs for post retrieval and counter operations

**Asynchronous:**

* Kafka for feed creation and deletion events
* Kafka for profile/avatar denormalization events
* Spring Async for background processing before events are persisted/published

---

## Design Notes

The service follows a denormalized data model to optimize read performance.

Profile information such as username and avatar is embedded into posts to avoid repeated service-to-service calls during feed and profile retrieval.

The platform uses Fan-Out-On-Write for post feeds, prioritizing fast feed reads at the cost of additional work during post creation.

Feed creation and deletion use Kafka with the Outbox pattern instead of relying on a synchronous Feign call to Feed Service. This keeps the database operation independent from the immediate availability of Feed Service or Kafka.

Profile updates are propagated asynchronously through Kafka so changes such as avatar updates can be applied to denormalized post data without requiring synchronous calls to every post.

Media uploads are progressively moving toward direct client-to-storage uploads through presigned URLs, reducing backend bandwidth usage and improving scalability.

Redis is used for Kafka consumer idempotency rather than as a general-purpose application cache.

The service uses synchronous communication where an immediate response is required and asynchronous events where eventual consistency is acceptable.
