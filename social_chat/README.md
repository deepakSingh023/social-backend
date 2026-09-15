# Social Chat Service

## Overview

The Social Chat Service provides real-time messaging between users using WebSockets and STOMP.

Messaging is conversation-based. Conversations are created when users become friends, and messages are stored and exchanged using the conversation ID.

The service supports:

* Real-time messaging with WebSockets and STOMP
* Persistent chat history
* Conversation management
* Image and video messages
* Redis-based instance routing for horizontally scaled WebSocket connections
* JWT authentication for WebSocket connections

---

## Responsibilities

* Conversation creation and deletion
* Real-time message delivery
* Chat history persistence
* Media upload and processing
* WebSocket authentication
* Cross-instance message routing
* Conversation state management

---

## Architecture

```text
                         API Gateway
                              |
                    REST /api/chat/**
                              |
                              v
                     Chat Service Instance
                              |
              +---------------+---------------+
              |               |               |
              v               v               v
           MongoDB          Redis          Cloudflare R2
          Chat Data       Presence &        Media
                           Routing
                              |
                              v
                         Redis Pub/Sub
                              |
                    Target Chat Instance
                              |
                              v
                     WebSocket Client
```

WebSocket connections connect directly to the Chat Service. JWT authentication is performed by the service during the STOMP `CONNECT` phase.

---

## Conversation-Based Messaging

Each message belongs to a conversation.

When two users become friends, the Interaction Service publishes a conversation creation event. The Chat Service consumes the event and creates the conversation.

When the friendship is removed, the corresponding conversation deletion event is consumed.

All subsequent messaging operations use the `conversationId`.

```text
Friendship
    |
    v
Conversation
    |
    v
conversationId
    |
    v
Chat Messages
```

This keeps messaging independent of repeated relationship lookups.

---

## Conversation Synchronization

Conversation creation and deletion are handled through Kafka.

### Consumed Topics

| Topic                 | Purpose               |
| --------------------- | --------------------- |
| `conversation-create` | Create a conversation |
| `conversation-delete` | Delete a conversation |

The Interaction Service persists these events through its Outbox before publishing them to Kafka.

The Chat Service uses Redis-based event-idempotency with a **24-hour TTL** to prevent duplicate processing.

---

## REST API

### Get Conversation ID

```http
GET /api/chat/get-convoId
```

Used when opening a conversation.

Parameter:

```text
receiverId
```

Returns the corresponding `conversationId`.

### Get Chat History

```http
GET /api/chat/get-chat
```

Parameters:

```text
conversationId
page
size
```

Returns paginated messages for the conversation.

### Create Conversation

```http
POST /api/conversation/create-conversation
```

Internal service endpoint used for conversation creation.

### Delete Conversation

```http
DELETE /api/conversation/delete-conversation
```

Internal service endpoint used when a friendship is removed.

### Upload Media

```http
POST /api/media/upload
```

Supports image and video uploads.

Media is compressed before being uploaded to Cloudflare R2.

---

## WebSocket Messaging

### Connection

```text
/ws
```

The endpoint uses STOMP over WebSocket with SockJS support.

### Authentication

WebSocket connections are not authenticated by the API Gateway.

The `JwtChannelInterceptor` validates the JWT during the STOMP `CONNECT` frame.

After successful authentication, the authenticated user is stored as the WebSocket session principal and is reused for subsequent frames.

### Send Message

```text
/app/chat.send
```

Messages are persisted in MongoDB before being published through Redis.

### Subscribe to Conversation

```text
/topic/conversation/{conversationId}
```

Clients receive messages for the subscribed conversation.

---

## Redis Instance Routing

Redis is used to track active WebSocket connections and their Chat Service instance.

When a user connects:

```text
User
  |
  v
WebSocket CONNECT
  |
  v
Redis
  |
  +-- User:{userId} -> instance
  +-- SessionId:{sessionId} -> userId
```

The service also tracks the users connected to a conversation.

When a user disconnects, the corresponding session and presence information is removed from Redis.

---

## Targeted Redis Pub/Sub

Messages are routed only to the Chat Service instance where the receiving user is connected.

```text
Sender
  |
  v
Chat Instance A
  |
  +-- Find receiver from conversation
  |
  +-- Find receiver's instance in Redis
  |
  v
Redis Channel
chat-channel:{receiverInstance}
  |
  v
Chat Instance B
  |
  v
/topic/conversation/{conversationId}
  |
  v
Receiver
```

Each Chat Service instance subscribes only to its own Redis channel.

This avoids broadcasting every message to every Chat Service instance and makes Redis Pub/Sub suitable for horizontally scaled WebSocket connections.

---

## Horizontal Scaling

Multiple Chat Service instances can run simultaneously.

Redis maintains the mapping between users and the instance holding their active WebSocket connection.

```text
                Chat Instances
             +-------------------+
             |                   |
             v                   v
        Instance A          Instance B
             |                   |
             +-------- Redis ----+
```

A message is published only to the instance responsible for the receiving user.

---

## Media Processing

The service supports image and video messages.

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
Public Media URL
```

* Images are compressed before upload.
* Videos are compressed using FFmpeg.
* The resulting media URL is sent as part of the chat message.

---

## Security

### REST APIs

REST requests pass through the API Gateway.

The Chat Service does not perform JWT authentication or CORS handling for these API requests.

Requests reaching the service through the Gateway are validated using the `GatewayHeaderFilter`.

Internal service requests are protected by the `InternalFilter`.

### WebSockets

WebSocket connections bypass the API Gateway filters and are authenticated directly by the Chat Service through the `JwtChannelInterceptor`.

The JWT is validated during STOMP `CONNECT`.

---

## Database

### Conversation

Stores the users participating in a conversation.

```text
Conversation
├── id
├── userId1
├── userId2
└── createdAt
```

A compound unique index prevents duplicate conversations.

### Chat Message

Stores persistent message history.

```text
ChatMessage
├── id
├── messageId
├── senderId
├── conversationId
├── type
├── content
├── createdAt
└── delivered
```

Messages are indexed by `conversationId` for efficient history retrieval.

---

## Observability

### Metrics

The service exposes metrics through **Spring Boot Actuator and Micrometer**, which are collected by Prometheus.

Custom method-level metrics track:

* Request count
* Success/error status
* Method latency
* P50, P95 and P99 percentiles

### Distributed Tracing

Tracing uses:

```text
OpenTelemetry
      |
      v
OTel Collector
      |
      v
Jaeger
```

W3C Trace Context propagation is used across service boundaries.

---

## Technology Stack

* Java 21
* Spring Boot
* Spring Security
* Spring WebSocket
* STOMP
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
