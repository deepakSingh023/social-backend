# Social View Service

## Overview

The Social View Service is responsible for processing reel engagement events and updating user interests. It acts as the bridge between reel consumption and the recommendation system.

When a user watches or likes a reel, the View Service coordinates updates across the Reel Service and Interest Service.

The service itself does not store views or interests. Instead, it orchestrates communication between services responsible for popularity scoring and interest management.

---

## Responsibilities

### View Processing

When a user watches a reel, the View Service:

1. Receives the view event.
2. Calls the Reel Service.
3. Updates reel popularity and view statistics.
4. Retrieves semantic tags associated with the reel.
5. Sends the tags to the Interest Service.
6. Updates the user's interest profile.

### Like Interest Processing

When a user likes a reel:

1. The Likes Service calls the View Service.
2. The View Service requests semantic tags from the Reel Service.
3. The Interest Service receives a stronger interest signal.
4. User interests are updated accordingly.

---

## Architecture

```text
User Watches Reel
        |
        v
View Service
        |
        +------------------+
        |                  |
        v                  |
Reel Service              |
(Update View Count        |
 & Popularity Score)      |
        |                  |
        v                  |
Semantic Tags ------------+
        |
        v
Interest Service
(Update User Interests)
```

---

## Like Interest Flow

```text
User Likes Reel
        |
        v
Likes Service
        |
        v
View Service
        |
        v
Reel Service
(Get Semantic Tags)
        |
        v
Interest Service
(Update Interest Score)
```

---

## API Endpoints

### Create View

```http
POST /api/view/create
```

Registers a reel view event and updates user interests.

#### Request

```json
{
  "reelId": "reel123",
  "type": "WATCH_90"
}
```

#### Flow

* Updates reel popularity.
* Retrieves reel semantic tags.
* Updates user interests.

---

### Create Like Interest

```http
POST /api/view/create-like-interest
```

Called internally by the Likes Service when a reel receives a like.

#### Parameters

```http
?reelId=reel123
&userId=user456
```

#### Flow

* Retrieves reel semantic tags.
* Sends LIKE interest event.
* Updates user interests.

---

## Service Communication

### Reel Service

Used for:

* View updates
* Popularity recalculation
* Semantic tag retrieval

### Interest Service

Used for:

* User interest updates
* Interest score management

---

## Design Decisions

### Dedicated View Service

Instead of allowing multiple services to directly update interests, all engagement events pass through a centralized View Service.

Benefits:

* Single source of engagement processing
* Simplified recommendation pipeline
* Consistent interest update logic
* Easier future expansion

---

### Separation of Responsibilities

#### Reel Service

Responsible for:

* Reel storage
* View counts
* Popularity scoring
* Semantic tags

#### Interest Service

Responsible for:

* User interest profiles
* Interest decay
* Interest scoring

#### View Service

Responsible for:

* Event orchestration
* Coordinating updates
* Connecting engagement events to recommendation signals

---

## Observability

The service includes the same observability stack used across the platform.

### Structured Logging

Logs API execution details including:

* Controller
* Endpoint
* Status
* Latency

Example:

```text
controller=ViewController
api=createView
status=SUCCESS
latencyMs=34
```

### Distributed Tracing

Each request receives a unique trace identifier.

```text
traceId=8a3f2d8b-1234-5678-90ab-cdef12345678
```

This enables request tracking across services.

### Metrics

Micrometer metrics are collected for:

* Request count
* Success rate
* Error rate
* API latency

Example metrics:

```text
http.api.count
http.api.latency
```

Metrics are exposed through Spring Boot Actuator and can be visualized using Prometheus and Grafana.

---

## Technology Stack

* Java 17
* Spring Boot
* Spring Security
* Spring AOP
* OpenFeign
* Micrometer
* Spring Actuator

---

## Role in Recommendation Architecture

The View Service is a critical component of the reel recommendation pipeline.

```text
User Action
     |
     v
View Service
     |
     +--> Reel Service
     |
     +--> Interest Service
     |
     v
Updated User Interests
     |
     v
Reel Fetch Service
     |
     v
Personalized Reel Feed
```

By connecting engagement events with interest updates, the service enables personalized reel recommendations across the platform.
