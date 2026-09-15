# Social Interaction Service

## Overview

The Social Interaction Service manages user relationships across the platform. It handles friendships, followers, follow requests, friend requests, interaction tracking, feed relationship generation, profile denormalization, and cross-service communication with Profile, Counter, Feed, and Chat services.

The service acts as the relationship layer of the platform and maintains the interaction data used by the Feed Service for efficient content distribution.

---

## Responsibilities

* Friend management
* Follower management
* Friend requests
* Follow requests
* Relationship search
* Relationship status checks
* Feed interaction generation
* Profile denormalization
* Friend conversation synchronization
* Counter synchronization
* Feed synchronization

---

## Data Model

### Friends

Represents an accepted friendship between two users.

Stores:

* Sender information
* Receiver information
* Avatar snapshots
* Username snapshots
* Acceptance timestamp

Friendships are stored as a single document and queried bidirectionally.

### Friend Requests

Represents pending friendship requests.

Stores:

* Sender information
* Receiver information
* Avatar snapshots
* Username snapshots
* Request timestamp

Requests are converted into friendships when accepted.

### Followers

Represents a follower relationship.

Stores:

* Follower information
* Followed user information
* Avatar snapshots
* Username snapshots
* Creation timestamp

Used for public account follows and accepted private account requests.

### Follow Requests

Represents pending requests for private accounts.

Stores:

* Sender information
* Receiver information
* Avatar snapshots
* Username snapshots
* Request timestamp

Converted into follower relationships when accepted.

### Feed Interaction

The interaction collection acts as a relationship graph optimized for feed generation.

Stores:

* Author user ID
* Recipient user ID
* Creation timestamp

Instead of repeatedly querying friends and followers when content is created, the service maintains precomputed interaction data for feed recipient lookup.

---

## Friendship Management

### Send Friend Request

Users can send friendship requests to other users.

Validation includes:

* User existence
* Duplicate friendship prevention
* Duplicate request prevention

If a reverse request already exists, the friendship is automatically accepted.

### Accept Friend Request

When accepted:

* Friendship is created
* Friend counters are updated
* Feed interactions are created for both users
* Chat conversation creation is published asynchronously
* Friend request is removed

### Reject Friend Request

The pending request is removed without creating a friendship.

### Remove Friend

Removing a friendship:

* Deletes the friendship
* Updates friend counters
* Removes feed interactions
* Publishes the corresponding Chat conversation deletion event

### Friend Search

Supports cursor-based pagination and username filtering.

* Infinite scrolling
* Username search
* Bidirectional friendship lookup

---

## Follow System

### Follow User

For public accounts, the follow relationship is created immediately.

For private accounts, a follow request is created.

Validation includes:

* Self-follow prevention
* Duplicate follow prevention
* Duplicate request prevention

### Accept Follow Request

When accepted:

* Follower relationship is created
* Counters are updated
* Feed interaction is generated
* Request is removed

### Reject Follow Request

Deletes the pending follow request.

### Unfollow

Removing a follow:

* Deletes the follower record
* Updates counters
* Removes the interaction when no other relationship exists

### Remove Follower

Users can remove followers manually.

The service updates:

* Follower counters
* Following counters
* Interaction relationships

### Follower Search

Supports:

* Followers lookup
* Following lookup
* Username filtering
* Cursor pagination

---

## Interaction Engine

The interaction engine maintains the relationship graph used by the platform's feed system.

### Interaction Creation

Interactions are created when:

* Users become friends
* Users follow someone
* Follow requests are accepted
* Friend requests are accepted

Example:

```text
User A follows User B

Author    = User B
Recipient = User A
```

Future posts created by User B can therefore identify User A as a feed recipient without rebuilding the relationship graph.

### Interaction Removal

Interactions are removed only when the corresponding relationship no longer exists.

This prevents accidental deletion when multiple relationship types still connect two users.

---

## Feed Generation Support

The service exposes internal APIs used by the Feed Service to retrieve relationship-based recipients.

### Interaction Fetch API

Returns:

* Recipient users
* Cursor information

Used during feed generation when the Feed Service needs to determine the recipients for content.

---

## Profile Denormalization

Relationship documents contain profile snapshots such as:

* Username
* Avatar

This avoids additional Profile Service lookups during relationship reads.

When profile data changes, Profile Service publishes a profile event to Kafka.

### Topic

```text
profile-interaction-events
```

The Interaction Service consumes the event and updates the relevant relationship collections:

* Friends
* Friend Requests
* Followers
* Follow Requests

Processed event IDs are stored in Redis with a **24-hour TTL** to provide idempotent event processing.

---

## Profile Relationship Checks

The service exposes an internal API used by the Profile Service.

Given a current user and target profile, it returns relationship information such as:

* Is Friend
* Is Following

This allows the Profile Service to render the appropriate relationship state.

---

## Chat Service Integration

Friendship changes are synchronized with the Chat Service asynchronously through Kafka.

When users become friends:

* A conversation creation event is written to the Outbox
* The event is published to Kafka
* Chat Service consumes the event

When a friendship is removed:

* A conversation deletion event is written to the Outbox
* The event is published to Kafka
* Chat Service consumes the event

### Topics

```text
conversation-create
conversation-delete
```

The Outbox Pattern ensures that the event is persisted before it is published.

---

## Counter Service Integration

Relationship changes update profile statistics such as:

* Friends count
* Followers count
* Following count

Counter synchronization is handled asynchronously through the service's existing event/worker flow.

---

## Feed Service Integration

Feed relationship synchronization is handled asynchronously through Kafka.

When relationships change, the service generates feed interaction events for creation or deletion.

### Topics

```text
create-feed-interaction
delete-feed-interaction
```

The events are persisted through the Outbox Pattern before publication.

Feed Service consumes these events and updates the corresponding feed interaction data.

---

## Kafka & Outbox

The service uses the **Outbox Pattern** for reliable asynchronous event publishing.

Events are first persisted in the service database and then published to Kafka by the scheduled publisher.

### Produced Events

| Topic                     | Purpose                   |
| ------------------------- | ------------------------- |
| `create-feed-interaction` | Create feed interactions  |
| `delete-feed-interaction` | Delete feed interactions  |
| `conversation-create`     | Create Chat conversations |
| `conversation-delete`     | Delete Chat conversations |

### Consumed Events

| Topic                        | Purpose                 |
| ---------------------------- | ----------------------- |
| `profile-interaction-events` | Profile denormalization |

Outbox records are cleaned up after **24 hours**.

Redis stores processed Kafka event IDs for **24-hour idempotency**.

---

## Pagination

Cursor-based pagination is used throughout the service.

Cursor format:

```text
timestamp_id
```

Benefits:

* Consistent ordering
* No offset performance degradation
* Infinite scrolling support
* Suitable for large datasets

---

## Asynchronous Processing

The service uses asynchronous processing for operations such as:

* Profile denormalization
* Feed synchronization
* Counter updates
* Conversation creation
* Conversation deletion
* Kafka event processing

Kafka-based operations use the Outbox Pattern and Redis idempotency where applicable.

---

## Observability

### Distributed Tracing

The service uses OpenTelemetry for distributed tracing.

```text
Service
   │
   ▼
OpenTelemetry
   │
   ▼
OpenTelemetry Collector
   │
   ▼
Jaeger
```

W3C trace context is propagated across service boundaries.

### Metrics

Application and HTTP metrics are exposed through Spring Boot Actuator and Micrometer.

```text
Actuator + Micrometer
        │
        ▼
    Prometheus
        │
        ▼
     Grafana
```

Custom AOP metrics are also used for method-level measurements.

---

## Technology Stack

* Java 17
* Spring Boot
* Spring Data MongoDB
* Redis
* Apache Kafka
* Kafka Outbox Pattern
* Spring AOP
* Spring Boot Actuator
* OpenFeign
* OpenTelemetry
* Prometheus
* Docker

---

## Design Trade-Offs

### Interaction Collection

A dedicated interaction collection introduces additional writes when relationships change.

However, it simplifies feed recipient lookup and avoids repeatedly aggregating friend and follower relationships during feed generation.

### Denormalized Relationship Data

Usernames and avatars are stored inside relationship documents.

Advantages:

* Faster reads
* Fewer cross-service lookups
* Reduced dependency on Profile Service

Trade-off:

* Profile changes require asynchronous denormalization through Kafka.

---

## Summary

The Social Interaction Service provides the relationship layer of the platform, managing friendships, followers, requests, interaction graphs, feed relationships, profile denormalization, and Chat synchronization.

The service uses **Kafka and the Outbox Pattern** for reliable asynchronous Feed and Chat synchronization, with **Redis-based idempotency** for Kafka consumers. Observability is provided through **OpenTelemetry, Jaeger, Micrometer, Prometheus, and Grafana**.
