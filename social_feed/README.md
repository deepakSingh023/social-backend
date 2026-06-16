# Feed Service

## Overview

The Feed Service is responsible for generating and serving the home feed for users across the platform.

The service follows a **Fan-Out-on-Write architecture**, where feed entries are precomputed and stored whenever posts are created or new relationships are established. This allows feed retrieval to remain extremely fast because the expensive relationship calculations occur during writes instead of reads.

The service acts as the bridge between the Post Service and the Interaction Service and is responsible for maintaining personalized feed entries for every user.

---

## Responsibilities

The service is responsible for:

* Feed generation
* Feed distribution
* Feed retrieval
* Feed cleanup
* Interaction-based feed creation
* Post deletion propagation
* Relationship deletion propagation
* Feed pagination
* Feed enrichment with like status
* Cross-service feed synchronization

---

## Architecture

The service follows a Fan-Out-on-Write model.

Instead of generating feeds dynamically whenever a user opens the application, feed entries are created ahead of time.

This shifts computational complexity from read operations to write operations.

Benefits include:

* Fast feed retrieval
* Predictable read latency
* Reduced database joins
* Simplified feed queries
* Better scalability for read-heavy workloads

---

## Data Model

### Feed

Represents a single post visible inside a user's feed.

Stores:

* Feed owner id
* Author id
* Post id
* Creation timestamp

Example:

User A creates a post.

User B follows User A.

Feed Entry:

```text
feedOwnerId = UserB
authorId = UserA
postId = Post123
```

When User B opens their feed, the service simply fetches feed entries associated with their user id.

---

## Feed Creation Flow

### Post Creation

When a new post is created:

1. Post Service creates a post
2. Post Service notifies Feed Service
3. Feed Service requests all interaction recipients from Interaction Service
4. Feed entries are generated for every recipient
5. Feed entries are stored in MongoDB

Workflow:

```text
Post Created
      |
      v
Feed Service
      |
      v
Interaction Service
      |
      v
Recipient Users
      |
      v
Feed Entries Created
```

This ensures future feed reads require no relationship calculations.

---

## Interaction-Based Feed Creation

A second feed generation path exists.

When a new relationship is created:

* Friend accepted
* User followed
* Follow request accepted

the Interaction Service notifies the Feed Service.

The Feed Service then:

1. Requests historical posts from the author
2. Batch fetches those posts
3. Creates feed entries for the new recipient

Example:

```text
User A creates 100 posts

User B follows User A

Feed Service fetches:
- Post1
- Post2
- Post3
...
- Post100

Feed entries are generated for User B
```

This ensures newly connected users can immediately access existing content.

---

## Feed Retrieval

The service exposes APIs used by the client home page.

Feed retrieval process:

1. Fetch feed entries
2. Resolve post ids
3. Request full post metadata from Post Service
4. Request like status from Likes Service
5. Construct final feed response

Workflow:

```text
Feed Documents
      |
      v
Post Service
      |
      v
Full Post Data
      |
      v
Likes Service
      |
      v
Like Status
      |
      v
Final Feed Response
```

The feed service never stores complete post content.

Instead it stores lightweight references and enriches them during reads.

---

## Feed Cleanup

### Post Deletion

When a post is deleted:

1. Post Service notifies Feed Service
2. Feed Service removes all feed entries referencing that post

This prevents orphaned feed records.

---

### Interaction Deletion

When a friendship or follow relationship is removed:

1. Interaction Service notifies Feed Service
2. Feed Service removes all feed entries associated with that author-recipient relationship

Example:

```text
User B unfollows User A

Delete:

feedOwnerId = UserB
authorId = UserA
```

All content from User A disappears from User B's feed.

---

## Batch Processing

The service performs feed generation in batches.

### Interaction Fetching

Recipients are fetched from Interaction Service using cursor pagination.

Batch size:

```text
100 users per request
```

This prevents large relationship graphs from exhausting memory.

---

### Historical Post Fetching

When new interactions are created, posts are fetched from Post Service in batches.

Batch size:

```text
100 posts per request
```

This allows the service to efficiently process users with large content histories.

---

## Cursor Pagination

Feed retrieval uses cursor pagination.

Cursor consists of:

```text
createdAt
feedId
```

Pagination query:

```text
(createdAt < cursorCreatedAt)
OR
(createdAt == cursorCreatedAt
 AND id < cursorId)
```

Benefits:

* Stable ordering
* Infinite scrolling support
* No offset performance degradation
* Scales efficiently for large feeds

---

## Feed Enrichment

Feed entries only store references.

Before returning data:

### Post Service

Used to retrieve:

* Caption
* Images
* Videos
* Tags
* Author information
* Counts

### Likes Service

Used to retrieve:

* Current user like status

This allows feed documents to remain lightweight while still providing rich responses.

---

## External Service Integrations

### Interaction Service

Used for:

* Recipient discovery
* Relationship-based feed generation

Operations:

* Batch interaction retrieval

---

### Post Service

Used for:

* Feed post retrieval
* Historical post retrieval

Operations:

* Fetch post metadata
* Fetch author posts

---

### Likes Service

Used for:

* Like status enrichment

Operations:

* Bulk like lookup

---

## Reliability Features

Cross-service communication is protected using Resilience4j.

Features include:

* Retry
* Circuit Breaker
* Fallback methods

Applied to:

* Interaction Service calls
* Post Service calls

---

### Interaction Fetch Fallback

If Interaction Service becomes unavailable:

```text
Return empty recipient list
```

Feed generation safely terminates.

---

### Post Fetch Fallback

If Post Service becomes unavailable:

```text
Return empty post list
```

Interaction feed generation safely terminates.

---

## Asynchronous Processing

Feed creation operations execute asynchronously.

Asynchronous tasks include:

* Feed generation after post creation
* Feed generation after relationship creation
* Feed cleanup after post deletion
* Feed cleanup after interaction deletion

This prevents expensive feed operations from impacting API response times.

---

## Scalability Characteristics

### Read Path

Feed reads are extremely lightweight.

Operations:

1. Query feed entries
2. Resolve post data
3. Return results

Complexity:

```text
O(page_size)
```

---

### Write Path

Feed writes are more expensive.

Operations:

1. Discover recipients
2. Generate feed entries
3. Persist feed records

Complexity:

```text
O(number_of_recipients)
```

This trade-off is intentional because social platforms typically experience significantly more reads than writes.

---

## Observability

The service includes custom observability components.

### Request Tracing

Every request receives a unique trace identifier.

Features:

* Distributed tracing
* Log correlation
* Easier debugging

---

### Structured Logging

Controller requests are automatically logged through AOP.

Captured information:

* Controller
* API
* Status
* Latency

---

### Metrics

Micrometer metrics are collected for all APIs.

Metrics include:

* Request count
* Success count
* Error count
* API latency
* P50 latency
* P95 latency
* P99 latency

Metrics are exposed through Spring Boot Actuator.

---

## Technology Stack

* Java 17
* Spring Boot
* Spring Data MongoDB
* MongoDB
* Spring Security
* OpenFeign
* Resilience4j
* Spring AOP
* Micrometer
* Spring Actuator
* Async Processing
* MDC Tracing

---

## Design Trade-Offs

### Fan-Out-on-Write

Advantages:

* Extremely fast reads
* Simple feed queries
* Predictable latency

Trade-offs:

* More expensive writes
* Additional storage requirements
* Feed duplication across users

The platform prioritizes read performance because feed consumption occurs significantly more frequently than content creation.

---

### Feed References Instead of Full Posts

The service stores only references rather than full post content.

Advantages:

* Smaller feed documents
* Lower storage usage
* Easier post updates

Trade-offs:

* Additional service calls during reads
* Dependency on Post Service availability

---

## Summary

The Feed Service is the content distribution layer of the platform. By implementing a Fan-Out-on-Write architecture, the service precomputes personalized feeds whenever posts are created or relationships change.

This design enables extremely fast feed retrieval while supporting scalable content distribution, relationship-driven visibility, asynchronous processing, fault-tolerant service communication, cursor pagination, structured observability, and efficient integration with the Post, Likes, and Interaction services.
