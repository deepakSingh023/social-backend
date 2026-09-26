# Security Architecture

## Overview

The backend uses separate security mechanisms for external client requests, internal service communication, and WebSocket connections.

For REST APIs, the API Gateway is the external entry point. It validates JWTs, handles CORS, and forwards authenticated requests to downstream services with trusted gateway headers. Downstream services validate those headers before processing the request.

Internal service-to-service requests use dedicated service authentication headers and are validated by the receiving service.

The Chat Service uses a separate WebSocket authentication flow. Since WebSocket/STOMP connections do not pass through the normal REST security filters, the `JwtChannelInterceptor` validates the JWT during the STOMP `CONNECT` frame and establishes the authenticated user as the connection principal.

The system is stateless. Authentication state is carried by JWTs and trusted service headers rather than server-side user sessions.

---

## Security Goals

* Authenticate client requests using JWTs
* Protect communication between services
* Keep REST authentication centralized at the Gateway
* Protect downstream services from unauthorized direct requests
* Authenticate WebSocket connections
* Protect media upload operations
* Avoid storing user authentication state in server-side sessions

---

## REST Request Security

REST requests enter through the API Gateway.

```text
Client
  |
  | JWT
  v
API Gateway
  |
  | JWT validation
  | CORS
  | Rate limiting
  | Trusted headers
  v
Downstream Service
  |
  | Gateway / Internal filter
  v
Controller
```

The Gateway validates the user's JWT before forwarding the request.

For authenticated requests, the Gateway adds trusted headers containing the authenticated user identity and an internal gateway secret.

Downstream services validate these headers through their application-level filters.

This keeps JWT validation centralized while still preventing downstream controllers from accepting requests without the expected gateway or internal-service authentication headers.

---

## JWT Authentication

Users authenticate through the Authentication Service.

The authentication flow is:

1. The user submits credentials.
2. Authentication Service validates the credentials.
3. The submitted password is checked against the stored BCrypt hash.
4. A JWT is generated for the authenticated user.
5. The JWT is returned to the client.
6. The client sends the JWT with subsequent authenticated requests.
7. The API Gateway validates the JWT before forwarding the request.

The backend does not maintain a server-side session for the user.

---

## Password Security

Passwords are not stored in plain text.

During registration, the Authentication Service hashes the password using BCrypt before storing it.

During login, the submitted password is verified against the stored BCrypt hash.

```text
Registration
    |
    v
Password
    |
    v
BCrypt
    |
    v
Stored Hash


Login
    |
    v
Submitted Password
    |
    v
BCrypt Verification
    |
    v
Stored Hash
```

The stored password hash cannot be used to recover the original password.

---

## Downstream Service Protection

Downstream REST services do not independently perform the complete external JWT authentication flow.

Instead, they validate the authentication information supplied by the Gateway.

The relevant filters distinguish between:

* Requests coming through the Gateway
* Authorized internal service requests
* Requests that do not contain the required authentication headers

```text
External Client
      |
      v
API Gateway
      |
      +---- JWT validation
      |
      +---- Gateway headers
      |
      v
Service
      |
      +---- GatewayHeaderFilter
      |
      +---- InternalFilter
      |
      v
Application Logic
```

This keeps authentication responsibilities separated:

* **Gateway:** external JWT validation and CORS
* **Service filters:** validation of trusted gateway/internal requests
* **Controllers/services:** business authorization and application logic

The filters provide application-level protection. They are not a replacement for network-level isolation.

---

## Internal Service Authentication

Service-to-service requests use dedicated internal authentication headers.

The receiving service validates the expected internal credentials before allowing the request to reach the protected application path.

This is used for internal communication that does not originate from a normal client request.

The design therefore separates client authentication from service authentication rather than treating every request as a user request.

---

## WebSocket Security

The Chat Service uses STOMP over WebSocket for real-time communication.

WebSocket connections use a separate authentication path from REST requests.

```text
Client
  |
  | WebSocket / STOMP CONNECT
  | JWT
  v
Chat Service
  |
  v
JwtChannelInterceptor
  |
  +---- Validate JWT
  |
  +---- Extract userId
  |
  +---- Create Principal
  |
  v
STOMP Connection
```

The `JwtChannelInterceptor` validates the JWT during the STOMP `CONNECT` frame.

After successful validation, the authenticated user is associated with the STOMP connection as its `Principal`.

Subsequent STOMP messages use that established connection identity.

This is separate from the Gateway's REST authentication flow because WebSocket messages are handled through the WebSocket/STOMP channel rather than normal HTTP controller filters.

---

## Chat REST Security

The Chat Service also exposes REST endpoints.

For these requests:

* The Gateway handles external JWT validation and CORS.
* `GatewayHeaderFilter` validates Gateway requests.
* `InternalFilter` validates authorized internal requests.

The WebSocket authentication mechanism is not used for these REST endpoints.

---

## Media Upload Security

Media uploads use an authenticated pre-signed URL workflow.

```text
Client
  |
  | Authenticated REST request
  v
API Gateway
  |
  v
Chat / Application Service
  |
  | Validate request
  | Generate pre-signed URL
  v
Cloudflare R2
  ^
  |
  | Direct upload using temporary URL
  |
Client
```

The client does not receive Cloudflare R2 credentials.

Before generating the upload URL, the backend validates the authenticated request and the supplied file metadata.

The implementation also validates supported content types and sanitizes the original filename before generating the storage key.

The resulting pre-signed URL is temporary and is used by the client to upload directly to Cloudflare R2.

---

## Rate Limiting

Rate limiting is handled at the API Gateway.

Redis is used to maintain the distributed state required by the Gateway's rate-limiting mechanism.

```text
Client
  |
  v
API Gateway
  |
  +---- Rate Limiter
  |          |
  |          v
  |        Redis
  |
  v
Service
```

This keeps rate limiting outside the individual business services and allows the Gateway to apply the policy before forwarding requests downstream.

---

## Security Boundaries

The current architecture has three main security boundaries:

| Boundary                | Mechanism                           |
| ----------------------- | ----------------------------------- |
| Client → Gateway        | JWT                                 |
| Gateway → REST Service  | Trusted Gateway headers             |
| Service → Service       | Internal service authentication     |
| WebSocket Client → Chat | JWT through `JwtChannelInterceptor` |
| Client → Cloudflare R2  | Backend-generated pre-signed URL    |

This separation avoids using the same authentication mechanism for every type of communication.

---

## Current Trade-Offs

### Centralized REST Authentication

JWT validation is centralized at the Gateway instead of being duplicated across every REST service.

This reduces repeated security configuration, but downstream services depend on the Gateway/header validation mechanism for normal external requests.

### Internal Service Authentication

Dedicated internal credentials provide a simple way to distinguish trusted service requests.

The current implementation does not provide mutual TLS or a dedicated service identity system.

### WebSocket Authentication

Chat WebSockets authenticate through the STOMP `CONNECT` frame rather than the normal REST security pipeline.

This keeps WebSocket authentication close to the actual messaging channel, but requires a separate security implementation.

### Stateless Authentication

The backend does not maintain user sessions.

This simplifies horizontal scaling because an application instance does not need to maintain a user's authentication session.

---

## Current Security Limitations

The current implementation does not include:

* Role-based access control
* OAuth2 / OpenID Connect
* Refresh-token rotation
* Mutual TLS between services
* Centralized enterprise secret management
* A distributed authorization policy engine

These are outside the current security implementation rather than requirements that the existing architecture claims to solve.

---

## Security Architecture Summary

```text
                         Client
                           |
                         JWT
                           |
                           v
                    API Gateway
                    /    |     \
                   /     |      \
              REST     Rate      WebSocket
                |      Limit        |
                |        |          |
                v      Redis        v
          Trusted Headers     JwtChannelInterceptor
                |                   |
                v                   v
        Application Services     Chat Service
                |
        Gateway / Internal
             Filters
                |
                v
          Business Logic

                 Media Upload
                      |
                      v
             Pre-signed R2 URL
                      |
                      v
                Cloudflare R2
```

The security architecture separates external authentication, internal service authentication, WebSocket authentication, and media access.

JWTs provide stateless user authentication, the API Gateway handles external REST security, service filters validate trusted internal requests, and the Chat Service authenticates WebSocket connections through the STOMP channel.

The result is a security model that matches the current service architecture without requiring every service to independently implement the same external authentication flow.
