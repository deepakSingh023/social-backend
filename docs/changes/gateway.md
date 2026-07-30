##  API Gateway Architecture & Development Notes

This document covers the architectural layout, core dependencies, request lifecycle, and critical development lessons learned during the implementation of our horizontally scalable Spring Cloud Gateway Server MVC infrastructure.

------------------------------
## 1. Core Dependency Manifest
Our gateway uses the modern Servlet-based MVC architecture (not the reactive WebFlux engine). The required production dependencies in our pom.xml are categorized as follows:

| Dependency Name / Artifact | Purpose | Group Id |
|---|---|---|
| spring-cloud-starter-gateway-server-webmvc | Core API gateway engine for blocking/servlet environments. | org.springframework.cloud |
| spring-boot-starter-security | Framework container to run our stateless security pipeline. | org.springframework.boot |
| spring-boot-starter-data-redis | Connection client (Lettuce) for tracking rate limits globally. | org.springframework.boot |
| commons-pool2 | Critical: Manages the Redis connection pool to prevent scale crashes. | org.apache.commons |
| spring-boot-starter-actuator | Exposes metrics (/actuator/metrics) and health indicators. | org.springframework.boot |
| micrometer-registry-prometheus | Translates internal gateway tracking into a Prometheus-readable format. | io.micrometer |
| opentelemetry-spring-boot-starter | Automatically generates and propagates trace contexts cluster-wide. | io.opentelemetry.instrumentation |
| jjwt-api / jjwt-impl / jjwt-jackson | Handcrafted JWT extraction utilities to parse incoming auth headers. | io.jsonwebtoken |

------------------------------
## 2. How Spring Configuration Works (Under the Hood)
Instead of over-engineering the application with custom Java proxy managers, we let Spring Boot's auto-configuration engine handle the infrastructure boilerplate directly via application.properties:

* Convention Over Configuration: When Spring Boot detects spring.data.redis.host and the RateLimiter properties, it automatically spins up a shared, cluster-wide state manager.
* Property-Driven Routing: The gateway routes are explicitly indexed as structured properties (e.g., spring.cloud.gateway.mvc.routes[x]). This decouples our routing logic from our application code entirely.
* SpEL Bean Injection: By declaring args.key-resolver=#{@tokenKeyResolver}, the framework uses the Spring Expression Language (SpEL) to automatically link our custom Java identification rules to the property-driven route engine.

------------------------------
## 3. How the Dual-Strategy Rate Limiter Works
To ensure our application scales horizontally across an infinite number of container instances, no state is stored inside the Gateway's local JVM memory. Everything lives in our central Redis cluster.
The Java code's only job is to return a unique bucket name string (String key). Spring then takes that key and updates the counter in Redis using atomic tracking scripts.
## Strategy A: IP-Based Rate Limiting

* Target Ends: Public endpoints (e.g., /api/auth/login, /api/auth/register).
* Mechanism: Reads the incoming TCP socket connection info via request.remoteAddress().
* The Key Created: rate:ip:192.168.1.5
* Outcome: Protects internal databases against brute-force login attacks or script spamming. [1] 

## Strategy B: Token/User-Based Rate Limiting

* Target Ends: Private endpoints (e.g., /api/posts/**, /api/feed/**).
* Mechanism: Checks if our security system has already stamped an X-User-Id header onto the request. If it hasn't, it falls back to parsing the raw Authorization: Bearer <JWT> header.
* The Key Created: rate:user:9942 or rate:token:18472942 (using .hashCode() on the token string to keep the Redis key footprint compact and fast).
* Outcome: Ensures that if a user changes locations (e.g., switches from cellular data to a home Wi-Fi network, changing their IP address), their rate-limiting bucket correctly follows their Identity, not their network location.

------------------------------
## 4. Key Mistakes to Avoid 

During implementation, several critical bugs were identified and fixed. Keep these notes handy to ensure future updates do not reintroduced them:

## Mistake 1: Not Pushing the Request Down the Pipe 

* What went wrong: The initial custom filter implementation returned a plain Function<ServerRequest, ServerRequest>. While it successfully mutated the request headers, it lacked access to the gateway framework's chain execution engine. The filter essentially swallowed the request; it modified it, but never passed it along.
* The Architectural Fix: We rewrote the filter to implement HandlerFilterFunction<ServerResponse, ServerResponse>. This allows us to call next.handle(mutatedRequest), which acts as the physical plumbing lever that pushes the modified request downstream to the next filter and ultimately to our targeted microservices.

## Mistake 2: Metric Blindness 

* What went wrong: The original metric counter definitions were hardcoded standalone variables (e.g., routingFailures.increment()). In a scaled production environment, this information is practically useless because it only indicates that a failure happened. It completely hides which service failed or why.
* The Architectural Fix: We refactored the metric code to use Tags (Dimensions) via Micrometer's Counter.builder(). Now, our system tracks parameters like route_id, status_code, and outcome. This allows monitoring panels (like Grafana) to isolate exactly which service is struggling under heavy loads.

## Mistake 3: Over-Engineering via Classpath Conflicts

* What went wrong: Manually importing raw bucket4j-core and bucket4j-lettuce files alongside our standard Spring Data Redis engine caused severe internal classpath conflicts.
* The Architectural Fix: We removed the third-party Bucket4j dependencies entirely. Spring Cloud Gateway MVC includes managed Bucket4j support natively under the hood out-of-the-box. We simply define the properties, and the framework safely handles the thread bindings behind the scenes.

------------------------------
