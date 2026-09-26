# Feed Generation Architecture

## Overview

The platform uses a fan-out-on-write strategy for post feed generation.

Feed entries are generated when a post is created and when a new relationship requires existing posts to be added to a user's feed.

Feed generation is now triggered asynchronously through Kafka. Post Service and Interaction Service write feed events to their Outbox, and those events are published to Kafka for processing by Feed Service.

The actual feed generation work remains inside Feed Service, where followers, relationships, and posts are processed in batches.

---

## Why Fan-Out On Write

Two common approaches to feed generation are fan-out-on-read and fan-out-on-write.

### Fan-Out On Read

The system collects posts from relevant users when the feed is requested and constructs the feed at read time.

Advantages:

* Lower write cost
* No feed-entry duplication

Disadvantages:

* Higher read latency
* More work during feed retrieval
* Higher database load for users with many relationships

### Fan-Out On Write

Feed entries are generated before the feed is requested.

Advantages:

* Faster feed retrieval
* Predictable read workload
* Feed queries operate on already-generated entries

Disadvantages:

* Higher write-side processing
* Additional feed-entry storage
* Large fan-out for users with many followers

The platform uses fan-out-on-write because feed generation is separated from feed retrieval, allowing the read path to remain simple.

---

## Feed Creation Through Kafka

Feed creation is triggered by events from Post Service and Interaction Service.

```text
                         +------------------+
                         |   Post Service   |
                         +--------+---------+
                                  |
                                Outbox
                                  |
                                  v
                                Kafka
                                  |
                                  v
                            Feed Service
                                  ^
                                  |
                                Kafka
                                  |
                                Outbox
                                  |
                         +--------+---------+
                         | Interaction     |
                         | Service         |
                         +-----------------+
```

Post Service publishes events for post creation and deletion.

Interaction Service publishes events when relationships change and feed entries need to be created or removed.

This removes the need for Post Service and Interaction Service to synchronously perform feed writes.

---

## Post Creation Workflow

When a user creates a post:

1. Post Service stores the post.
2. Post Service creates a feed event in its Outbox.
3. The Outbox publisher sends the event to Kafka.
4. Feed Service consumes the event.
5. Feed Service requests the required relationship information from Interaction Service.
6. The relationship data is processed in batches.
7. Feed entries are generated for the relevant users.
8. Feed entries are stored in Feed Service.

```text
User Creates Post
       |
       v
Post Service
       |
       +---- Post
       |
       +---- Outbox
              |
              v
            Kafka
              |
              v
        Feed Service
              |
              v
     Interaction Service
              |
              v
       Followers / Users
              |
              v
        Feed Entries
```

---

## Relationship Creation Workflow

A similar process is used when a new relationship requires existing posts to be added to a user's feed.

When a friendship or follow relationship is created:

1. Interaction Service stores the relationship.
2. Interaction Service creates a feed event in its Outbox.
3. The event is published to Kafka.
4. Feed Service consumes the event.
5. Feed Service requests the required post information from Post Service.
6. Posts are processed in batches.
7. Feed entries are generated.
8. Generated feed records are stored in Feed Service.

```text
Relationship Created
        |
        v
Interaction Service
        |
        +---- Relationship
        |
        +---- Outbox
               |
               v
             Kafka
               |
               v
         Feed Service
               |
               v
          Post Service
               |
               v
        Existing Posts
               |
               v
         Feed Entries
```

---

## Batch Processing

Feed generation is performed in batches rather than loading all followers or posts into memory at once.

For post creation, Feed Service obtains the relevant followers through Interaction Service and processes them in batches.

For relationship creation, Feed Service obtains the required posts from Post Service and processes them in batches.

This keeps memory usage bounded and allows the same generation logic to handle larger result sets.

---

## Service-to-Service Resilience

Although feed creation is triggered asynchronously through Kafka, Feed Service still performs synchronous calls to Post Service and Interaction Service during the actual feed-generation process.

These calls use Resilience4j Retry and Circuit Breaker mechanisms.

```text
Feed Service
     |
     +----> Interaction Service
     |          |
     |       Retry /
     |      Circuit Breaker
     |
     +----> Post Service
                |
             Retry /
            Circuit Breaker
```

Retry handles transient failures when fetching the required data.

Circuit Breaker prevents repeated calls to an unavailable dependency from continuously consuming Feed Service resources.

This allows Kafka to decouple the event that starts feed generation while the required data-fetching operations remain synchronous.

---

## Feed Retrieval Workflow

When a user requests their feed:

1. Feed Service retrieves the user's feed entries.
2. Post identifiers are collected from those entries.
3. Post Service is queried for the associated post data.
4. Feed Service assembles the feed response.
5. Likes are fetched from Likes Service where required.
6. The completed feed is returned to the user.

```text
Client
  |
  v
Feed Service
  |
  +---- Feed Entries
  |
  +----> Post Service
  |
  +----> Likes Service
  |
  v
Feed Response
```

Feed construction therefore does not require rebuilding the user's entire relationship graph during every feed request.

Feed entries contain references to posts rather than duplicating post content. Post data remains owned by Post Service.

---

## Feed Service Responsibilities

Feed Service is responsible for:

* Feed generation
* Feed entry storage
* Feed retrieval
* Feed pagination
* Processing Kafka feed events

It does not own post content.

Post content remains owned by Post Service, while relationship data remains owned by Interaction Service.

---

## Scalability

Feed generation is separated from Post Service and Interaction Service.

Kafka allows feed-generation work to be processed independently from the request that created the post or relationship.

Feed Service can also process follower and post data in batches, while Retry and Circuit Breaker protect its synchronous dependencies.

The resulting flow separates the initial write from the potentially larger feed-generation workload:

```text
Post / Relationship Write
          |
          v
        Outbox
          |
          v
        Kafka
          |
          v
    Feed Generation
          |
          v
     Batch Processing
          |
          v
      Feed Entries
```

---

## Current Trade-Offs

### Advantages

* Feed retrieval remains lightweight.
* Feed generation is decoupled from Post and Interaction write requests.
* Kafka provides asynchronous processing.
* Batch processing limits the amount of data handled at once.
* Retry handles transient dependency failures.
* Circuit Breaker limits repeated calls to unavailable dependencies.
* Post content remains owned by Post Service.

### Limitations

* Feed generation still requires synchronous calls to Post Service and Interaction Service.
* Large follower counts can increase the amount of fan-out work.
* Feed entries consume additional storage.
* Kafka introduces eventual consistency between the original write and the generated feed.

---

## Current Architecture

```text
                         Post Created
                              |
                              v
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
                     +--------+--------+
                     |                 |
                  Retry/CB          Batch
                     |                 |
                     v                 v
             Interaction Service   Followers
                     
                     
                    Relationship Created
                              |
                              v
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
                           Retry/CB
                              |
                              v
                        Post Service
                              |
                           Batches
                              |
                              v
                         Feed Entries
```

The feed architecture therefore combines **fan-out-on-write, Kafka-based event processing, batch generation, and synchronous protected data retrieval**.

This keeps the user-facing feed read path separate from the heavier feed-generation work.
