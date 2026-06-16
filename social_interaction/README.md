# Social Interaction Service

## Overview

The Social Interaction Service manages user relationships across the platform. It is responsible for friendships, followers, follow requests, friend requests, interaction tracking, feed relationship generation, profile denormalization, and cross-service communication with Profile, Post, Counter, and Chat services.

The service acts as the relationship layer of the platform and determines how users are connected to one another. These connections are later used by the Feed Service to distribute content efficiently without repeatedly querying multiple relationship collections.

---

## Responsibilities

The service is responsible for:

* Friend management
* Follower management
* Friend requests
* Follow requests
* Relationship search
* Relationship status checks
* Feed interaction generation
* Profile denormalization
* Friend conversation creation
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

---

### Friend Requests

Represents pending friendship requests.

Stores:

* Sender information
* Receiver information
* Avatar snapshots
* Username snapshots
* Request timestamp

Requests are automatically converted into friendships when accepted.

---

### Followers

Represents a follower relationship.

Stores:

* Follower information
* Followed user information
* Avatar snapshots
* Username snapshots
* Creation timestamp

Used for both public account follows and accepted private account requests.

---

### Follow Requests

Represents pending requests for private accounts.

Stores:

* Sender information
* Receiver information
* Avatar snapshots
* Username snapshots
* Request timestamp

Converted into follower relationships when accepted.

---

### Feed Interaction

The interaction collection acts as a relationship graph optimized for feed generation.

Stores:

* Author user id
* Recipient user id
* Creation timestamp

Instead of repeatedly fetching all followers and friends whenever a post is created, the service maintains a precomputed interaction collection.

This significantly reduces complexity during feed fan-out operations.

---

## Friendship Management

### Send Friend Request

Users can send friendship requests to other users.

Validation includes:

* User existence validation
* Duplicate friendship prevention
* Duplicate request prevention

If a reverse request already exists, the friendship is automatically accepted.

---

### Accept Friend Request

When accepted:

* Friendship document is created
* Friend counters are incremented
* Feed interactions are created for both users
* Chat conversation is automatically created
* Friend request is removed

---

### Reject Friend Request

The pending request is removed without creating a friendship.

---

### Remove Friend

Removing a friendship:

* Deletes the friendship record
* Decrements friend counters
* Removes feed interactions
* Removes associated chat conversations

---

### Friend Search

Supports cursor-based pagination and username filtering.

Features:

* Infinite scrolling support
* Search by username
* Bidirectional friendship lookup

---

## Follow System

### Follow User

For public accounts:

* Follow relationship is created immediately

For private accounts:

* Follow request is created

Validation includes:

* Self-follow prevention
* Duplicate follow prevention
* Duplicate request prevention

---

### Accept Follow Request

When accepted:

* Follower relationship is created
* Counters are updated
* Feed interaction is generated
* Request is removed

---

### Reject Follow Request

Deletes the pending follow request.

---

### Unfollow

Removing a follow:

* Deletes follower record
* Updates counters
* Removes interaction if no other relationship exists

---

### Remove Follower

Users can remove followers manually.

The service updates:

* Follower counters
* Following counters
* Interaction relationships

---

### Follower Search

Supports:

* Followers lookup
* Following lookup
* Username filtering
* Cursor pagination

---

## Interaction Engine

The interaction engine is one of the core architectural components of the service.

### Why It Exists

Without interaction aggregation, generating feeds would require:

1. Fetching all friends
2. Fetching all followers
3. Merging results
4. Removing duplicates
5. Returning the final audience

This becomes increasingly expensive as relationships grow.

Instead, the Interaction Service maintains a dedicated interaction collection.

---

### Interaction Creation

Interactions are created when:

* Users become friends
* Users follow someone
* Follow requests are accepted
* Friend requests are accepted

Example:

User A follows User B

Interaction created:

Author = User B

Recipient = User A

Future posts created by User B can immediately identify User A as a feed recipient.

---

### Interaction Removal

Interactions are removed only when:

* Friendship no longer exists
* Follow relationship no longer exists

This prevents accidental deletion when multiple relationship types still connect two users.

---

## Feed Generation Support

The service exposes internal APIs used by the Feed Service.

### Interaction Fetch API

Returns:

* Recipient users
* Cursor information

Used when a new post is created.

Workflow:

1. Post Service creates post
2. Feed Service requests interactions
3. Interaction Service returns recipients
4. Feed Service fans out the post

This avoids expensive relationship joins during feed generation.

---

## Profile Denormalization

Relationship documents store profile snapshots:

* Username
* Avatar

This avoids profile lookups during reads.

When profile data changes:

1. Profile Service sends denormalization request
2. Interaction Service updates all relationship collections

Updated collections:

* Friends
* Friend Requests
* Followers
* Follow Requests

Denormalization runs asynchronously.

---

## Profile Relationship Checks

The service exposes an internal API used by the Profile Service.

Given:

* Current user
* Target profile

The service returns:

* Is Friend
* Is Following

This allows the Profile Service to render the correct UI state without performing relationship queries itself.

Examples:

* Follow button
* Following button
* Add Friend button
* Friends badge

---

## Chat Service Integration

Friendship creation automatically provisions conversations.

When users become friends:

* Conversation is created asynchronously

When friendship is removed:

* Conversation is deleted asynchronously

This integration is protected using:

* Retry
* Circuit Breaker
* Fallback methods

using Resilience4j.

---

## Counter Service Integration

Relationship events update profile statistics.

Examples:

* Friends count
* Followers count
* Following count

Counter updates are executed asynchronously through dedicated workers.

---

## Feed Service Integration

The service notifies the Feed Service whenever new relationships are created.

Generated interactions allow the Feed Service to distribute future content efficiently.

Operations include:

* Feed creation
* Feed cleanup
* Recipient synchronization

---

## Reliability Features

The service uses Resilience4j for critical cross-service communication.

Features include:

* Retry
* Circuit Breaker
* Fallback methods

Applied to:

* Chat Service integration
* Feed synchronization

Failures are logged without affecting the primary user operation.

---

## Pagination Strategy

Cursor pagination is used throughout the service.

Cursor format:

```text
timestamp_id
```

Benefits:

* Consistent ordering
* No offset performance degradation
* Infinite scrolling support
* Scalable for large datasets

---

## Asynchronous Processing

Several operations execute asynchronously:

* Profile denormalization
* Feed generation
* Feed synchronization
* Counter updates
* Conversation creation
* Conversation deletion
* Interaction creation
* Interaction removal

This keeps relationship operations responsive while allowing background processing.

---

## Observability

The service includes custom observability components:

### Request Tracing

Each request receives a unique trace identifier using MDC.

Enables:

* Request tracking
* Distributed debugging
* Log correlation

---

### Structured Logging

Controller APIs are automatically logged through AOP.

Captured fields include:

* Controller
* API
* Request status
* Latency

---

### Metrics

Micrometer metrics are collected for all controller endpoints.

Metrics include:

* API request count
* Success count
* Error count
* Latency
* Percentiles (P50, P95, P99)

Exposed through Spring Boot Actuator.

---

## Technology Stack

* Java 21
* Spring Boot
* Spring Security
* Spring Data MongoDB
* MongoDB
* Spring AOP
* Micrometer
* Spring Actuator
* Resilience4j
* OpenFeign
* Async Processing
* MDC Tracing

---

## Design Trade-Offs

### Interaction Collection

A dedicated interaction collection introduces additional writes whenever relationships change.

However, it significantly simplifies feed generation and eliminates repeated friend/follower aggregation queries.

The trade-off favors read efficiency and feed scalability.

---

### Denormalized Relationship Data

Usernames and avatars are stored inside relationship documents.

Advantages:

* Faster reads
* Fewer service calls
* Reduced dependency on Profile Service

Trade-off:

* Requires asynchronous denormalization when profile data changes

---

## Summary

The Social Interaction Service serves as the relationship backbone of the platform. It manages friendships, followers, requests, interaction graphs, feed recipient generation, profile denormalization, and chat provisioning.

By maintaining a dedicated interaction graph and denormalized relationship data, the service minimizes expensive cross-service queries and enables efficient feed fan-out operations while remaining resilient through asynchronous processing, retries, circuit breakers, metrics, tracing, and structured logging.
