# Reel Service

## Overview

The Reel Service manages short-form video content, reel discovery, semantic tagging, and recommendation signals. It stores reel metadata, manages media uploads, tracks engagement metrics, and generates personalized reel feeds based on user interests and reel popularity.

The service acts as the content source for the recommendation system and works closely with the Interest Service to improve feed relevance over time.

---

## Responsibilities

- Create and delete reels
- Manage reel video uploads
- Generate personalized reel feeds
- Track reel views
- Calculate reel popularity scores
- Store raw and semantic tags
- Provide reel data for profile pages
- Handle avatar denormalization
- Maintain engagement counters

---

## Key Features

### Reel Creation

The service supports two upload workflows:

#### Backend Upload

- User uploads a video through the API
- Video validation is performed
- Video is compressed before storage
- Media is uploaded to Cloudflare R2
- Reel metadata is saved in MongoDB

#### Frontend Direct Upload

- Client requests a presigned upload URL
- Frontend uploads directly to Cloudflare R2
- Metadata is submitted separately after upload

This approach reduces backend bandwidth usage and improves upload performance.

---

### Semantic Tag Resolution

Each reel stores two types of tags:

#### Raw Tags

Tags directly provided by the creator.

Example:

```text
gym
workout
fitness
```

#### Semantic Tags

Normalized tags used by the recommendation system.

Example:

```text
fitness
```

When a raw tag has no mapping:

- A new mapping entry is created automatically
- The semantic tag remains empty
- Semantic mapping can be added later

This allows new tags to enter the system without breaking recommendation functionality.

---

### Personalized Reel Feed

Feed generation combines multiple content sources:

#### Interest-Based Content

Reels matching the user's strongest semantic interests.

#### Popular Content

Reels ranked by popularity score.

#### Recent Content

Recently uploaded reels to keep the feed fresh.

Results are:

- Combined
- Shuffled
- Deduplicated
- Limited to the requested page size

---

### Popularity Scoring

Popularity is recalculated whenever engagement changes.

Formula:

```text
Popularity = (Views + Likes × 5) / Hours^1.5
```

Characteristics:

- Rewards engagement
- Gives more weight to likes than views
- Applies time decay
- Prevents old content from permanently dominating recommendations

---

### View Tracking

Every reel view:

- Increments the view counter
- Recalculates popularity score
- Returns reel semantic tags

The returned semantic tags are used by the Interest Service to update user interests.

---

### Interest Integration

The service works closely with the Interest Service.

User actions such as:

- Viewing reels
- Liking reels

are translated into interest signals.

These signals help build user preference profiles and improve future feed recommendations.

---

### Profile Integration

During reel creation, the service retrieves profile information from the Profile Service.

Stored denormalized data includes:

- Username
- Avatar URL

This reduces repeated profile lookups during feed generation.

---

### Avatar Denormalization

When a user updates their profile picture:

1. Profile Service sends a denormalization request.
2. All reels belonging to that user update their stored avatar.

This ensures profile updates are reflected across existing content.

---

### Engagement Counters

The service maintains denormalized counters for:

- Likes
- Comments
- Views
- Popularity Score

Counters are updated asynchronously by other services.

---

## Internal APIs

The service exposes internal APIs used by other services.

### Feed Retrieval

Used by the Reel Feed Service to fetch personalized reels.

### Avatar Denormalization

Used by the Profile Service when a user updates their profile picture.

### View Updates

Used to update:

- View counts
- Popularity scores
- Interest signals

### Engagement Counter Updates

Used by the Likes & Comments Service to update:

- Like counts
- Comment counts

without performing expensive aggregation queries.

---

## Storage

### reels Collection

Stores:

- Reel metadata
- Video URLs
- User information
- Tags
- Engagement statistics
- Popularity scores

### tag_mappings Collection

Stores mappings between:

- Raw creator tags
- Semantic recommendation tags

---

## Cloudflare R2 Integration

Cloudflare R2 is used for:

- Reel video storage
- Presigned uploads
- Media delivery

Videos are stored separately from application data to improve scalability.

---

## Service Communication

The Reel Service communicates with:

- Profile Service
- Likes & Comments Service
- Interest Service

Profile information is denormalized to reduce cross-service requests during feed generation.

---

## Observability

The service includes observability features for monitoring and debugging.

### Tracing

Each request receives a unique trace ID using MDC.

### Metrics

Micrometer metrics are collected for:

- Request count
- Request latency
- Success rate
- Error rate

Latency percentiles:

- P50
- P95
- P99

### Logging

Structured logs include:

- Controller name
- API name
- Request status
- Processing latency

### Actuator

Spring Boot Actuator exposes:

- Health endpoints
- Metrics
- Runtime information

---

## Tech Stack

- Java 17
- Spring Boot
- Spring Security
- MongoDB
- Cloudflare R2
- AWS S3 SDK
- OpenFeign
- Micrometer
- Spring Actuator
- Aspect-Oriented Programming (AOP)

---

## Limitations

The semantic tagging system is currently manual.

When new raw tags appear:

1. The raw tag is stored automatically.
2. Semantic mappings must be added manually.

A future improvement would be AI-assisted tag classification to automatically generate semantic mappings and reduce manual maintenance.

---

## Summary

The Reel Service is responsible for reel creation, engagement tracking, semantic tagging, popularity calculation, and personalized feed generation. It combines semantic interests, popularity-based ranking, and content freshness to provide a lightweight recommendation system while remaining simple enough to evolve as the platform grows.