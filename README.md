# Social Media Platform Backend

## Overview

A distributed microservices-based social media backend built with Spring Boot.

The platform supports:

* Authentication & JWT Authorization
* User Profiles
* Posts
* Reels
* Likes & Comments
* Friends & Followers
* Personalized Reel Recommendations
* Fan-Out-On-Write Feed Generation
* Real-Time Chat
* Media Uploads
* Interest-Based Recommendation System
* Distributed Observability

The system is designed around independently deployable services communicating through REST APIs using OpenFeign.

---

# Architecture

```text
Client
   |
   v
-------------------------------------------------
|                   Backend                      |
-------------------------------------------------

Auth Service
      |
      v
Profile Service
      |
      +---------------------------+
      |                           |
      v                           v
Post Service                Reel Service
      |                           |
      |                           |
      v                           v
Feed Service                View Service
      |                           |
      |                           v
      |                    Interest Service
      |                           |
      +-------------+-------------+
                    |
                    v
             Reel Fetch Service

Likes & Comments Service
           |
           v
     Post/Reel Updates

Interaction Service
           |
           +---- Friends
           |
           +---- Followers
           |
           +---- Feed Relationships
           |
           +---- Chat Conversations

Chat Service
      |
      +---- Redis Pub/Sub
      |
      +---- WebSocket Messaging
      |
      +---- Cloudflare R2 Media
```

---

# Services

## Auth Service

Responsible for:

* Registration
* Login
* JWT Generation
* Authentication

### Features

* Stateless authentication
* JWT-based security
* Profile creation on signup
* Password encryption

---

## Profile Service

Responsible for:

* User profile management
* Profile updates
* User search
* Counter management

### Maintains

* Avatar
* Username
* Bio
* Followers count
* Following count
* Friends count
* Post count
* Reel count

---

## Post Service

Responsible for:

* Post creation
* Post deletion
* Post retrieval
* Media management

### Features

* Image uploads
* Video uploads
* Cloudflare R2 integration
* Denormalized counters

---

## Likes & Comments Service

Responsible for:

* Post likes
* Reel likes
* Comment creation
* Comment replies

### Features

* Nested comments
* Like tracking
* Like status lookup
* Counter denormalization

---

## Reel Service

Responsible for:

* Reel creation
* Reel retrieval
* Popularity scoring
* Semantic tagging

### Features

* Video uploads
* Popularity ranking
* Semantic tag management
* Personalized reel generation

---

## View Service

Responsible for:

* View processing
* Interest event generation

### Features

* Reel view tracking
* Like interest tracking
* Interest pipeline orchestration

---

## Interest Service

Responsible for:

* User interest storage
* Interest score calculation
* Interest decay

### Features

* Semantic tag tracking
* Time-based decay
* Personalized recommendation signals

---

## Reel Fetch Service

Responsible for:

* Personalized reel feed generation

### Flow

1. Fetch user interests from Interest Service.
2. Request reels from Reel Service.
3. Return personalized feed.

Acts as an orchestration layer.

---

## Interaction Service

Responsible for:

* Friend relationships
* Follower relationships
* Follow requests
* Friend requests
* Feed interaction management

### Features

* Social graph management
* Feed relationship generation
* Conversation creation triggers

---

## Feed Service

Responsible for:

* Home feed generation

### Architecture

Fan-Out-On-Write

When a post is created:

1. Feed Service fetches all recipients.
2. Feed entries are generated.
3. Feed is precomputed for fast reads.

### Features

* Cursor pagination
* Feed generation
* Feed cleanup
* Like status integration

---

## Chat Service

Responsible for:

* Real-time messaging
* Conversation management
* Media messages

### Features

* WebSocket communication
* STOMP messaging
* Redis Pub/Sub
* Media compression
* Cloudflare R2 uploads

---

# Feed Architectures

## Post Feed

Uses Fan-Out-On-Write.

```text
Create Post
      |
      v
Feed Service
      |
      v
Generate Feed Records
      |
      v
Fast Feed Reads
```

Advantages:

* Fast reads
* Precomputed timelines

---

## Reel Feed

Uses Fan-Out-On-Read.

```text
Request Feed
      |
      v
Fetch Interests
      |
      v
Generate Recommendations
      |
      v
Return Reels
```

Advantages:

* Dynamic recommendations
* Interest-based ranking
* Personalized content

---

# Recommendation Pipeline

```text
User Watches Reel
        |
        v
View Service
        |
        v
Reel Service
(Update Popularity + Fetch Semantic Tags)
        |
        v
Interest Service
(Update User Interests)
        |
        v
Reel Fetch Service
        |
        v
Fetch Interests
        |
        v
Reel Service
        |
        v
Personalized Reel Feed
```

---

# Real-Time Chat Architecture

```text
Client
   |
WebSocket
   |
Chat Service
   |
MongoDB
   |
Redis Pub/Sub
   |
---------------------
|                   |
v                   v
Chat Instance A   Chat Instance B
```

### Conversation-Based Design

All messages belong to a conversation.

```text
Friend Accepted
      |
      v
Create Conversation
      |
      v
conversationId
      |
      v
Messaging
```

---

# Scalability

The platform is designed around stateless microservices.

Most services can be horizontally scaled by running multiple instances behind a load balancer because application state is stored in external systems such as:

* MongoDB
* Redis
* Cloudflare R2

The Chat Service supports horizontal scaling through Redis Pub/Sub, allowing multiple WebSocket instances to synchronize real-time messages across nodes.

---

# Observability

Every service includes:

### Structured Logging

Logs include:

* Controller
* Endpoint
* Status
* Latency

### Distributed Tracing

Each request receives a trace identifier.

```text
traceId=xxxxxxxx
```

Using MDC-based tracing.

### Metrics

Micrometer metrics exposed through Spring Boot Actuator.

Examples:

```text
http.api.count
http.api.latency
```

---

# Technology Stack

## Backend

* Java 17
* Spring Boot
* Spring Security
* Spring AOP
* Spring WebSocket
* OpenFeign

## Databases

* MongoDB

## Messaging

* Redis Pub/Sub

## Storage

* Cloudflare R2

## Observability

* Micrometer
* Spring Boot Actuator

## Infrastructure

* Docker
* Docker Compose
* NGINX

---

# Docker Architecture

```text
auth-service
profile-service
post-service
likes-service
reel-service
interaction-service
feed-service
view-service
interest-service
reelfetch-service

chat-service-a
chat-service-b
redis
nginx
```

Chat services are deployed as multiple instances behind NGINX and synchronized through Redis Pub/Sub.

---

# Environment Variables

Example:

```env
# JWT
JWT_SECRET=your-secret

# Internal Service Authentication
SERVICE_SECRET=your-internal-secret

# MongoDB
MONGO_URI=mongodb://localhost:27017/social

# Cloudflare R2
R2_ACCESS_KEY=
R2_SECRET_KEY=
R2_BUCKET=
R2_PUBLIC_URL=

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Chat Instance
INSTANCE_NAME=CHAT-A
```

---

# Running Locally

## Prerequisites

* Docker
* Docker Compose
* Node.js

---

## Backend Setup

### 1. Clone the Backend Repository

```bash
git clone <backend-repository-url>
cd backend
```

### 2. Configure Environment Variables

Create environment files from the provided examples and fill in the required values.

```bash
cp .env.example .env
```

### 3. Start All Backend Services

```bash
docker compose up --build
```

All services will start using the predefined ports configured in Docker Compose.

Do not change service names or ports unless you also update the frontend configuration.

---

## Frontend Setup

### 1. Clone the Frontend Repository

```bash
git clone <frontend-repository-url>
cd frontend
```

### 2. Configure Environment Variables

```bash
cp .env.example .env
```

### 3. Install Dependencies

```bash
npm install
```

### 4. Start Frontend

```bash
npm run dev
```

Once both frontend and backend are running, the application should work without additional configuration.

---

# Frontend Integration

## Authentication

1. Login using Auth Service.
2. Receive JWT.
3. Store JWT.
4. Send JWT in the Authorization header.

```http
Authorization: Bearer <token>
```

---

## Home Feed

```text
Frontend
     |
     v
Feed Service
     |
     v
Posts + Like Status
```

---

## Reel Feed

```text
Frontend
     |
     v
Reel Fetch Service
     |
     +---- Interest Service
     |
     +---- Reel Service
```

---

## Chat

### Step 1

Fetch conversation ID.

```http
GET /api/chat/get-convoId
```

### Step 2

Fetch chat history.

```http
GET /api/chat/get-chat
```

### Step 3

Connect WebSocket.

```text
/ws
```

### Step 4

Subscribe.

```text
/topic/conversation/{conversationId}
```

### Step 5

Send Messages.

```text
/app/chat.send
```

---

# Project Highlights

* Distributed microservice architecture
* Fan-Out-On-Write feed generation
* Fan-Out-On-Read reel recommendations
* Interest-based personalization
* Real-time chat with Redis Pub/Sub
* JWT-secured APIs
* WebSocket authentication
* Cloudflare R2 media storage
* Distributed tracing
* Metrics and observability
* Dockerized deployment
* Horizontally scalable services
* Multi-instance chat scaling
