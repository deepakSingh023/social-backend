# Social Chat Service

## Overview

The Social Chat Service provides real-time messaging between users.

The service uses a conversation-based architecture where every message belongs to a conversation. Users do not communicate directly through user IDs during messaging operations. Instead, a conversation is created when two users become friends, and all future communication happens through the conversation ID.

The service supports:

* Real-time messaging using WebSockets and STOMP
* Horizontal scaling through Redis Pub/Sub
* Chat history persistence
* Media messaging
* Conversation management
* JWT-secured WebSocket connections

---

## Responsibilities

### Conversation Management

The service creates and removes conversations between users.

Conversations are created when users become friends and deleted when friendships are removed.

### Real-Time Messaging

Messages are sent through WebSocket connections and delivered in real time to subscribed clients.

### Message Persistence

Every message is stored in MongoDB before being published.

This ensures messages remain available even if users are offline.

### Media Uploads

The service supports image and video messages.

Media files are compressed before being uploaded to Cloudflare R2 storage.

### Horizontal Scaling

Redis Pub/Sub enables multiple chat service instances to deliver messages regardless of which instance receives the WebSocket request.

---

## Architecture

```text
Client
   |
   v
WebSocket (STOMP)
   |
   v
Chat Service
   |
   +---- Save Message (MongoDB)
   |
   +---- Publish Event (Redis)
                |
                v
        Redis Pub/Sub
                |
        -----------------
        |               |
        v               v
   Chat Instance A   Chat Instance B
        |               |
        +-------+-------+
                |
                v
         WebSocket Clients
```

---

## Conversation-Based Design

The service uses a conversation-first architecture.

When two users become friends:

1. Interaction Service creates a conversation.
2. Conversation ID is stored.
3. All future messages use the conversation ID.
4. Chat history retrieval uses the conversation ID.

This avoids repeated relationship lookups during messaging operations.

```text
User A + User B
       |
       v
Conversation Created
       |
       v
conversationId
       |
       v
All Messages Stored Under Conversation
```

---

## API Endpoints

### Get Conversation ID

Used when opening a chat screen.

```http
GET /api/chat/get-convoId
```

Parameters:

```text
receiverId
```

Response:

```text
conversationId
```

Flow:

```text
Open Chat
    |
    v
Get Conversation ID
    |
    v
Fetch Chat History
```

---

### Get Chat History

Fetches paginated chat messages.

```http
GET /api/chat/get-chat
```

Parameters:

```text
conversationId
page
size
```

Messages are returned sorted by creation time.

---

### Create Conversation

Internal endpoint called by the Interaction Service.

```http
POST /api/conversation/create-conversation
```

Creates a conversation between two users.

---

### Delete Conversation

Internal endpoint called by the Interaction Service.

```http
DELETE /api/conversation/delete-conversation
```

Removes a conversation when a friendship is removed.

---

### Upload Media

```http
POST /api/media/upload
```

Supports:

* Images
* Videos

Flow:

```text
Upload Media
      |
      v
Compress File
      |
      v
Upload To R2
      |
      v
Return Public URL
      |
      v
Send URL Through Socket Message
```

---

## WebSocket Messaging

### Client Sends Message

Destination:

```text
/app/chat.send
```

The Chat Service:

1. Validates authentication.
2. Stores the message.
3. Publishes the event to Redis.
4. Redis distributes the event.
5. Subscribers receive the event.
6. Clients receive the message instantly.

---

### Conversation Subscription

Clients subscribe to:

```text
/topic/conversation/{conversationId}
```

Only messages for that conversation are received.

---

## Redis Pub/Sub

To support multiple chat instances, Redis acts as the message broker.

### Publisher

When a message is saved:

```text
Save Message
      |
      v
Publish To Redis
```

### Subscriber

Every chat instance subscribes to the Redis channel.

When a message arrives:

```text
Redis Event
      |
      v
Chat Subscriber
      |
      v
WebSocket Topic
      |
      v
Connected Clients
```

This allows messages to be delivered even when users are connected to different chat server instances.

---

## Horizontal Scaling

The service was tested using multiple chat instances behind NGINX.

```text
                NGINX
                   |
      -------------------------
      |                       |
      v                       v
 Chat Service A       Chat Service B
      |                       |
      -------- Redis ----------
```

Benefits:

* Load distribution
* Fault tolerance
* Real-time synchronization
* Scalable WebSocket architecture

---

## Media Processing

### Images

Images are compressed before upload.

Benefits:

* Reduced storage usage
* Faster uploads
* Faster delivery

### Videos

Videos are compressed using FFmpeg before upload.

Benefits:

* Reduced bandwidth
* Smaller storage footprint
* Improved client performance

---

## Security

### JWT Authentication

REST APIs use JWT authentication.

Authenticated user information is extracted from the JWT token.

### WebSocket Authentication

WebSocket connections are authenticated during the STOMP CONNECT phase.

The JWT token is validated once when the connection is established.

After authentication, Spring propagates the authenticated principal to all future WebSocket frames.

### Internal APIs

Conversation creation and deletion endpoints are protected using internal service authentication.

These endpoints are intended for service-to-service communication.

---

## Database Design

### Conversation Collection

Stores unique conversations.

```text
Conversation
├── id
├── userId1
├── userId2
└── createdAt
```

A compound unique index prevents duplicate conversations.

---

### Chat Message Collection

Stores message history.

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

Messages are indexed by conversation ID for efficient retrieval.

---

## Observability

The service includes the same observability stack used across the platform.

### Structured Logging

Logs include:

* Controller
* API
* Status
* Latency

Example:

```text
controller=ChatController
api=getChat
status=SUCCESS
latencyMs=21
```

### Distributed Tracing

Each request receives a trace identifier.

Example:

```text
traceId=1d3f45c2-acde-11ee-b9d1-0242ac120002
```

### Metrics

Micrometer metrics are collected for:

* Request count
* Success count
* Error count
* API latency

Example metrics:

```text
http.api.count
http.api.latency
```

Metrics are exposed through Spring Boot Actuator endpoints.

---

## Technology Stack

* Java 21
* Spring Boot
* Spring Security
* Spring WebSocket
* STOMP
* Redis Pub/Sub
* MongoDB
* OpenFeign
* Spring AOP
* Micrometer
* Spring Actuator
* Cloudflare R2
* FFmpeg

---

## Role in System Architecture

The Chat Service is responsible for all real-time communication between users.

```text
Friend Created
      |
      v
Interaction Service
      |
      v
Chat Service
(Create Conversation)
      |
      v
Users Exchange Messages
      |
      v
Redis Pub/Sub
      |
      v
Real-Time Delivery
```

The service provides scalable, persistent, and real-time messaging while remaining independent from the rest of the social platform's content and recommendation systems.
