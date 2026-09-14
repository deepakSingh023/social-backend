# Feed Service

## Overview

The Feed Service generates and serves the home feed for users.

It follows a **Fan-Out-on-Write** architecture. Feed entries are created when posts are created or when relationships change, allowing feed retrieval to use precomputed feed references instead of calculating relationships on every read.

The service maintains feed entries and coordinates with the Post, Interaction, and Likes services.

---

## Responsibilities

* Feed generation
* Feed retrieval
* Feed cleanup
* Post-based feed creation
* Interaction-based feed creation
* Post deletion propagation
* Relationship deletion propagation
* Feed pagination
* Feed enrichment with like status

---

## Architecture

Feed generation is event-driven through Kafka.

```text
Post Service
     |
     | post create/delete event
     v
Feed Service
     |
     v
Fan-Out-on-Write
     |
     v
Feed Entries
```

Relationship changes follow the same pattern:

```text
Interaction Service
     |
     | relationship event
     v
Feed Service
     |
     v
Fan-Out-on-Write
```

Redis is used for event idempotency. Processed Kafka event IDs are stored for 24 hours to prevent duplicate events from repeating feed operations.

---

## Feed Creation

### Post Creation

When a post is created:

1. Post Service stores the post
2. Post Service creates an Outbox event
3. The event is published to Kafka
4. Feed Service consumes the event
5. Feed Service retrieves the author's recipients from Interaction Service
6. Feed entries are created for those recipients

Kafka topic:

```text
post-feed-events-create
```

### Relationship Creation

When a friendship or follow relationship is created:

1. Interaction Service publishes an event
2. Feed Service consumes the event
3. Existing posts from the author are retrieved from Post Service
4. Feed entries are created for the new recipient

Kafka topic:

```text
create-feed-interaction
```

This allows newly connected users to receive existing posts from the relevant author.

---

## Feed Retrieval

Feed retrieval uses the stored feed references.

The service:

1. Fetches feed entries
2. Retrieves the referenced posts from Post Service
3. Retrieves the user's like status from Likes Service
4. Builds the final feed response

```text
Feed Entries
     |
     v
Post Service
     |
     v
Post Data
     |
     v
Likes Service
     |
     v
Feed Response
```

The Feed Service stores references rather than complete post documents.

---

## Feed Cleanup

### Post Deletion

Post deletion is propagated through Kafka.

Topic:

```text
post-feed-events-delete
```

The Feed Service consumes the event and removes feed entries referencing the deleted post.

### Relationship Deletion

Relationship deletion is also propagated through Kafka.

Topic:

```text
delete-feed-interaction
```

The Feed Service removes feed entries belonging to the affected author-recipient relationship.

---

## Kafka Event Idempotency

Kafka consumers use Redis to prevent duplicate processing.

For each event:

1. Redis is checked using the event ID
2. Already processed events are ignored
3. The feed operation is executed
4. The event ID is stored in Redis for 24 hours

This handles Kafka's at-least-once delivery behavior and prevents unnecessary repeated feed operations.

---

## Batch Processing

Feed generation uses batched service calls.

### Interaction Fetching

Recipients are fetched using cursor pagination.

Batch size:

```text
100 users per request
```

### Historical Post Fetching

When a relationship is created, posts are fetched from Post Service in batches.

Batch size:

```text
100 posts per request
```

---

## Cursor Pagination

Feed retrieval uses cursor pagination.

The cursor contains:

```text
createdAt
feedId
```

The next page is selected using the timestamp and ID ordering.

This provides stable ordering and avoids the performance problems associated with large offset-based queries.

---

## Feed Enrichment

Feed documents contain lightweight references.

### Post Service

Provides:

* Caption
* Images
* Videos
* Tags
* Author information
* Counts

### Likes Service

Provides:

* Current user's like status

---

## Reliability

Resilience4j Retry and Circuit Breaker are used for the important synchronous data-fetch operations involved in feed generation.

They are applied to:

* Interaction Service calls
* Post Service calls

If Interaction Service fails, the fallback returns an empty recipient list.

If Post Service fails, the fallback returns an empty post list.

This allows feed generation to terminate safely when a downstream service is temporarily unavailable.

---

## Security

JWT authentication and browser CORS are handled by the API Gateway.

The Feed Service does not perform JWT validation or CORS handling.

The service uses:

* `GatewayHeaderFilter` to validate the Gateway secret
* `InternalFilter` for internal service protection

These provide application-level protection against direct downstream access.

---

## Observability

### Distributed Tracing

Tracing is handled using OpenTelemetry.

The service uses W3C trace context propagation and exports traces to the OpenTelemetry Collector, which forwards them to Jaeger.

Kafka listener observation is enabled for asynchronous event processing.

### Metrics

Spring Boot Actuator and Micrometer provide the main service metrics.

Metrics are exposed through the Prometheus endpoint and collected by Prometheus for visualization in Grafana.

A custom Spring AOP aspect additionally records method-level metrics:

* `http.api.latency`
* `http.api.count`

These include the method, service class, status, and latency percentiles.

---

## Technology Stack

* Java 17
* Spring Boot
* Spring Data MongoDB
* MongoDB
* Spring Security
* OpenFeign
* Apache Kafka
* Spring Kafka
* Redis
* Resilience4j
* Spring AOP
* Micrometer
* Spring Boot Actuator
* OpenTelemetry
* Async Processing

---

## Design Trade-Offs

### Fan-Out-on-Write

Feed entries are generated during writes instead of during feed reads.

Advantages:

* Simple feed queries
* Predictable read processing
* No relationship calculation during every feed request

Trade-offs:

* More work during post creation and relationship changes
* Additional feed storage

### Feed References

The service stores post references instead of complete post data.

This keeps feed documents small but requires calls to Post and Likes services when building the final response.

---

## Summary

The Feed Service implements the platform's Fan-Out-on-Write feed system.

Post and relationship changes are propagated through Kafka, while Redis provides event idempotency. Feed generation retrieves recipients and historical posts in batches, and feed reads enrich stored post references with data from Post and Likes services.

Resilience4j protects the important synchronous data-fetch operations, while OpenTelemetry and Actuator/Micrometer provide tracing and metrics.
