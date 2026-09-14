# Like & Comment Service

## Overview

The Like & Comment Service manages engagement across the platform.

It handles likes for posts, reels, and comments, as well as comments and nested replies.

The service is used by Post Service, Reel Service, Profile Service, and other services for engagement-related operations.

---

## Responsibilities

* Create and remove likes
* Store comments and replies
* Track engagement counts
* Provide liked-status information
* Maintain comment reply counts
* Update post and reel like counters
* Synchronize profile avatar updates
* Generate reel interest signals from likes

---

## Features

### Likes

* Like posts
* Unlike posts
* Like reels
* Unlike reels
* Like comments
* Unlike comments
* Check if a user liked a specific target
* Batch liked-status lookup
* Retrieve like counts

### Comments

* Create comments
* Create replies
* Delete comments
* Fetch paginated comments
* Fetch paginated replies
* Ownership detection
* Include liked status in responses

### Denormalization

* Post like counter updates
* Reel like counter updates
* Comment like counter updates
* Comment reply counter updates
* Avatar synchronization from Profile Service

---

## Architecture Notes

### Unified Like Model

Likes for posts, reels, and comments are stored in a single collection.

```text
Like
 ├─ targetId
 ├─ targetType
 ├─ userId
 └─ createdAt
```

Supported target types:

* POST
* REEL
* COMMENT

---

### Comment Hierarchy

Comments support nested replies through the `parentCommentId` field.

```text
Post
 ├─ Comment
 │   ├─ Reply
 │   ├─ Reply
 │   └─ Reply
 └─ Comment
```

Top-level comments have a null parent ID.

The service maintains denormalized reply counts to avoid expensive aggregation queries.

---

### Feed Optimization

The service provides batch liked-status APIs so other services can check multiple posts, reels, or comments without making individual requests.

Used by:

* Post Service
* Reel Service
* Feed Service

This reduces service-to-service traffic and avoids N+1 request patterns.

---

### Interest Signal Generation

When a user likes a reel, the service also generates an interaction signal used by the reel recommendation system.

---

## Profile Avatar Updates

The service consumes profile denormalization events from Profile Service through Kafka.

Topic:

```text
profile-comment-events
```

When a user's avatar changes:

1. Profile Service publishes the profile update event
2. Like & Comment Service consumes the event
3. Redis is checked using the event ID
4. Duplicate events are ignored
5. Existing comments belonging to the user are updated
6. The event ID is stored in Redis for 24 hours

Redis idempotency prevents duplicate Kafka deliveries from causing unnecessary MongoDB writes.

---

## Kafka

The service acts as a Kafka consumer for profile denormalization events.

Kafka is used for asynchronous propagation of profile avatar changes rather than requiring Profile Service to synchronously update every comment.

The service uses Spring Kafka with W3C trace context propagation for asynchronous processing.

---

## Internal APIs

The service exposes internal endpoints used by other services.

### Batch Like Status

Used to determine whether a user has liked multiple posts, reels, or comments in a single request.

### Individual Like Status

Used when fetching a single post or reel.

### Avatar Denormalization

Updates comments when a user's profile avatar changes.

---

## Security

JWT authentication and browser CORS are handled by the API Gateway.

The Like & Comment Service does not independently validate JWTs or handle browser CORS.

The service uses an application-level `GatewayHeaderFilter` to verify the gateway secret on requests coming through the Gateway.

It also contains an `InternalFilter` for service-level internal protection.

These filters provide application-level protection against direct downstream access. Network-level isolation can additionally be applied through private service networking, firewall rules, or other deployment-level controls.

---

## Resilience

Several operations are performed asynchronously so that non-critical downstream work does not block the original request.

Examples include:

* Post like counter updates
* Reel like counter updates
* Interest tracking updates
* Avatar synchronization

This keeps user-facing operations independent from non-critical downstream processing.

---

## Observability

### Distributed Tracing

Tracing is handled using OpenTelemetry.

The service uses W3C trace context propagation and exports traces to the OpenTelemetry Collector, which forwards them to Jaeger.

Kafka listener observation is also enabled so asynchronous Kafka processing participates in the tracing pipeline.

---

### Metrics

Spring Boot Actuator and Micrometer provide the service metrics.

Metrics are exposed through the Prometheus endpoint and collected by Prometheus for visualization in Grafana.

Metrics include:

* Request count
* Request latency
* JVM metrics
* Process/system metrics
* Application metrics

---

## Data Model

### Like

```text
id
targetId
targetType
userId
createdAt
```

### Comment

```text
id
postId
parentCommentId
userId
username
userAvatar
content
likesCount
repliesCount
createdAt
updatedAt
```

---

## Tech Stack

* Java 17
* Spring Boot
* Spring Security
* MongoDB
* Redis
* Apache Kafka
* Spring Kafka
* OpenFeign
* Micrometer
* Spring Boot Actuator
* OpenTelemetry

---

## Summary

The Like & Comment Service provides the engagement layer for posts, reels, and comments.

It combines a unified like model, nested comments, denormalized counters, batch APIs, asynchronous Kafka-based profile updates, and Redis-based consumer idempotency to support the rest of the platform.
