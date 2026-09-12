# API Gateway

The API Gateway is the single entry point for the backend and is built using **Spring Cloud Gateway WebFlux**.

It handles request routing, JWT authentication, service authentication, rate limiting, CORS, WebSocket routing, request logging, and gateway-level metrics.

The Gateway communicates with backend services through Docker's internal DNS names, allowing the same service address to work when a service is scaled to multiple replicas.

---

## Responsibilities

* Route API requests to the appropriate microservice
* Validate JWT access tokens
* Propagate authenticated user identity to downstream services
* Authenticate Gateway-to-service communication
* Apply IP-based and user/token-based rate limiting
* Handle CORS
* Route WebSocket connections to Chat Service
* Record gateway request metrics
* Log inbound and outbound routing information
* Export distributed traces through OpenTelemetry

---

# Architecture

```text
                         Client
                            |
                            v
                  +-------------------+
                  |    API Gateway    |
                  |  Spring WebFlux   |
                  +---------+---------+
                            |
             +--------------+---------------+
             |              |               |
             v              v               v
        Auth Service   Profile Service   Post Service
             |              |               |
             |              |               |
             +--------------+---------------+
                            |
                       Other Services
```

The Gateway is the only externally exposed application entry point in the normal request flow.

Internal services are addressed using Docker service names, for example:

```text
http://auth-service:8080
http://profile-service:8081
http://post-service:8082
http://chat-service:8090
```

This also allows Docker's service discovery and load balancing to distribute requests when a service has multiple replicas.

---

# Spring Cloud Gateway WebFlux

The Gateway uses the reactive Spring Cloud Gateway implementation.

The application runs on port:

```text
8091
```

The routing layer maps public API paths to internal Docker services.

Examples:

| Public Path           | Target                  |
| --------------------- | ----------------------- |
| `/api/auth/**`        | Auth Service            |
| `/api/posts/**`       | Post Service            |
| `/api/profiles/**`    | Profile Service         |
| `/api/getFeed/**`     | Feed Service            |
| `/api/like/**`        | Likes Service           |
| `/api/comments/**`    | Likes Service           |
| `/api/relations/**`   | Interaction Service     |
| `/api/friends/**`     | Interaction Service     |
| `/api/search/**`      | Interaction Service     |
| `/api/reels/**`       | Reel Service            |
| `/api/view/**`        | View Service            |
| `/api/reels-fetch/**` | Reel Fetch Service      |
| `/api/chat/**`        | Chat Service            |
| `/ws/**`              | Chat WebSocket endpoint |

---

# Authentication

JWT authentication is performed at the Gateway.

The Gateway reads the `Authorization` header:

```text
Authorization: Bearer <JWT>
```

The token is validated using the configured JWT secret.

For a valid token, the user's ID is extracted from the token subject and placed into the reactive Spring Security context.

Authenticated requests can then be propagated to downstream services.

Public endpoints are explicitly permitted, including:

```text
/api/health
/api/auth/login
/api/auth/signup
/api/auth/refresh
/api/profiles/search
/actuator/**
/ws/**
```

All other exchanges require authentication.

---

# Gateway-to-Service Authentication

The Gateway adds a shared secret to authenticated downstream requests:

```text
X-Gateway-Secret
```

It also propagates the authenticated user's ID:

```text
X-User-Id
```

This provides two pieces of information to downstream services:

```text
X-User-Id
        |
        +---- authenticated user identity

X-Gateway-Secret
        |
        +---- request originated through the trusted Gateway
```

The secrets are provided through environment variables rather than being stored directly in source code.

---

# Rate Limiting

Rate limiting is implemented using Spring Cloud Gateway's `RequestRateLimiter` filter with Redis as the backing store.

Two key-resolution strategies are used.

## IP-Based Rate Limiting

The IP resolver creates keys in the form:

```text
rate:ip:<ip-address>
```

This is used for routes where limiting traffic by originating IP is appropriate.

For example, authentication endpoints use IP-based limiting:

```text
/api/auth/**
```

Current configuration:

```text
replenishRate: 20
burstCapacity: 20
```

---

## Token/User-Based Rate Limiting

Authenticated routes primarily use the authenticated user's ID:

```text
rate:user:<user-id>
```

If the user ID is unavailable, the resolver can fall back to a hash derived from the Bearer token.

Anonymous requests ultimately fall back to:

```text
rate:user:anonymous
```

Different routes have different limits based on their expected traffic characteristics.

Current configured limits include:

| Route          | Key        | Rate | Burst |
| -------------- | ---------- | ---: | ----: |
| Auth           | IP         |   20 |    20 |
| Posts          | User/Token |  100 |   100 |
| Profiles       | User/Token |   60 |    60 |
| Feed           | User/Token |  200 |   200 |
| Likes/Comments | User/Token |   80 |    80 |
| Interactions   | User/Token |   80 |    80 |
| Reels          | User/Token |  120 |   120 |
| Views          | User/Token |  150 |   150 |
| Reel Fetch     | User/Token |  200 |   200 |
| Chat REST      | User/Token |   60 |    60 |

These values are configured in the Gateway route definitions and can be changed without modifying the rate-limiter implementation.

---

# WebSocket Routing

Chat WebSocket traffic is routed through the Gateway using:

```text
/ws/**
```

The Gateway forwards the connection to:

```text
http://chat-service:8090
```

The Chat Service uses STOMP over WebSocket for real-time messaging.

The Gateway also applies response-header deduplication for the WebSocket route to avoid conflicting CORS headers.

---

# CORS

CORS is configured at the Gateway using Spring WebFlux's reactive CORS support.

The current development configuration allows:

```text
http://localhost:3000
```

Allowed methods include:

```text
GET
POST
PUT
DELETE
PATCH
OPTIONS
```

Credentials are enabled and the `Authorization` header is exposed.

CORS is therefore handled centrally at the external entry point rather than requiring every service to independently manage the browser-facing CORS policy.

---

# Request Logging

A global Gateway filter records inbound and outbound routing information.

For an incoming request, the filter records information such as:

```text
Method
Path
Target service
Target URI
```

When the request completes, it records:

```text
Method
Path
Service
HTTP status
```

Routing failures are also recorded separately.

The route ID is obtained directly from Spring Cloud Gateway's route metadata.

---

# Gateway Metrics

The Gateway defines application-specific Micrometer metrics in addition to the standard Spring Boot metrics.

### Request Processing Time

```text
gateway.request.processing.time
```

This records Gateway request-processing duration and publishes percentile measurements including:

```text
p50
p90
p95
p99
```

### Authentication Traffic

```text
gateway.auth.requests
```

Requests are categorized by authentication status.

```text
authenticated
unauthorized
```

### JWT Validation Failures

```text
gateway.jwt.validation.failures
```

Failures can be categorized by cause, such as:

```text
expired
invalid_signature
malformed
```

### Routing Status

```text
gateway.routing.requests
```

Routing metrics contain information including:

```text
route_id
status_code
outcome
```

where the outcome is classified as `SUCCESS` or `FAILURE`.

---

# Observability

The Gateway participates in the same observability pipeline as the other backend services.

## Metrics

Spring Boot Actuator and Micrometer expose metrics through:

```text
/actuator/prometheus
```

Prometheus scrapes the Gateway's metrics endpoint.

```text
Gateway
   |
   v
Actuator / Micrometer
   |
   v
Prometheus
   |
   v
Grafana
```

The Gateway also adds custom metrics for authentication, routing, JWT failures, and request-processing time.

---

## Distributed Tracing

The Gateway has OpenTelemetry tracing enabled.

Tracing uses W3C propagation and exports spans to the OpenTelemetry Collector:

```text
Gateway
   |
   v
OpenTelemetry
   |
   v
OTel Collector
   |
   v
Jaeger
```

Because the Gateway is the entry point, its spans can appear at the beginning of distributed traces that continue into downstream services.

This makes it possible to follow a request from the client through the Gateway and into the service handling the operation.

---

# Configuration

The Gateway uses environment variables for secrets:

```env
JWT_SECRET=
GATEWAY_SECRET=
```

Redis is accessed through the Docker service name:

```text
redis:6379
```

The OpenTelemetry Collector is accessed through:

```text
otel-collector:4318
```

The application runs on:

```text
server.port=8091
```

---

# Docker and Scaling

The Gateway itself runs as a Docker service and communicates with backend services over the shared Docker network.

Backend services can be scaled independently.

For example, when multiple Chat Service replicas are running:

```text
                         Gateway
                            |
                            v
                     chat-service
                            |
              +-------------+-------------+
              |             |             |
              v             v             v
          Instance 1    Instance 2    Instance 3
```

Docker service discovery and load balancing distribute requests across the available replicas.

No separate NGINX load-balancing layer is required between the Gateway and Swarm-managed backend services.

The Chat Service was specifically tested with multiple instances locally, and Gateway routing successfully distributed requests across the scaled instances.

Full multi-replica Swarm deployment of the complete backend was not exhaustively tested because of local hardware constraints.

---

# Security Model

The Gateway provides the first layer of request protection:

```text
Client
  |
  v
JWT validation
  |
  v
Rate limiting
  |
  v
Gateway authentication
  |
  v
X-User-Id + X-Gateway-Secret
  |
  v
Internal Service
```

The Gateway therefore separates external client authentication from trusted internal service communication.

JWT secrets and Gateway secrets are supplied through environment variables.

---

# Local Development

The Gateway is normally started as part of the Docker Compose environment.

```bash
docker compose up --build gateway
```

Or start the complete backend:

```bash
docker compose up --build
```

The Gateway is available at:

```text
http://localhost:8091
```

Health and observability endpoints are available through the exposed Actuator configuration.

For example:

```text
http://localhost:8091/actuator/health
http://localhost:8091/actuator/prometheus
```

---

# Key Design Decisions

### Reactive Gateway

Spring Cloud Gateway WebFlux was selected to provide a reactive request-routing layer suitable for a service-oriented backend.

### Centralized Authentication

JWT validation is performed at the Gateway so that external requests are authenticated before being routed internally.

### Two Rate-Limiting Strategies

IP-based limiting is useful for endpoints such as authentication, while user/token-based limiting provides more appropriate control for authenticated API traffic.

### Redis for Rate Limiting

Redis provides the shared state required for rate limiting and allows the limiter to remain independent of the Gateway process itself.

### Docker-Native Service Discovery

The Gateway addresses services through Docker service names, allowing the deployment layer to handle service discovery and load balancing when replicas are introduced.

### Centralized Observability

Tracing and metrics are integrated at the Gateway as well as the downstream services, allowing both the entry point and the distributed request path to be observed.

---

# Related Components

The Gateway is part of the wider backend infrastructure:

```text
API Gateway
    |
    +---- Auth Service
    +---- Profile Service
    +---- Post Service
    +---- Likes Service
    +---- Interaction Service
    +---- Feed Service
    +---- Reel Service
    +---- View Service
    +---- Interest Service
    +---- Reel Fetch Service
    +---- Chat Service
```

Observability:

```text
Services
   |
   +---- Prometheus ----> Grafana
   |
   +---- OTel Collector -> Jaeger
```

The detailed system architecture and service-specific documentation are available in the repository's `docs/` directory.
