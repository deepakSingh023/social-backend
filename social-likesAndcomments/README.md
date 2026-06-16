# Like & Comment Service

## Overview

The Like & Comment Service is responsible for managing engagement across the platform. It handles likes for posts, reels, and comments while also providing support for comments and nested replies.

The service acts as a shared engagement layer used by multiple services. Post Service and Reel Service rely on it for like tracking, while Profile Service communicates with it to keep denormalized user information up to date.

In addition to storing likes and comments, the service updates engagement counters and contributes user interaction signals used by the reel recommendation system.

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

This keeps the engagement system simple while supporting multiple content types.

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

Several services need to know whether content has been liked by a user.

Instead of making individual requests for every post or reel, the service provides a batch liked-status API.

This API is used by:

* Post Service
* Reel Service
* Friend Feed Service

This reduces service-to-service traffic and avoids N+1 query patterns during feed generation.

---

### Interest Signal Generation

When a user likes a reel, the service not only updates engagement counts but also sends a signal to the Reel Interest Service.

This interaction helps build user interest profiles that are later used for personalized reel recommendations.

---

## Internal APIs

The service exposes internal endpoints used by other services.

### Batch Like Status

Used to determine whether a user has liked multiple posts, reels, or comments in a single request.

### Individual Like Status

Used when fetching a single post or reel.

### Avatar Denormalization

Used by Profile Service whenever a user updates their profile picture.

The service updates all existing comments belonging to that user to keep profile information consistent.

---


## Resilience

Several operations are performed asynchronously to keep user requests fast.

Examples include:

* Post like counter updates
* Reel like counter updates
* Interest tracking updates
* Avatar synchronization

Failures in downstream services do not block the original user request.

This approach improves responsiveness and isolates failures between services.

---

## Observability

The service includes tracing, metrics, and structured logging.

### Tracing

A unique trace ID is generated for every request and stored using MDC.

This makes it easier to follow a request through logs.

### Metrics

Micrometer metrics are collected for:

* Request count
* Success count
* Error count
* API latency

Percentiles include:

* P50
* P95
* P99

Metrics are exposed through Spring Boot Actuator.

### Logging

Controller requests are logged with:

* Controller name
* API name
* Request latency
* Success status
* Error information

This helps simplify debugging and performance analysis.

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
* OpenFeign
* Micrometer
* Spring Boot Actuator
* JWT Authentication

---

## Summary

The Like & Comment Service provides the engagement layer of the platform. It centralizes likes, comments, and replies while exposing efficient APIs for feed generation and content retrieval.

By combining denormalization, asynchronous updates, internal service communication, and observability features, the service remains lightweight, scalable, and responsive while supporting engagement-heavy workloads across the platform.
