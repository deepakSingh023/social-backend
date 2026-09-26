# Chat Service Architecture

## Overview

The Chat Service provides real-time messaging using STOMP over WebSocket connections.

The service uses a conversation-based messaging model where messages belong to a conversation rather than being sent directly between user IDs. Conversations are created when users become friends and are synchronized asynchronously through Kafka.

The service supports persistent chat history, media messages, conversation management, WebSocket authentication, and horizontal scaling.

Redis is used for WebSocket presence and targeted cross-instance message routing. Instead of broadcasting messages to every Chat Service instance, the service identifies the instance where the receiving user is connected and publishes the message only to that instance.

---

## Design Goals

* Real-time communication
* Low-latency message delivery
* Conversation-based messaging
* Persistent message storage
* Horizontal scalability
* Targeted cross-instance routing
* Independent deployment and scaling
* Asynchronous conversation synchronization

---

## Why a Dedicated Chat Service

Messaging workloads have different requirements from traditional REST services.

The Chat Service handles:

* Persistent WebSocket connections
* STOMP messaging
* Conversation management
* Chat history persistence
* WebSocket authentication
* Cross-instance message routing
* Media message support

Keeping these workloads separate prevents persistent WebSocket connections and real-time traffic from directly affecting content-oriented services such as Posts, Reels, and Feed generation.

---

## High-Level Architecture

The Chat Service consists of:

* WebSocket/STOMP clients
* Chat Service instances
* Redis
* MongoDB
* Kafka
* Interaction Service
* Cloudflare R2

REST requests enter through the API Gateway. WebSocket connections use the Chat Service directly and are authenticated during the STOMP `CONNECT` phase.

```text
                         Client
                           |
             +-------------+-------------+
             |                           |
          REST API                   WebSocket
             |                           |
             v                           v
        API Gateway                Chat Service
             |                    Instance A / B
             |                           |
             v                           |
       Chat REST APIs                    |
                                         |
                    +--------------------+
                    |
                    v
                  Redis
                    |
          Targeted instance channel
                    |
                    v
             Receiving Instance
                    |
                    v
             WebSocket Client

MongoDB  <---- Chat messages
Kafka    <---- Conversation events
R2       <---- Media files
```

---

## Conversation Model

The Chat Service uses a conversation-based architecture.

```json
{
  "_id": "6a2bb8165205d77026ae8c5d",
  "userId1": "1c50475c-c188-4839-99ac-aa27e40630de",
  "userId2": "3e5046eb-3c66-474d-9a10-937160576122",
  "createdAt": "2026-06-12T07:41:10.602Z"
}
```

Each conversation represents the communication channel between two users.

Messages reference the `conversationId` rather than directly identifying the recipient. This provides a stable key for message persistence and history retrieval.

---

## Conversation Synchronization

Conversation creation and deletion are handled asynchronously through Kafka.

When a friendship is created:

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
        |
        v
 Create Conversation
```

When the friendship is removed, the corresponding deletion event follows the same flow.

### Kafka Topics

```text
conversation-create
conversation-delete
```

The Chat Service uses Redis to track processed event IDs for **24 hours**, preventing duplicate processing of the same Kafka event.

This removes the need for synchronous conversation creation/deletion calls between Interaction Service and Chat Service.

---

## Conversation Retrieval

When a user opens a chat:

```text
Open Chat
    |
    v
Get Conversation ID
    |
    v
Load Chat History
    |
    v
Connect / Use WebSocket
    |
    v
Exchange Messages
```

The client obtains the conversation ID using the other participant's user ID.

Once the conversation is identified, message retrieval and messaging use the `conversationId`.

---

## Message Model

Messages are stored independently from conversations and reference the conversation through `conversationId`.

```json
{
  "messageId": "d1fd13a7-f4bf-4925-8f81-5b8adfdededd",
  "senderId": "1c50475c-c188-4839-99ac-aa27e40630de",
  "conversationId": "6a2bb8165205d77026ae8c5d",
  "type": "TEXT",
  "content": "hi",
  "createdAt": "2026-06-12T08:38:10.553Z",
  "delivered": false
}
```

Supported message types include:

* `TEXT`
* `IMAGE`
* `VIDEO`

MongoDB is the source of truth for persisted messages.

---

## Message Delivery

When a user sends a message:

```text
Client
  |
  v
STOMP SEND
  |
  v
Chat Service
  |
  +---- Persist Message ---> MongoDB
  |
  v
Redis Publisher
  |
  v
Find Receiver Instance
  |
  v
Targeted Redis Channel
  |
  v
Receiver's Chat Instance
  |
  v
STOMP Topic
  |
  v
Receiver
```

The message is persisted before it is published for real-time delivery.

Redis is used only as the transient cross-instance transport layer.

---

## WebSocket Authentication

WebSocket connections are authenticated directly by the Chat Service.

The WebSocket endpoint is:

```text
/ws
```

During the STOMP `CONNECT` frame:

1. The client sends its JWT in the `Authorization` header.
2. `JwtChannelInterceptor` validates the token.
3. The user ID is extracted from the JWT.
4. The authenticated user is assigned as the WebSocket `Principal`.
5. Subsequent STOMP frames inherit that authenticated principal.

The JWT is therefore validated once during connection establishment rather than for every WebSocket frame.

---

## WebSocket Routing

Clients send messages through:

```text
/app/chat.send
```

Conversation subscriptions use:

```text
/topic/conversation/{conversationId}
```

The Chat Service uses the STOMP broker to deliver messages to clients connected to the corresponding conversation.

---

## Redis Presence and Instance Routing

Redis maintains the relationship between connected users and Chat Service instances.

When a user connects:

```text
WebSocket CONNECT
       |
       v
Authenticated User
       |
       v
Redis
       |
       +---- User:{userId} -> instance
       |
       +---- SessionId:{sessionId} -> userId
```

The service also tracks users associated with active conversations.

When the WebSocket disconnects, the corresponding session and user routing information is removed from Redis.

---

## Targeted Redis Pub/Sub

The current architecture uses **instance-targeted Redis Pub/Sub**.

A message is not broadcast to every Chat Service instance.

Instead:

```text
Sender
  |
  v
Chat Instance A
  |
  v
Conversation Members
  |
  v
Find Receiver
  |
  v
Redis User:{receiverId}
  |
  v
Receiver Instance B
  |
  v
chat-channel:B
  |
  v
Chat Instance B
  |
  v
WebSocket
  |
  v
Receiver
```

Each Chat Service instance subscribes to its own channel:

```text
chat-channel:{instanceId}
```

This reduces unnecessary message propagation as the number of Chat Service instances increases.

---

## Horizontal Scaling

Multiple Chat Service instances can run simultaneously.

```text
                    Load Balancer / Gateway
                             |
               +-------------+-------------+
               |                           |
               v                           v
        Chat Instance A             Chat Instance B
               |                           |
               +-------------+-------------+
                             |
                           Redis
```

Redis maintains the user-to-instance mapping required for targeted message delivery.

For example:

```text
User A -> chat-instance-1
User B -> chat-instance-2
```

If User A sends a message to User B, the publisher resolves User B's instance and publishes only to:

```text
chat-channel:chat-instance-2
```

The architecture therefore allows WebSocket connections to be distributed across multiple Chat Service instances without requiring both users to connect to the same instance.

---

## Media Messages

Media is not transmitted through the WebSocket connection.

The client first uploads the media through the Chat Service:

```text
Client
  |
  v
Media Upload
  |
  v
Compression
  |
  v
Cloudflare R2
  |
  v
Public URL
  |
  v
STOMP Message
  |
  v
MongoDB + Recipient
```

Images are compressed before upload.

Videos are compressed using FFmpeg.

The resulting R2 URL is stored as message content and delivered through the normal messaging pipeline.

---

## REST Security

REST APIs such as conversation lookup, chat history, and media upload pass through the API Gateway.

The Chat Service does not perform JWT authentication or CORS handling for these REST requests.

The service uses:

* `GatewayHeaderFilter` for requests coming through the Gateway
* `InternalFilter` for protected internal service communication

The Gateway handles external JWT validation, routing, CORS, and rate limiting.

---

## Message Persistence and Ordering

Messages are persisted in MongoDB before being published to Redis.

This provides:

* Persistent chat history
* Recovery after service restart
* Conversation reconstruction
* Historical message retrieval

Redis Pub/Sub is used only for real-time transport and does not provide message durability.

Chat history is retrieved using the conversation ID and ordered using persisted message timestamps.

The current implementation does not use a dedicated distributed message-ordering mechanism.

---

## Observability

### Metrics

The service exposes metrics through Spring Boot Actuator and Micrometer.

Prometheus collects the exposed metrics and Grafana is used for visualization.

Custom AOP metrics measure service-method latency and execution status.

```text
Service
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

### Distributed Tracing

Tracing uses OpenTelemetry with W3C Trace Context propagation.

```text
Chat Service
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

---

## Current Trade-Offs

### Redis Pub/Sub

Redis provides low-latency cross-instance message transport but does not provide durable message delivery.

MongoDB remains the source of truth for chat history.

### Instance Routing

Targeted instance routing requires maintaining user presence and instance information in Redis.

This adds Redis state but avoids broadcasting every message to every Chat Service instance.

### Two-User Conversations

The current conversation model is designed for two users. Group conversations are not part of the current implementation.

### Delivery State

The current message model contains a `delivered` field, but the architecture does not implement a complete distributed delivery-acknowledgement system.

---

## Technology Stack

* Java 21
* Spring Boot
* Spring WebSocket
* STOMP
* Spring Security
* MongoDB
* Redis
* Apache Kafka
* OpenFeign
* Cloudflare R2
* FFmpeg
* Spring AOP
* Micrometer
* Spring Boot Actuator
* Prometheus
* OpenTelemetry
* Jaeger
* Docker

---

## Conclusion

The Chat Service provides the platform's real-time communication layer using conversation-based messaging, persistent MongoDB storage, STOMP over WebSockets, and Redis-based cross-instance routing.

Kafka and the Outbox Pattern handle asynchronous conversation lifecycle events, while Redis maintains WebSocket presence and routes messages to the specific Chat Service instance hosting the receiving user.

This architecture separates persistent message storage from transient real-time transport while allowing Chat Service instances to scale horizontally without requiring users in the same conversation to share the same server instance.
