# Reel Fetch Service

## Overview

The Reel Fetch Service acts as the orchestration layer for reel recommendations.

Its responsibility is to connect user interest profiles with the Reel Service recommendation engine.

The service does not store reels, manage popularity, calculate interests, or process engagement events.

Instead, it retrieves a user's interests from the Interest Service and forwards them to the Reel Service, which generates the final personalized reel feed.

---

## Responsibilities

The service is responsible for:

* Reel feed orchestration
* Interest retrieval
* Recommendation request construction
* Feed pagination forwarding
* Service-to-service communication
* Personalized feed generation workflow

The service is not responsible for:

* Reel storage
* Interest calculation
* Popularity calculation
* View tracking
* Like tracking
* Recommendation ranking

---

## Architecture

```text
User Opens Reel Feed
        |
        v
Reel Fetch Service
        |
        v
Interest Service
        |
        |-- Return User Interests
        |
        v
Reel Fetch Service
        |
        v
Reel Service
        |
        |-- Generate Personalized Feed
        |
        v
Reel Fetch Service
        |
        v
Client
```

The service acts as an orchestration layer rather than a business logic layer.

---

## Feed Generation Flow

When a user requests reels:

1. Reel Fetch Service receives the request
2. Interest Service is called
3. User interest profile is retrieved
4. Feed request is constructed
5. Reel Service receives the request
6. Reel Service generates a personalized feed
7. Feed is returned to the client

---

## API Endpoints

### Get Personalized Feed

```http
GET /api/reels/feed
```

| Parameter | Required | Description               |
| --------- | -------- | ------------------------- |
| cursor    | No       | Pagination cursor         |
| limit     | No       | Number of reels to return |

---

## Request Processing

### Step 1

Retrieve user interests:

```text
Interest Service
        |
        v
UserInterest
```

### Step 2

Create recommendation request:

```text
FetchReelDto
|
|-- UserInterest
|-- Cursor
|-- Limit
```

### Step 3

Send request to Reel Service:

```text
Reel Service
        |
        v
FeedResponse
```

### Step 4

Return response to frontend.

---

## Service Integrations

### Interest Service

Purpose:

* Retrieve user interests
* Provide personalization data

API Used:

```text
POST /api/interests/getInterest
```

Returns:

```text
UserInterest
```

---

### Reel Service

Purpose:

* Generate reel recommendations
* Apply recommendation logic
* Return personalized reels

API Used:

```text
POST /api/reel/feed
```

Returns:

```text
FeedResponse
```

---

## Pagination Support

Pagination is delegated to the Reel Service.

The Reel Fetch Service forwards:

```text
cursor
limit
```

to the recommendation engine.

The service remains stateless and does not maintain pagination state.

---

## Error Handling

The service handles failures originating from downstream services.

Example:

### Interest Service Unavailable

```text
Reel Fetch Service
        |
        X
Interest Service
```

The exception is logged and propagated.

Logged information includes:

* HTTP status code
* Response body
* Service failure details

---

## Security

Security and JWT authentication are handled by the API Gateway.

The Reel Fetch Service does not perform JWT authentication or CORS handling.

The service contains a `GatewayHeaderFilter` that validates the Gateway secret before allowing requests to proceed.

---

## Scalability Characteristics

The service is stateless.

Each request performs:

```text
Fetch Interest
        +
Request Feed
```

No database access occurs inside this service.

No recommendation calculations occur inside this service.

This makes horizontal scaling straightforward.

---

## Observability

### Distributed Tracing

Tracing is handled using OpenTelemetry.

The service uses W3C trace context propagation and exports traces to the OpenTelemetry Collector, which forwards them to Jaeger.

### Metrics

Spring Boot Actuator and Micrometer provide the main service metrics.

Metrics are exposed through the Prometheus endpoint and collected by Prometheus for visualization in Grafana.

A custom Spring AOP metric aspect additionally records service-layer method metrics:

* `http.api.latency`
* `http.api.counter`

These metrics track method execution latency and success/error status.

---

## Technology Stack

* Java 17
* Spring Boot
* Spring Security
* OpenFeign
* Spring AOP
* Spring Boot Actuator
* Micrometer
* OpenTelemetry

---

## Design Decisions

### Dedicated Orchestration Layer

Instead of allowing frontend clients to call multiple services directly:

```text
Frontend
   |
   +--> Interest Service
   |
   +--> Reel Service
```

the architecture centralizes orchestration:

```text
Frontend
     |
     v
Reel Fetch Service
     |
     +--> Interest Service
     |
     +--> Reel Service
```

This keeps the recommendation workflow in one place and reduces frontend complexity.

---

### Interest Isolation

Interest management is separated from reel generation.

Interest Service manages user interests while Reel Service handles reel recommendation logic.

This keeps the service boundaries separate and allows each component to evolve independently.

---

## Summary

The Reel Fetch Service acts as the recommendation orchestration layer of the reel ecosystem.

It retrieves user interests from the Interest Service, constructs recommendation requests, forwards them to the Reel Service, and returns personalized reel feeds to clients.

The service remains stateless and focused on orchestration, while authentication is handled by the API Gateway and recommendation logic remains in Reel Service.
