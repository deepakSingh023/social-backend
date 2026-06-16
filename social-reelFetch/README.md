# Reel Fetch Service

## Overview

The Reel Fetch Service acts as the orchestration layer for reel recommendations.

Its responsibility is to connect user interest profiles with the Reel Service recommendation engine.

The service does not store reels, manage popularity, calculate interests, or process engagement events.

Instead, it retrieves a user's interests from the Interest Service and forwards them to the Reel Service, which generates the final personalized reel feed.

This separation keeps recommendation orchestration independent from both interest management and reel storage.

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

The Reel Fetch Service sits between the client and the recommendation ecosystem.

Workflow:

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
2. User identity is extracted from JWT authentication
3. Interest Service is called
4. User interest profile is retrieved
5. Feed request is constructed
6. Reel Service receives the request
7. Reel Service generates a personalized feed
8. Feed is returned to the client

Workflow:

```text
Request Feed
      |
      v
Fetch User Interests
      |
      v
Build Recommendation Request
      |
      v
Call Reel Service
      |
      v
Return Feed
```

---

## API Endpoints

### Get Personalized Feed

```http
GET /api/reels/feed
```

Parameters:

| Parameter | Required | Description               |
| --------- | -------- | ------------------------- |
| cursor    | No       | Pagination cursor         |
| limit     | No       | Number of reels to return |

Authentication:

```text
JWT Required
```

Purpose:

* Retrieve personalized reels
* Support infinite scrolling
* Forward recommendation requests

---

## Request Processing

The service performs the following sequence:

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

The Reel Fetch Service simply forwards:

```text
cursor
limit
```

to the recommendation engine.

Advantages:

* Stateless orchestration
* Simple implementation
* Centralized recommendation pagination

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

This simplifies debugging and monitoring.

---

## Security

The service communicates with internal services using service-to-service authentication.

Internal requests include:

```text
X-SECRET-TOKEN
```

This token is forwarded when calling:

* Interest Service
* Reel Service

External users access the service through JWT authentication.

---

## Scalability Characteristics

The service is stateless.

Each request performs:

```text
Fetch Interest
        +
Request Feed
```

Complexity:

```text
O(1)
```

No database access occurs inside this service.

No recommendation calculations occur inside this service.

This makes horizontal scaling straightforward.

---

## Technology Stack

* Java 17
* Spring Boot
* Spring Security
* OpenFeign
* Spring Aop

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

Advantages:

* Reduced frontend complexity
* Centralized recommendation workflow
* Easier service evolution
* Better separation of concerns

Trade-off:

* Additional network hop

The architectural simplicity outweighs the small latency increase.

---

### Interest Isolation

Interest management is separated from reel generation.

Advantages:

* Independent scaling
* Clear service boundaries
* Easier recommendation experimentation

This allows interest algorithms to evolve without modifying reel retrieval logic.

---

## Summary

The Reel Fetch Service acts as the recommendation orchestration layer of the reel ecosystem.

It retrieves user interests from the Interest Service, constructs recommendation requests, forwards them to the Reel Service, and returns personalized reel feeds to clients.

By separating recommendation orchestration from interest management and reel storage, the architecture remains modular, scalable, and easier to maintain as the recommendation system evolves.
