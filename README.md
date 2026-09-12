## Social Media Platform Backend

A distributed social media backend built with **Java 17, Spring Boot, and microservices**.

The project focuses on practical distributed-system patterns rather than simply splitting an application into multiple services. It includes asynchronous processing with Kafka, Redis-based coordination and idempotency, a reactive API Gateway, fan-out feed generation, recommendation pipelines, real-time multi-instance chat, centralized observability, and horizontal service scaling with Docker Swarm.

---

## Features

* JWT authentication and authorization
* Reactive API Gateway using Spring Cloud Gateway WebFlux
* IP-based and token-based rate limiting
* Independent microservices with service-to-service communication through OpenFeign
* Kafka-based asynchronous processing
* Fan-Out-On-Write feed generation
* Interest-based reel recommendation pipeline
* Real-time chat using STOMP over WebSocket
* Redis Pub/Sub for multi-instance chat communication
* Instance-aware chat message routing
* Redis-based distributed locking
* Method-level and Redis-backed idempotency
* Cloudflare R2 media storage
* Prometheus and Grafana metrics
* OpenTelemetry distributed tracing with Jaeger
* Docker Compose development environment
* Docker Swarm support for horizontal service scaling

---

# Architecture

The system is organized as independently deployable services.

Synchronous communication is primarily handled through REST and OpenFeign, while Kafka is used for operations that can be processed asynchronously.

Redis is used for distributed coordination rather than as a general application cache.

```text
                              Client
                                 |
                                 v
                    +-------------------------+
                    |      API Gateway         |
                    | Spring Cloud Gateway     |
                    |         WebFlux          |
                    +------------+-------------+
                                 |
              +------------------+------------------+
              |                  |                  |
              v                  v                  v
        Auth Service       Profile Service     Other Services
                                 |
                                 |
                         Kafka Events
                                 |
                +----------------+----------------+
                |                |                |
                v                v                v
         Post / Reel       Likes Service    Interaction
         Services                              Service
                |                |                |
                +----------------+----------------+
                                 |
                                 v
                           Feed Service


                    Interaction Service
                            |
                            | Kafka
                            v
                       Chat Service
                            |
                    +-------+-------+
                    |               |
                    v               v
               Redis Pub/Sub    WebSocket
                    |
                    v
              Chat Instances


       Application Services
              |
              +---- OpenTelemetry
              |           |
              |           v
              |      OTel Collector
              |           |
              |           v
              |         Jaeger
              |
              +---- Actuator / Micrometer
                          |
                          v
                     Prometheus
                          |
                          v
                       Grafana
```

All services and infrastructure components run on the same Docker network in the Compose environment.

---

# Services

| Service                 | Responsibility                                              |
| ----------------------- | ----------------------------------------------------------- |
| **Auth Service**        | Registration, authentication and JWT generation             |
| **Profile Service**     | User profiles and profile-related data                      |
| **Post Service**        | Post creation, retrieval and media management               |
| **Likes Service**       | Likes, comments, nested replies and related counters        |
| **Reel Service**        | Reel creation, storage and metadata                         |
| **Feed Service**        | Feed generation and feed retrieval                          |
| **View Service**        | View tracking and popularity-related processing             |
| **Interest Service**    | User interest modeling and interest decay                   |
| **Reel Fetch Service**  | Personalized reel recommendation orchestration              |
| **Interaction Service** | Friends, followers and social-graph operations              |
| **Chat Service**        | Conversations and real-time messaging                       |
| **API Gateway**         | Routing, authentication, rate limiting and WebSocket access |

---

# API Gateway

The API Gateway is built with **Spring Cloud Gateway WebFlux**.

It is the external entry point for the backend and is responsible for:

* Routing requests to internal services
* JWT validation
* Gateway-to-service authentication
* CORS handling
* IP-based rate limiting
* Token/user-based rate limiting
* Public endpoint handling
* WebSocket routing

The gateway is reactive and uses Spring WebFlux rather than the servlet-based Spring MVC stack.

### Rate Limiting

Two rate-limiting approaches are used:

* **IP-based limiting** for limiting traffic originating from an IP address
* **Token-based limiting** for limiting authenticated users independently of their IP

This allows the gateway to apply different protection strategies depending on whether a request is anonymous or authenticated.

---

# Kafka

Kafka is used for asynchronous communication between services.

The main goal is to remove work that does not need to block the original request from the synchronous request path.

## Profile Data Denormalization

Profile Service acts as a producer of profile-related events.

Consumers include:

* Post Service
* Reel Service
* Likes Service
* Interaction Service

These services maintain the profile information required for their own operations instead of making synchronous profile lookups for every request.

This also reduces coupling between the services during normal request processing.

```text
              Profile Service
                    |
                    | Kafka
                    v
        +-----------+-----------+-----------+
        |           |           |           |
        v           v           v           v
      Post        Reel       Likes     Interaction
     Service     Service     Service     Service
```

---

## Feed Generation

Post Service and Interaction Service produce events consumed by Feed Service.

These events represent changes that can affect a user's feed, such as:

* Post creation
* Friend relationships
* Follow relationships

Feed Service processes these events asynchronously and generates the corresponding feed entries.

```text
Post Service -----------+
                        |
                        +----> Kafka ----> Feed Service
                        |
Interaction Service ----+
```

This keeps feed generation work separate from the request that created the post or relationship.

---

## Conversation Creation

Interaction Service also produces relevant relationship events consumed by Chat Service.

This allows conversation creation to happen asynchronously when a new relationship requires a corresponding conversation.

```text
Interaction Service
        |
        | Kafka
        v
   Chat Service
        |
        v
 Conversation
```

---

# Feed Generation

The home feed follows a **Fan-Out-On-Write** approach.

When a post is created, the system determines the users who should receive the post and generates feed entries asynchronously.

The work can involve collecting:

* Posts
* Followers
* Friends
* Relationship information

The Feed Service performs this processing in batches rather than requiring the complete operation to happen inside the original post-creation request.

The feed retrieval path can then work against generated feed data instead of rebuilding the feed from scratch for every request.

### Resilience

Selected critical operations in Feed Service use retry and circuit-breaker mechanisms.

In particular, the batch operation responsible for collecting the data required for feed generation is treated differently from less important supporting operations.

Retries are intentionally not applied indiscriminately. Repeating non-critical operations can create additional load without providing enough benefit to justify the retry.

---

# Reel Recommendations

The reel system separates reel storage and recommendation processing into multiple services.

The recommendation pipeline uses information such as:

* User interests
* Semantic tags
* Popularity
* View activity
* Time-based interest decay

The main services involved are:

```text
Reel Service
     |
     +---- View Service
     |
     +---- Interest Service
     |
     v
Reel Fetch Service
```

Reel Fetch Service coordinates the information required to produce personalized recommendations.

---

# Real-Time Chat

Chat uses **STOMP over WebSocket** for real-time communication.

The service also uses Redis for:

* Pub/Sub
* User presence
* Instance information
* Session metadata
* Targeted message routing

A major change in v2 was the way messages are routed between multiple Chat Service instances.

## Previous Approach

The earlier implementation propagated a message through Redis Pub/Sub in a way that allowed every Chat Service instance to receive it.

With multiple instances:

```text
Chat Instance A
       |
       v
   Redis Pub/Sub
       |
   +---+---+---+
   |   |   |   |
   v   v   v   v
  A    B   C   D
```

This meant instances could receive messages that were not intended for any connected user on that instance.

## Current Approach

Each Chat Service instance has its own instance identifier.

When a user establishes a WebSocket connection, the service records the user's current instance in Redis.

For example:

```text
User:user-1
    instance -> chat-instance-2

User:user-2
    instance -> chat-instance-3
```

When a message is sent, the sender's instance looks up the recipient's instance.

The message is then published to a Redis channel specific to that instance.

```text
user-1
   |
   v
chat-instance-2
   |
   v
Redis presence lookup
   |
   | user-2 -> chat-instance-3
   v
chat-channel:chat-instance-3
   |
   v
chat-instance-3
   |
   v
user-2
```

This avoids broadcasting the message to unrelated Chat Service instances.

When both users are connected to the same instance, the message can be handled locally without requiring inter-instance Pub/Sub.

This makes Redis Pub/Sub a **targeted inter-instance communication mechanism**, rather than a global broadcast channel.

---

# Redis

Redis is deliberately **not used as a general-purpose cache** in this project.

Its primary roles are distributed coordination and real-time infrastructure.

### Pub/Sub

Used for communication between Chat Service instances.

### Distributed Locking

Used where concurrent execution across service instances needs coordination.

### Idempotency

Redis is used to maintain idempotency state for operations where duplicate processing is possible.

This is particularly important for asynchronous Kafka consumers and operations that may be retried.

### Chat Presence

Redis stores information required to determine where a connected user currently resides.

This includes mappings such as:

```text
User -> Chat Instance
Session -> User
Conversation -> Connected Users
```

This information allows the Chat Service to route messages to the correct instance.

---

# Idempotency

Idempotency is implemented at both the application and distributed levels.

### Method-Level Idempotency

Important service methods are designed to safely handle repeated execution of the same logical operation.

### Redis-Based Idempotency

Redis is also used to track operations where duplicate processing needs to be prevented.

This is particularly relevant to Kafka consumers because asynchronous processing should not assume that an event will only ever be encountered once.

The combination allows the system to tolerate retries and duplicate processing without blindly creating duplicate application state.

---

# Distributed Tracing

Distributed tracing is implemented using **OpenTelemetry**.

Each service exports telemetry to a centralized OpenTelemetry Collector.

```text
Spring Boot Services
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

The collector acts as the telemetry pipeline between the services and Jaeger.

This allows requests passing through multiple services to be inspected as a distributed trace.

For example:

```text
Client
  |
  v
Gateway
  |
  v
Profile Service
  |
  v
Downstream Service
```

The resulting trace can be inspected in Jaeger to follow the request across service boundaries and examine individual spans and timings.

---

# Metrics

Application metrics are exposed through Spring Boot Actuator and Micrometer.

Each service exposes:

```text
/actuator/prometheus
```

Prometheus scrapes these endpoints and stores the resulting time-series data.

Grafana is connected to Prometheus and provides visualization of the collected metrics.

```text
Spring Boot Services
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

The monitoring stack can be used to inspect request rates, HTTP status codes, latency and service availability.

---

# Horizontal Scaling

The project is designed to support horizontal service scaling using **Docker Swarm**.

The Gateway communicates with Swarm-managed services, while Swarm provides service discovery and load balancing across service replicas.

No NGINX load balancer is required between the Gateway and the scaled backend services.

Conceptually:

```text
                         Client
                            |
                            v
                     API Gateway
                            |
                            v
                     Docker Swarm
                            |
              +-------------+-------------+
              |             |             |
              v             v             v
          Service #1    Service #2    Service #3
```

Services can therefore be scaled independently.

For example, the Chat Service can be deployed with multiple replicas when additional WebSocket capacity is required.

The instance-aware Redis routing described above allows those Chat Service replicas to coordinate message delivery.

---

## Scaling Validation

The complete multi-service Swarm deployment was not exhaustively tested locally because of available CPU and memory constraints.

The Chat Service, however, was specifically scaled and tested because multi-instance operation is a core requirement of its architecture.

Multiple Chat Service instances were run using Docker Compose scaling, and requests through the Gateway were successfully distributed across the scaled instances.

This validated the Gateway-to-multiple-instance routing behavior.

A full production-scale Swarm deployment remains an environment-level validation rather than something that was exhaustively exercised on the development machine.

---

# Docker

The complete backend infrastructure is designed to run through Docker.

The Compose environment contains:

* Auth Service
* Profile Service
* Post Service
* Likes Service
* Reel Service
* Interaction Service
* Feed Service
* View Service
* Interest Service
* Reel Fetch Service
* Chat Service
* API Gateway
* Redis
* Kafka
* Prometheus
* Grafana
* OpenTelemetry Collector
* Jaeger

All components are connected to the same Docker network.

Internal communication uses Docker service names.

Examples:

```text
http://profile-service:8081
http://redis:6379
http://kafka:9092
```

The Compose configuration also defines CPU and memory limits for the services and infrastructure components.

---

# Technology Stack

### Backend

* Java 17
* Spring Boot
* Spring Security
* Spring WebFlux
* Spring WebSocket
* Spring Data MongoDB
* OpenFeign
* Spring AOP

### Messaging & Coordination

* Apache Kafka
* Redis
* Redis Pub/Sub
* Redis distributed locking

### Databases

* MongoDB
* PostgreSQL where applicable

### Storage

* Cloudflare R2

### Observability

* Spring Boot Actuator
* Micrometer
* Prometheus
* Grafana
* OpenTelemetry
* OpenTelemetry Collector
* Jaeger

### Infrastructure

* Docker
* Docker Compose
* Docker Swarm

---

# Running Locally

## Prerequisites

* Docker
* Docker Compose
* Node.js
* npm

## Backend

```bash
git clone <backend-repository-url>
cd social-backend

cp .env.example .env
```

Configure the required environment variables and then start the stack:

```bash
docker compose up --build
```

## Frontend

```bash
git clone <frontend-repository-url>
cd frontend

npm install
npm run dev
```

The frontend communicates with the backend through the API Gateway.

---

# Default Local Ports

| Component               |           Port |
| ----------------------- | -------------: |
| API Gateway             |         `8091` |
| Prometheus              |         `9090` |
| Grafana                 |         `3001` |
| Jaeger UI               |        `16686` |
| Redis                   |         `6379` |
| OpenTelemetry Collector | `4317`, `4318` |

Some application services also expose ports directly for local development and testing.

Internal service-to-service communication uses Docker network addresses.

---

# Environment Variables

Configuration and secrets are supplied through environment variables.

Typical configuration includes:

```env
JWT_SECRET=
GATEWAY_SECRET=
SERVICE_SECRET=

AUTH_DB_URL=
AUTH_DB_USERNAME=
AUTH_DB_PASSWORD=

PROFILE_MONGO_URI=
POST_MONGO_URI=
LIKES_MONGO_URI=
REEL_MONGO_URI=
INTERACTION_MONGO_URI=
FEED_MONGO_URI=
INTEREST_MONGO_URI=
CHAT_MONGO_URI=

R2_ACCESS_KEY=
R2_SECRET_KEY=
R2_ACCOUNT_ID=
R2_PUBLIC_BASE_URL=

REDIS_HOST=
REDIS_PORT=
```

The exact variables depend on the service and are defined in the Docker Compose configuration.

Secrets should not be committed to the repository.

---

# Documentation

More detailed documentation is available under `docs/`.

The documentation covers areas including:

* Architecture
* API Gateway
* Service communication
* Kafka pipelines
* Feed generation
* Recommendation system
* Chat architecture
* Database strategy
* Security
* Resilience
* Redis coordination
* Idempotency
* Deployment
* Observability

---

# Project Status

The current `main` branch contains the completed v2 implementation.

The project has been developed incrementally from the earlier v1 architecture, with v2 introducing significant changes to the communication, gateway, asynchronous processing, observability, and scalability layers.

The implementation has been validated through service-level and API testing, infrastructure testing, Kafka flows, Redis coordination, Gateway routing, observability, and multi-instance Chat Service testing.

Full simultaneous execution of every service and exhaustive multi-replica testing was limited by local hardware resources.

---

# Future Improvements

Possible future work includes:

* Kubernetes deployment
* End-to-end encrypted messaging
* Further Kafka deployment and replication
* Automated CI/CD
* Additional monitoring and alerting
* Read receipts and typing indicators
* Further optimization of feed and recommendation pipelines

---

# License

This project was developed for educational and research purposes.
