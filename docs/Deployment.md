# Deployment Guide

## Overview

The Social Media Backend is deployed as independently deployable Spring Boot microservices using Docker Compose.

The deployment combines application containers, infrastructure containers, and managed external services.

### Application and Infrastructure

* Spring Boot microservices
* Spring Cloud Gateway
* Apache Kafka
* Redis
* Prometheus
* Grafana
* OpenTelemetry Collector
* Jaeger

### Managed Services

* MongoDB
* Cloudflare R2

Docker Compose provides the shared network, service configuration, resource constraints, and replica configuration used by the deployment.

---

## Deployment Architecture

```text
                         Client
                           |
                           v
                    API Gateway :8091
                           |
          +----------------+----------------+
          |                |                |
          v                v                v
     REST Services     Chat Service      WebSocket
          |                |                |
          v                v                v
       MongoDB          MongoDB           Redis


                 Asynchronous Communication

                    Service
                       |
                       v
                     Outbox
                       |
                       v
                     Kafka
                       |
              +--------+--------+
              |        |        |
              v        v        v
           Consumer Consumer Consumer


                    Observability

 Services
    |
    +---- OpenTelemetry ----> OTel Collector ----> Jaeger
    |
    +---- Actuator/Micrometer ----> Prometheus ----> Grafana
```

The API Gateway handles external application traffic.

Kafka handles selected asynchronous workflows, while Redis provides shared state and Chat Service instance routing.

---

## Infrastructure Components

| Component               | Deployment         | Purpose                                                             |
| ----------------------- | ------------------ | ------------------------------------------------------------------- |
| MongoDB                 | Managed / External | Service persistence                                                 |
| Redis                   | Docker             | Idempotency, coordination, rate limiting, Chat presence and routing |
| Apache Kafka            | Docker             | Asynchronous service communication                                  |
| Prometheus              | Docker             | Metrics collection                                                  |
| Grafana                 | Docker             | Metrics visualization                                               |
| OpenTelemetry Collector | Docker             | Trace collection                                                    |
| Jaeger                  | Docker             | Distributed trace visualization                                     |
| Cloudflare R2           | Managed Cloud      | Image and video storage                                             |

Application containers communicate with containerized infrastructure through the shared Docker network.

---

## Application Services

| Service                | Responsibility                                           |
| ---------------------- | -------------------------------------------------------- |
| Authentication Service | Authentication and JWT generation                        |
| Profile Service        | User profile management                                  |
| Post Service           | Post management                                          |
| Feed Service           | Feed generation and storage                              |
| Reel Service           | Reel storage and recommendation data                     |
| Reel Fetch Service     | Reel retrieval and orchestration                         |
| Interest Service       | User interest management                                 |
| View Service           | View tracking                                            |
| Interaction Service    | Friends, followers, relationships, and interaction graph |
| Likes Service          | Likes and comments                                       |
| Chat Service           | Real-time messaging and conversations                    |

Each service owns its business logic and persistence boundary.

Services do not directly access another service's database.

---

## API Gateway

The API Gateway is implemented using Spring Cloud Gateway and runs on host port `8091`.

It is responsible for:

* Request routing
* JWT validation
* CORS handling
* Rate limiting
* WebSocket routing
* Gateway request validation
* W3C trace propagation
* Gateway-level logging and metrics

After validating a request, the Gateway passes trusted headers to downstream services.

Downstream services validate these headers through application-level gateway and internal filters.

---

## Container Networking

Application and infrastructure containers communicate through a shared Docker network.

```text
                    Docker Network
                          |
       +------------------+------------------+
       |                  |                  |
       v                  v                  v
    Gateway             Kafka              Redis
       |
       +-------------------------+
       |                         |
       v                         v
 Application Services      OTel Collector
                                  |
                                  v
                                Jaeger

 Application Services
          |
          v
      Prometheus
          |
          v
       Grafana
```

Containers use Docker service names for internal communication rather than host IP addresses.

Only required services expose ports to the host.

| Component               |      Host Port |
| ----------------------- | -------------: |
| Frontend                |         `3000` |
| API Gateway             |         `8091` |
| Grafana                 |         `3001` |
| Prometheus              |         `9090` |
| Jaeger UI               |        `16686` |
| Redis                   |         `6379` |
| OpenTelemetry Collector | `4317`, `4318` |

---

## Service Communication

The system uses both synchronous and asynchronous communication.

### Synchronous

REST and OpenFeign are used when a service needs an immediate response from another service.

### Asynchronous

Kafka is used for workflows where an immediate response is not required.

```text
Producer Service
      |
      v
    Outbox
      |
      v
    Kafka
      |
      +------> Consumer
      |
      +------> Consumer
      |
      +------> Consumer
```

Current Kafka workflows include:

* Profile denormalization
* Feed creation and deletion
* Interaction feed synchronization
* Chat conversation creation
* Chat conversation deletion

The Outbox Pattern is used for reliable event publication.

---

## Kafka and Outbox

Kafka runs as a Docker container and is available to application services through the Docker network.

The main event flows are:

### Profile Events

```text
Profile Service
      |
    Outbox
      |
    Kafka
      |
      +---- Post
      +---- Reel
      +---- Likes
      +---- Interaction
```

### Feed Events

```text
Post / Interaction
       |
     Outbox
       |
     Kafka
       |
       v
   Feed Service
```

### Conversation Events

```text
Interaction Service
        |
      Outbox
        |
      Kafka
        |
        v
   Chat Service
```

The Chat Service consumes:

* `conversation-create`
* `conversation-delete`

Where implemented, consumers use Redis to store processed event IDs for a 24-hour idempotency window.

Outbox records are cleaned up after 24 hours.

---

## Redis

Redis is used for several different parts of the deployment:

* Kafka consumer idempotency
* Distributed coordination
* Gateway rate-limiting state
* Chat WebSocket presence
* Chat Service instance routing
* Chat Redis Pub/Sub

For Chat Service scaling, Redis maintains the relationship between connected users and their Chat Service instances.

```text
User
 |
 v
Chat Instance
 |
 v
Redis
 |
 +---- User:{userId} ------> instance
 |
 +---- SessionId:{sessionId} -> user
 |
 +---- chat-channel:{instanceId}
```

Redis stores transient coordination and routing state. Persistent chat messages remain in MongoDB.

---

## Chat Service Scaling

The Chat Service supports multiple instances.

Nginx is not used for inter-instance message routing. Redis handles this part of the architecture.

```text
             Chat Client A                 Chat Client B
                    |                             |
                    v                             v
             Chat Instance A               Chat Instance B
                    |                             |
                    +-------------+---------------+
                                  |
                                  v
                                Redis
                                  |
                    +-------------+-------------+
                    |                           |
             channel:A                    channel:B
```

When a message is sent:

1. The message is persisted in MongoDB.
2. The receiver is identified from the conversation.
3. Redis is used to find the receiver's active Chat Service instance.
4. The message is published to that instance's Redis channel.
5. That instance delivers the message through STOMP/WebSocket.

This avoids broadcasting every message to every Chat Service instance.

---

## Resource Constraints

The Docker Compose configuration defines CPU and memory constraints for the containers.

These limits are important when running the complete backend locally because the deployment includes multiple application services together with Kafka, Redis, and the observability stack.

The constraints prevent a single container from consuming an uncontrolled amount of the available host resources.

---

## Horizontal Scaling

The Compose configuration contains `deploy.replicas` settings for services.

These settings can be used in environments supporting Docker Swarm-style deployment.

```text
                  Service
                     |
          +----------+----------+
          |          |          |
          v          v          v
       Instance 1 Instance 2 Instance 3
```

The Chat Service has been used to demonstrate multiple instances and cross-instance message delivery.

Redis provides the instance routing required for this setup.

A full multi-node Swarm deployment has not been tested locally.

---

## Observability

The deployment includes metrics and distributed tracing.

### Metrics

```text
Spring Boot Services
        |
Actuator / Micrometer
        |
        v
Prometheus :9090
        |
        v
Grafana :3001
```

Spring Boot Actuator and Micrometer expose service and JVM metrics.

Prometheus collects the metrics and Grafana provides dashboards for inspection.

### Distributed Tracing

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
Jaeger :16686
```

OpenTelemetry provides distributed tracing instrumentation.

The OpenTelemetry Collector receives the trace data and forwards it to Jaeger.

W3C Trace Context propagation allows traces to be correlated across service boundaries.

---

## Environment Configuration

Environment-specific configuration is supplied through environment variables.

The deployment uses environment variables for:

* MongoDB connection strings
* Redis configuration
* Kafka configuration
* JWT secrets
* Gateway and internal service secrets
* Cloudflare R2 credentials
* OpenTelemetry configuration
* Service ports
* Chat Service instance identifiers

This keeps credentials and environment-specific configuration outside the application source code.

---

## Running the Platform

After the required environment variables and external service credentials are configured, the backend can be started using Docker Compose.

The deployment starts the infrastructure and application containers, after which the services connect to their configured databases and supporting infrastructure.

The API Gateway then provides the external backend entry point.

Prometheus, Grafana, OpenTelemetry Collector, and Jaeger run alongside the application and collect telemetry from the deployed services.

---

## Deployment Limitations

The current deployment has several known limitations:

* A full multi-node Swarm deployment has not been tested.
* Automatic horizontal scaling is not implemented.
* Redis Pub/Sub is transient and does not provide durable message delivery.
* Environment configuration requires manual setup.
* The deployment depends on externally configured MongoDB and Cloudflare R2 services.
* Local resource limits restrict how many service replicas can be run comfortably.

These limitations describe the current deployment rather than features that are planned or already implemented.

---

## Future Deployment Work

Potential deployment improvements are:

* Kubernetes orchestration
* CI/CD deployment
* Automated environment provisioning
* Centralized secret management
* Automated service scaling
* Multi-node deployment testing
* Centralized log aggregation
