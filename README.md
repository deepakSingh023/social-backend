# Social Media Platform Backend

A distributed social media backend built using **Java 17**, **Spring Boot**, and a **microservices architecture**. The platform demonstrates common distributed systems patterns including feed generation, recommendation pipelines, real-time communication, service isolation, and observability.

## Features

* JWT-based authentication and authorization
* User profiles
* Posts and reels
* Likes and nested comments
* Friends and followers
* Fan-Out-On-Write home feed generation
* Interest-based reel recommendations
* Real-time chat using STOMP over WebSocket
* Redis Pub/Sub based multi-instance chat synchronization
* Cloudflare R2 media storage
* Dockerized deployment
* Distributed tracing and metrics

---

# Architecture

The platform is composed of independently deployable microservices communicating through REST APIs using OpenFeign.

```
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
      +---- STOMP WebSocket
      |
      +---- Cloudflare R2
```

---

# Services

| Service          | Responsibility                                     |
| ---------------- | -------------------------------------------------- |
| Authentication   | User registration, login, JWT generation           |
| Profile          | User profiles and profile statistics               |
| Post             | Post creation, retrieval, media management         |
| Feed             | Fan-Out-On-Write home feed generation              |
| Reel             | Reel storage, popularity scoring, semantic tagging |
| Reel Fetch       | Personalized reel recommendation orchestration     |
| Interest         | User interest modeling and decay                   |
| View             | View tracking and popularity updates               |
| Likes & Comments | Likes, comments, replies, counter updates          |
| Interaction      | Friends, followers, social graph management        |
| Chat             | Real-time messaging and conversations              |

---

# Architectural Highlights

### Home Feed

The home feed uses a **Fan-Out-On-Write** architecture.

When a post is created, Feed Service generates feed entries for followers in the background, allowing subsequent feed retrieval to remain lightweight and predictable.

### Reel Recommendations

Reels are generated using a **Fan-Out-On-Read** recommendation pipeline.

Recommendations are ranked dynamically using:

* User interest profiles
* Semantic tag matching
* Popularity scoring
* Time-based interest decay

### Real-Time Chat

The chat service provides:

* STOMP over WebSocket communication
* Persistent conversation-based messaging
* Redis Pub/Sub for multi-instance synchronization
* Cloudflare R2 media uploads
* Horizontal scalability behind NGINX

---

# Technology Stack

## Backend

* Java 17
* Spring Boot
* Spring Security
* Spring WebSocket
* Spring Data MongoDB
* OpenFeign
* Spring AOP

## Databases

* MongoDB Atlas
* Supabase PostgreSQL

## Storage

* Cloudflare R2

## Messaging

* Redis Pub/Sub

## Observability

* Micrometer
* Spring Boot Actuator
* MDC-based Distributed Tracing

## Infrastructure

* Docker
* Docker Compose
* NGINX

---

# Documentation

Detailed architecture documentation is available in the `docs/` directory.

* Architecture
* Feed Generation
* Recommendation System
* Chat Architecture
* Service Communication
* Database Strategy
* Security
* Resilience
* Deployment

---

# Running Locally

## Prerequisites

* Docker
* Docker Compose
* Node.js (Frontend)

### Backend

```bash
git clone <backend-repository-url>
cd backend

cp .env.example .env

docker compose up --build
```

### Frontend

```bash
git clone <frontend-repository-url>
cd frontend

npm install
npm run dev
```

Once both frontend and backend are running, the application should connect automatically using the predefined Docker networking configuration.

---

# Environment Variables

Example:

```env
JWT_SECRET=

SERVICE_SECRET=

MONGO_URI=

R2_ACCESS_KEY=
R2_SECRET_KEY=
R2_BUCKET=
R2_PUBLIC_URL=

REDIS_HOST=
REDIS_PORT=
```

Complete examples are provided in the repository.

---

# Project Highlights

* Distributed microservices architecture
* Database-per-service design
* Fan-Out-On-Write feed generation
* Dynamic interest-based reel recommendations
* Redis Pub/Sub multi-instance chat
* JWT-secured APIs
* Cloudflare R2 media storage
* Dockerized deployment
* Distributed tracing
* Metrics and observability
* Horizontal chat scaling

---

# Future Improvements

* Kafka-based event streaming
* End-to-end encrypted messaging
* Kubernetes deployment
* API Gateway
* Distributed caching
* Read receipts and typing indicators
* Automated CI/CD pipelines

---

# License

This project was developed for educational and research purposes.
