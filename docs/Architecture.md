# System Architecture

The Social Media Backend is a distributed social media platform designed to support posts, reels, personalized feeds, user interactions, and real-time messaging.

The system is implemented using a microservices architecture in which each service owns a specific business domain and its corresponding data. Services communicate through well-defined APIs and asynchronous event flows where required.

The architecture uses a hybrid feed and recommendation model. Post feeds use a fan-out-on-write approach, while reel recommendations are generated on read using interest-based ranking and popularity signals.

Real-time messaging is implemented using WebSockets and STOMP. Redis is used for WebSocket presence and targeted cross-instance message routing, allowing messages to be delivered to the Chat Service instance where the receiving user is connected.

Kafka is used for asynchronous service communication, with the Outbox Pattern providing reliable event publishing for important cross-service workflows.

The platform also includes centralized API Gateway handling and an observability stack based on OpenTelemetry, Prometheus, Grafana, and Jaeger.

---

## System Goals

* Separation of concerns
* Independent service ownership
* Personalized content delivery
* Asynchronous service communication
* Real-time messaging
* Horizontal scalability
* Reliable event processing
* Centralized API Gateway
* Distributed tracing and monitoring

---

## High Level Architecture

The platform consists of multiple domain-oriented services. Each service is responsible for a specific business capability and owns its corresponding data.

External requests enter through the API Gateway, which provides centralized routing, authentication, rate limiting, CORS handling, and request propagation.

Asynchronous workflows use Kafka, while Redis supports distributed coordination and Chat Service instance routing.

```text
                         Client
                           |
                           v
                    API Gateway
                           |
          +----------------+----------------+
          |                |                |
          v                v                v
     REST Services     Chat Service      WebSocket
          |                                  |
          |                                  v
          |                              Redis
          |                                  |
          v                                  v
      Databases                         Chat Instance
          |
          |
          +---------- Kafka <----------+
                       |
                       v
                 Event Consumers

Observability:

Services
   |
   +---- OpenTelemetry ----> OTel Collector ----> Jaeger
   |
   +---- Actuator/Micrometer ----> Prometheus ----> Grafana
```

---

## Service Overview

The platform is composed of domain-oriented services that separate authentication, content management, recommendations, social interactions, feed generation, and real-time communication into independent deployment units.

| Service     | Responsibility                                           |
| ----------- | -------------------------------------------------------- |
| Auth        | Authentication and JWT                                   |
| Profile     | User profile management                                  |
| Post        | Post storage and post events                             |
| Reel        | Reel storage and recommendation data                     |
| Feed        | Feed generation and feed storage                         |
| Reel Fetch  | Reel retrieval and orchestration                         |
| Interest    | User interests                                           |
| View        | View tracking                                            |
| Interaction | Friends, followers, relationships, and interaction graph |
| Chat        | Real-time messaging and conversations                    |
| Likes       | Likes and comments                                       |

---

## API Gateway

The API Gateway is the external entry point for the backend services.

It is responsible for:

* JWT validation
* Request routing
* CORS handling
* Rate limiting
* WebSocket routing
* Gateway request validation
* W3C trace propagation
* Gateway-level metrics and logging

Authenticated requests are propagated to downstream services using trusted gateway headers.

Downstream services validate these headers through application-level gateway/internal filters.

---

## Data Ownership

The system follows a database-per-service approach where each service owns and manages its persistence layer.

Services do not directly access another service's database. Cross-service data access is performed through service APIs or asynchronous events.

Certain orchestration services such as View Service and Reel Fetch Service do not maintain dedicated persistent storage. Instead, they coordinate operations across other services and aggregate data required by business workflows.

| Service                | Owned Data                                      |
| ---------------------- | ----------------------------------------------- |
| Authentication Service | User credentials, authentication data           |
| Profile Service        | User profiles                                   |
| Post Service           | Posts                                           |
| Feed Service           | User feed entries                               |
| Likes Service          | Likes, comments                                 |
| Interaction Service    | Friends, followers, following, interaction data |
| Reel Service           | Reels, reel metadata                            |
| Interest Service       | User interests                                  |
| Chat Service           | Chat messages, conversations                    |
| View Service           | No dedicated database                           |
| Reel Fetch Service     | No dedicated database                           |

---

## Kafka Event Architecture

Kafka provides asynchronous communication between services where synchronous communication is not required.

Major event flows include:

### Profile Denormalization

```text
Profile Service
      |
      v
profile-* events
      |
      v
Kafka
      |
      +---- Post Service
      +---- Reel Service
      +---- Likes Service
      +---- Interaction Service
```

Profile changes are propagated asynchronously to services that maintain denormalized profile information.

### Feed Synchronization

```text
Post / Interaction Service
          |
          v
       Outbox
          |
          v
        Kafka
          |
          v
     Feed Service
```

Feed creation and deletion events are published through Kafka using the Outbox Pattern.

### Chat Conversation Synchronization

```text
Interaction Service
        |
        v
      Outbox
        |
        v
      Kafka
        |
        v
    Chat Service
```

Friendship changes generate conversation creation or deletion events.

The Chat Service consumes:

* `conversation-create`
* `conversation-delete`

### Reliability

Kafka event consumers use idempotency where implemented, with Redis used to track processed event IDs for a 24-hour window.

Outbox records are cleaned up after 24 hours.

---

## Feed Architecture

The platform uses a fan-out-on-write approach for post feeds.

When a post is created:

```text
Post Created
     |
     v
Feed Event
     |
     v
Kafka
     |
     v
Feed Service
     |
     v
Feed Entries
```

The Interaction Service maintains relationship data used to determine feed recipients.

This avoids repeatedly aggregating followers and friends during feed generation.

Feed deletion follows a similar asynchronous event flow.

---

## Reel Recommendation Architecture

Reel recommendations use a different approach from post feeds.

Rather than pre-generating a complete feed for every user, recommendations are generated during retrieval using:

* User interests
* Reel metadata
* Popularity signals
* View-related data

The Reel Fetch Service coordinates the required service calls and assembles the response.

This separates recommendation retrieval from reel storage and interaction processing.

---

## Real-Time Chat Architecture

The Chat Service uses WebSockets and STOMP for real-time communication.

Redis maintains WebSocket presence and maps connected users to Chat Service instances.

```text
User A
  |
  v
Chat Instance A
  |
  v
Redis
  |
  | receiver -> Chat Instance B
  v
Chat Instance B
  |
  v
User B
```

Messages are published only to the Redis channel belonging to the instance where the receiving user is connected.

This avoids broadcasting every message to every Chat Service instance.

Chat messages are persisted in MongoDB before being published for real-time delivery.

---

## Observability

The platform uses separate metrics and tracing pipelines.

### Metrics

```text
Spring Boot Actuator
        |
     Micrometer
        |
        v
   Prometheus
        |
        v
     Grafana
```

Prometheus collects service metrics exposed through Actuator, while Grafana provides visualization and dashboards.

### Distributed Tracing

```text
Spring Services
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

The platform uses W3C Trace Context propagation so traces can be correlated across service boundaries.

---

## Technology Stack

| Category                 | Technology              |
| ------------------------ | ----------------------- |
| Language                 | Java                    |
| Framework                | Spring Boot             |
| Database                 | MongoDB                 |
| Object Storage           | Cloudflare R2           |
| Authentication           | JWT                     |
| API Gateway              | Spring Cloud Gateway    |
| Service Communication    | REST / OpenFeign        |
| Async Messaging          | Apache Kafka            |
| Event Reliability        | Outbox Pattern          |
| Distributed Coordination | Redis                   |
| Real-Time Communication  | WebSocket / STOMP       |
| Containerization         | Docker                  |
| Orchestration            | Docker Compose          |
| Build Tool               | Maven                   |
| Metrics                  | Micrometer / Actuator   |
| Monitoring               | Prometheus / Grafana    |
| Distributed Tracing      | OpenTelemetry / Jaeger  |
| Trace Collection         | OpenTelemetry Collector |
| Logging                  | SLF4J                   |

---

## Docker Deployment

The platform is containerized using Docker Compose.

Services share a common Docker network and are configured with resource limits for CPU and memory.

The Compose configuration also includes deployment settings for service replicas, allowing services to be deployed in environments that support Docker Swarm-style scaling.

The Chat Service supports multiple instances using Redis-based instance routing.

Infrastructure services include:

* Redis
* Kafka
* Prometheus
* Grafana
* OpenTelemetry Collector
* Jaeger

---

## Architectural Highlights

* Microservices with domain-based ownership
* API Gateway as the external backend entry point
* Database-per-service architecture
* Kafka-based asynchronous communication
* Outbox Pattern for reliable event publishing
* Redis-based Kafka idempotency
* Fan-out-on-write post feed generation
* Interest-based reel recommendation pipeline
* Horizontally scalable WebSocket chat
* Targeted Redis Pub/Sub for Chat instance routing
* OpenTelemetry distributed tracing
* Prometheus and Grafana monitoring
* Docker-based deployment with resource constraints

---

## Design Trade-Offs

### Interaction Collection

A dedicated interaction collection introduces additional writes when relationships change.

However, it simplifies feed recipient lookup and avoids repeatedly aggregating friend and follower relationships during feed generation.

### Denormalized Data

Selected profile information such as usernames and avatars is stored in downstream relationship and content documents.

This improves read performance and reduces repeated cross-service lookups, at the cost of requiring asynchronous synchronization when profile data changes.

### Asynchronous Communication

Kafka introduces additional infrastructure and eventual consistency for selected workflows.

The benefit is reduced synchronous coupling between services and reliable event-driven propagation through the Outbox Pattern.

### Real-Time Chat Scaling

Redis introduces an additional dependency for cross-instance WebSocket routing.

In return, Chat Service instances can route messages to the specific instance holding the receiving user's active connection instead of broadcasting messages across all instances.

---

## Conclusion

The v2 architecture combines domain-oriented microservices with event-driven communication, centralized gateway handling, horizontally scalable real-time messaging, and a dedicated observability stack.

The architecture separates synchronous APIs from asynchronous workflows where appropriate. Kafka and the Outbox Pattern handle reliable event propagation, Redis provides distributed coordination and Chat instance routing, while OpenTelemetry, Prometheus, Grafana, and Jaeger provide system-wide observability.

The resulting architecture provides a practical foundation for social networking workloads while demonstrating service decomposition, database ownership, event-driven communication, feed fan-out, recommendation pipelines, real-time WebSocket scaling, and distributed observability.# System Architecture

The Social Media Backend is a distributed social media platform

designed to support posts, reels, personalized feeds, user interactions,

and real-time messaging.

The system is implemented using a microservices architecture in which

each service owns a specific business domain and data model. This

approach enables separation of concerns and independent scalability.

The architecture emphasizes feed generation, recommendation systems,

and real-time communication while maintaining resilience against

partial service failures.

The platform uses a hybrid feed architecture where post feeds are generated using a fan-out-on-write strategy, while reel recommendations are generated on read using interest-based ranking and popularity scoring.

Real-time messaging is implemented using WebSockets, with Redis Pub/Sub enabling communication across multiple chat service instances.

## System Goals

- Separation of concerns

- Independent service ownership

- Personalized content delivery

- Real-time messaging

- Fault tolerance

- Horizontal scalability

## High Level Architecture

The platform consists of multiple domain-oriented services.

Each service is responsible for a specific business capability

and owns its corresponding data.

## Service Overview

The platform is composed of domain-oriented services that separate

authentication, content management, recommendations, social interactions,

and real-time communication into independent deployment units.

| Service     | Responsibility          |

|-------------|-------------------------|

| Auth        | Authentication and JWT  |

| Profile     | User profile management |

| Post        | Post storage            |

| Reel        | Reel storage            |

| Feed        | Feed generation         |

| Reel Feed   | Reel retrieval          |

| Interest    | User interests          |

| View        | View tracking           |

| Interaction | Friends and followers   |

| Chat        | Real-time messaging     |

| likes       | likes and comments      |

## Data Ownership

The system follows a database-per-service approach where each service owns and manages its own persistence layer. This design reduces coupling between services, allows independent schema evolution, and improves service autonomy.

Services access external data through service-to-service communication rather than direct database access. This ensures that business rules remain encapsulated within the owning service.

Certain orchestration services such as View Service and Reel Fetch Service do not maintain dedicated persistent storage. Instead, they coordinate operations across other services and aggregate data required by business workflows.

| Service                | Owned Data                            |

| ---------------------- | ------------------------------------- |

| Authentication Service | User Credentials, Authentication Data |

| Profile Service        | User Profiles                         |

| Post Service           | Posts                                 |

| Feed Service           | User Feed Entries                     |

| Likes Service          | Likes, Comments                       |

| Interaction Service    | Friends, Followers, Following         |

| Reel Service           | Reels, Reel Metadata                  |

| Interest Service       | User Interests                        |

| Chat Service           | Chat Messages, Conversations          |

| View Service           | No Dedicated Database                 |

| Reel Fetch Service     | No Dedicated Database                 |

## Technology Stack

The platform is implemented using Java and Spring Boot microservices. MongoDB and PostgreSQL serve as the primary persistence layers, with Supabase providing managed PostgreSQL infrastructure. Cloudflare R2 is used for object storage of static assets such as posts, reels, and profile images. Redis Pub/Sub enables cross-instance communication for horizontally scalable real-time chat functionality. Docker and Docker Compose provide a reproducible deployment environment for local development and testing.

| Category                | Technology           |

|-------------------------|----------------------|

| Language                | Java 17              |

| Framework               | Spring Boot          |

| Database                | MongoDB / Postgres   |

| Object Storage          | Cloudflare R2        |

| Authentication          | JWT                  |

| Service Communication   | REST / OpenFeign     |

| Real-Time Communication | WebSocket            |

| Messaging               | Redis Pub/Sub        |

| Containerization        | Docker               |

| Orchestration           | Docker Compose       |

| Build Tool              | Maven                |

| Resilience              | resilience4j         |

| Logging                 | SLF4J                |

| Metrics                 | Micrometer           |

| Monitoring              | Spring Boot Actuator |

## Architectural Highlights

- Fan-out-on-write feed generation

- Interest-driven reel recommendation pipeline

- Redis Pub/Sub based chat scaling

- Database-per-service ownership model

## Conclusion

The architecture emphasizes separation of concerns through independently deployable microservices with clear ownership boundaries. Each service is responsible for a specific business domain while communicating through well-defined APIs.

The design prioritizes maintainability, scalability, and resilience while remaining deployable on modest infrastructure. Workloads such as feed generation, recommendation processing, view tracking, and real-time communication are isolated into dedicated services, allowing the platform to evolve without introducing unnecessary coupling between domains.

The resulting architecture provides a practical foundation for social networking workloads while demonstrating common distributed systems patterns including service decomposition, database ownership, real-time communication, recommendation pipelines, and fault-tolerant service interactions.
