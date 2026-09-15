# Reel Service

## Overview

The Reel Service manages short-form video content, reel discovery, semantic tagging, and recommendation signals.

It stores reel metadata, manages media uploads, tracks engagement metrics, and generates personalized reel feeds based on user interests and reel popularity.

---

## Responsibilities

* Create and delete reels
* Manage reel video uploads
* Generate personalized reel feeds
* Track reel views
* Calculate reel popularity scores
* Store raw and semantic tags
* Provide reel data for profile pages
* Handle avatar denormalization
* Maintain engagement counters

---

## Key Features

### Reel Creation

The service supports two upload workflows.

#### Backend Upload

* User uploads a video through the API
* Video validation is performed
* Video is compressed before storage
* Media is uploaded to Cloudflare R2
* Reel metadata is saved in MongoDB

#### Frontend Direct Upload

* Client requests a presigned upload URL
* Frontend uploads directly to Cloudflare R2
* Metadata is submitted separately after upload

This reduces backend bandwidth usage and upload processing.

---

### Semantic Tag Resolution

Each reel stores two types of tags.

#### Raw Tags

Tags directly provided by the creator.

```text
gym
workout
fitness
```

#### Semantic Tags

Normalized tags used by the recommendation system.

```text
fitness
```

When a raw tag has no mapping:

* A new mapping entry is created automatically
* The semantic tag remains empty
* The mapping can be added later

---

### Personalized Reel Feed

Feed generation combines:

* Interest-based content
* Popular content
* Recent content

Results are combined, shuffled, deduplicated, and limited to the requested page size.

---

### Popularity Scoring

Popularity is recalculated when engagement changes.

```text
Popularity = (Views + Likes × 5) / Hours^1.5
```

The formula gives additional weight to likes and applies time decay to older content.

---

### View Tracking

Every reel view:

* Increments the view counter
* Recalculates popularity
* Returns the reel's semantic tags

The semantic tags are used by the View Service to update user interests.

---

### Interest Integration

The Reel Service works with the Interest and View services to support recommendation signals generated from reel engagement.

---

### Profile Integration

During reel creation, profile information is retrieved from Profile Service.

Stored denormalized data includes:

* Username
* Avatar URL

This avoids repeated profile lookups when reels are retrieved.

---

### Avatar Denormalization

Profile avatar changes are propagated asynchronously through Kafka.

Topic:

```text
profile-reel-events
```

When an event is received:

1. Reel Service checks Redis using the event ID
2. Duplicate events are ignored
3. Reels belonging to the user are updated
4. The event ID is stored in Redis for 24 hours

The avatar update is naturally idempotent because the stored value is overwritten, while Redis prevents unnecessary repeated MongoDB writes from duplicate Kafka deliveries.

---

### Engagement Counters

The service maintains denormalized counters for:

* Likes
* Comments
* Views
* Popularity Score

Counters are updated by the relevant services.

---

## Internal APIs

The service exposes internal APIs used by other services.

### Feed Retrieval

Used by Reel Fetch Service to retrieve personalized reels.

### Avatar Denormalization

Profile updates are propagated through Kafka and applied to existing reels.

### View Updates

Used to update:

* View counts
* Popularity scores
* Semantic tags for interest processing

### Engagement Counter Updates

Used by Likes & Comments Service to update:

* Like counts
* Comment counts

---

## Storage

### `reels` Collection

Stores:

* Reel metadata
* Video URLs
* User information
* Tags
* Engagement statistics
* Popularity scores

### `tag_mappings` Collection

Stores mappings between:

* Raw creator tags
* Semantic recommendation tags

---

## Cloudflare R2 Integration

Cloudflare R2 is used for:

* Reel video storage
* Presigned uploads
* Media delivery

Videos are stored separately from application data.

---

## Service Communication

The Reel Service communicates with:

* Profile Service
* Likes & Comments Service
* Interest Service

It uses synchronous service-to-service communication where an immediate response is required and Kafka for profile avatar denormalization.

---

## Reliability

Resilience4j Retry and Circuit Breaker are used for relevant cross-service operations.

This provides retry handling for temporary failures and prevents repeated calls to unavailable dependencies.

---

## Security

JWT authentication and browser CORS are handled by the API Gateway.

The Reel Service does not independently validate JWTs or handle CORS.

The service uses:

* `GatewayHeaderFilter` to validate requests coming through the Gateway
* `InternalFilter` for internal service requests

These provide application-level protection at the service boundary.

---

## Observability

### Distributed Tracing

Tracing is handled using OpenTelemetry.

The service uses W3C trace context propagation and exports traces to the OpenTelemetry Collector, which forwards them to Jaeger.

### Metrics

Spring Boot Actuator and Micrometer provide the main service metrics.

Metrics are exposed through the Prometheus endpoint and scraped by Prometheus for visualization in Grafana.

A custom Spring AOP aspect additionally records service-method metrics:

* `http.api.latency`
* `http.api.count`

These track method execution latency and success/error status.

---

## Tech Stack

* Java 17
* Spring Boot
* Spring Security
* MongoDB
* Redis
* Apache Kafka
* Spring Kafka
* Cloudflare R2
* AWS S3 SDK
* OpenFeign
* Resilience4j
* Micrometer
* Spring Boot Actuator
* OpenTelemetry
* Spring AOP

---

## Limitations

The semantic tagging system is currently manual.

When new raw tags appear:

1. The raw tag is stored automatically.
2. Semantic mappings must be added manually.

A future improvement would be automated tag classification to reduce manual maintenance.

---

## Summary

The Reel Service manages reel content, media storage, engagement data, semantic tags, popularity scoring, and personalized feed generation.

It uses denormalized profile data, Resilience4j for relevant service calls, and Kafka-based avatar propagation to keep reel data synchronized across the platform.
