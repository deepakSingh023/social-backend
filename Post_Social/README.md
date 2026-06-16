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

The service stores post content and acts as the source of truth for post-related data.

---

## Technology Stack

* Java 17
* Spring Boot
* Spring Data MongoDB
* MongoDB
* OpenFeign
* Cloudflare R2
* AWS S3 SDK
* Spring Async
* Resilience4j
* Micrometer
* Spring AOP

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

When a post is created:

1. Post Service stores the post
2. Feed Service is notified asynchronously
3. Feed entries are generated for followers

When a post is deleted:

1. Post is removed
2. Associated feed entries are removed asynchronously

This keeps feed reads fast because feed generation happens during writes.

---

## Feed APIs

The service exposes internal APIs used by Feed Service.

### Batch Post Fetch

Used when Feed Service already knows a list of post IDs and needs full post data.

### Author Post Fetch

Used when Feed Service needs posts from a specific author during feed generation.

Supports cursor-based pagination.

---

## Denormalization

To reduce expensive cross-service lookups, selected profile information is denormalized into posts.

Stored fields include:

* Username
* Avatar

When a user updates their profile picture:

1. Profile Service sends a denormalization request
2. All matching posts are updated asynchronously

This allows post retrieval without repeatedly calling Profile Service.

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

## Fault Tolerance

Several cross-service operations are protected using Resilience4j.

### Feed Creation

Protected by:

* Retry
* Circuit Breaker
* Fallback logging

This prevents temporary Feed Service outages from affecting post creation.

---

### Feed Deletion

Protected by:

* Retry
* Circuit Breaker
* Fallback logging

This improves reliability during post deletion workflows.

---

### Like Service Failures

Like status retrieval is treated as a non-critical dependency.

If Like Service becomes unavailable:

* Posts are still returned
* Like state defaults to false
* Errors are logged

This ensures feed and profile pages remain available during partial outages.

---

## Observability

The service includes basic observability features implemented using Spring AOP and Micrometer.

### Request Tracing

A unique trace ID is generated for each request and stored in MDC.

This helps correlate logs across request execution.

---

### API Logging

Controller requests are automatically logged with:

* Controller name
* API name
* Execution status
* Latency

This provides visibility into request execution and failures.

---

### Metrics

Micrometer metrics are collected for API endpoints.

Tracked metrics include:

* Request count
* Request latency
* Success rate
* Error rate
* Percentile latency measurements

These metrics can be exported to monitoring platforms such as Prometheus and Grafana.

---

## Error Handling

Custom exceptions are used for business-level failures.

Example:

* PostNotFound

Global exception handlers translate these exceptions into consistent API responses.

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
* Cloudflare R2

### Internal Components

* Media Compression Utilities
* Presigned URL Generator
* Denormalization Service
* Feed Integration Services
* Metrics and Logging Aspects

---

## Design Notes

The service follows a denormalized data model to optimize read performance.

Profile information is embedded into posts to avoid repeated service-to-service calls during feed and profile retrieval.

The platform uses Fan-Out-On-Write for post feeds, prioritizing fast feed reads at the cost of additional work during post creation.

Media uploads are progressively moving toward direct client-to-storage uploads through presigned URLs, reducing backend bandwidth usage and improving scalability.

Cross-service operations are handled asynchronously where possible and protected using retries and circuit breakers to improve resilience during partial system failures.
