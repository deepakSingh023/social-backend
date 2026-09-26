# S# Service Communication

## Overview

The backend uses two service communication models:

* **Synchronous communication:** OpenFeign for operations that require an immediate response.
* **Asynchronous communication:** Kafka with the Outbox Pattern for operations that can continue after the original request completes.

The main asynchronous workflows are profile denormalization, feed generation, and Chat conversation synchronization.

---

## 1. Synchronous Communication

OpenFeign is used when a service needs data from another service before it can continue.

Typical uses include:

* Profile and user data retrieval
* Post retrieval
* Feed retrieval and enrichment
* Like information
* User interests
* Recommendation data
* Chat history
* Other immediate service dependencies

```text
Service A
    |
    | OpenFeign
    v
Service B
    |
    v
 Response
```

The calling service waits for the response, so the availability and latency of the downstream service directly affect the operation.

---

## 2. Asynchronous Communication

Kafka is used for workflows that do not need to block the original request.

Current Kafka-based workflows:

* Profile denormalization
* Post → Feed generation
* Interaction → Feed generation
* Conversation creation
* Conversation deletion

```text
Producer Service
      |
      v
   Outbox
      |
      v
    Kafka
      |
      +----> Consumer
      +----> Consumer
      +----> Consumer
```

The producing service stores the event in its Outbox. A scheduled publisher then publishes pending events to Kafka.

---

## 3. Outbox Pattern

The Outbox Pattern is used for the main Kafka-producing workflows.

```text
Business Operation
       |
       v
Service Database
   |          |
   |          +---- Domain Data
   |
   +--------------- Outbox Event
                         |
                         v
                 Scheduled Publisher
                         |
                         v
                       Kafka
                         |
                         v
                    Consumers
```

The business data and its corresponding Outbox event are stored by the producing service before the event is published.

This keeps event creation tied to the business operation instead of relying on a separate network call after the transaction.

Outbox records are cleaned up after the configured 24-hour retention period.

---

## 4. Profile Denormalization

Profile changes are propagated through Kafka to services that maintain denormalized profile information.

```text
                    Profile Service
                          |
                        Outbox
                          |
                          v
                        Kafka
                          |
          +---------------+---------------+
          |               |               |
          v               v               v
     Post Service    Reel Service    Likes Service
          |
          v
 Interaction Service
```

Consumers:

* Post Service
* Reel Service
* Likes Service
* Interaction Service

These services update their local profile data instead of requiring synchronous Profile Service calls for every read.

Where implemented, Redis stores processed event IDs to provide consumer idempotency.

---

## 5. Feed Generation

Feed generation is asynchronous and can be triggered by both Post Service and Interaction Service.

### Post Creation

```text
Post Service
     |
   Outbox
     |
     v
   Kafka
     |
     v
Feed Service
     |
     +----> Interaction Service
     |
     v
Batch Feed Generation
```

When a post is created, Post Service creates a feed event in its Outbox.

Feed Service consumes the event, retrieves the required follower information from Interaction Service, and generates feed entries in batches.

### New Relationship

```text
Interaction Service
        |
      Outbox
        |
        v
      Kafka
        |
        v
   Feed Service
        |
        +----> Post Service
        |
        v
 Batch Feed Generation
```

When a new relationship requires existing posts to be added to a feed, Interaction Service publishes the corresponding event.

Feed Service retrieves the required posts from Post Service and generates the feed entries.

### Feed Dependencies

Feed generation itself is asynchronous, but Feed Service still uses synchronous calls while processing the event.

```text
              Feed Service
               /        \
              v          v
     Interaction      Post Service
       Service
          |               |
       Retry/CB        Retry/CB
```

The calls to Post Service and Interaction Service use Resilience4j Retry and Circuit Breaker mechanisms.

This protects batch feed generation from transient failures and continuously unavailable dependencies.

---

## 6. Conversation Synchronization

Interaction Service publishes conversation changes through Kafka.

```text
Interaction Service
        |
      Outbox
        |
        v
      Kafka
        |
        +---- conversation-create ----> Chat Service
        |
        +---- conversation-delete ----> Chat Service
```

Chat Service consumes these events and updates its conversation data.

Redis event-ID tracking is used where implemented to prevent duplicate event processing.

---

## 7. Synchronous Read Flows

Asynchronous processing is used for background updates, but read operations remain synchronous when the client needs the result immediately.

### Feed Retrieval

```text
Client
  |
  v
Feed Service
  |
  +----> Post Service
  |
  +----> Likes Service
  |
  v
Feed Response
```

Feed Service retrieves the user's feed entries, obtains the corresponding post data and required like information, and assembles the response.

### Recommendation Retrieval

```text
Client
  |
  v
Reel Fetch Service
  |
  +----> Interest Service
  |
  +----> Reel Service
  |
  +----> Likes Service
  |
  v
Recommendations
```

Reel Fetch Service coordinates the required service calls while recommendation and ranking logic remains in Reel Service.

---

## 8. Internal Service Communication

External REST authentication and internal service authentication are handled separately.

```text
Client
  |
 JWT
  v
API Gateway
  |
 Trusted Gateway Headers
  v
REST Service

Service A
    |
    | Internal Authentication
    v
Service B
```

For normal REST traffic, the API Gateway validates the client's JWT and adds trusted headers.

Downstream services validate the expected Gateway or internal-service credentials before processing protected requests.

---

## 9. Event Idempotency

Kafka consumers use Redis to track processed event IDs where idempotency is implemented.

```text
Kafka Event
     |
     v
 Consumer
     |
     v
   Redis
     |
     +---- Already processed? ---> Ignore
     |
     No
     |
     v
 Process Event
     |
     v
 Store Event ID
```

Processed event IDs are retained for the configured 24-hour period.

This prevents duplicate processing during that window.

---

## 10. Communication Architecture

```text
                         Client
                           |
                           v
                      API Gateway
                           |
              +------------+------------+
              |            |            |
              v            v            v
          REST APIs    Feed/Retrieval   Chat
              |            |            |
          OpenFeign    OpenFeign     WebSocket
              |
              |
       Asynchronous Workflows
              |
       +------+--------+---------+
       |               |         |
    Profile           Post   Interaction
       |               |         |
    Outbox           Outbox    Outbox
       |               |         |
       +---------------+---------+
                       |
                      Kafka
                       |
          +------------+------------+
          |            |            |
        Feed         Chat       Profile
                                Consumers
```

---

## 11. Communication Model

| Workflow                        | Mechanism                         |
| ------------------------------- | --------------------------------- |
| Client → Backend                | API Gateway / REST                |
| Service data retrieval          | OpenFeign                         |
| Profile denormalization         | Kafka + Outbox                    |
| Post → Feed generation          | Kafka + Outbox                    |
| Interaction → Feed generation   | Kafka + Outbox                    |
| Interaction → Chat conversation | Kafka + Outbox                    |
| Feed dependency calls           | OpenFeign + Retry/Circuit Breaker |
| Feed retrieval                  | OpenFeign                         |
| Recommendation retrieval        | OpenFeign                         |
| Chat client communication       | WebSocket / STOMP                 |
| Kafka consumer idempotency      | Redis                             |

---

## 12. Consistency Model

The Kafka-based workflows are eventually consistent.

The original operation can complete before the consuming service processes its event.

This applies to:

* Profile denormalization
* Feed generation
* Conversation creation
* Conversation deletion
* Other related asynchronous updates

This behavior is intentional because these operations do not need to block the original client request.

## Summary

The communication model is deliberately mixed:

* **OpenFeign** is used when the current operation needs another service's response.
* **Kafka + Outbox** is used when work can continue independently of the original request.
* **Redis** provides event-id idempotency for applicable Kafka consumers.
* **Resilience4j** protects the synchronous dependencies used during asynchronous feed generation.

The result is synchronous communication for immediate data requirements and asynchronous communication for decoupled background workflows.
