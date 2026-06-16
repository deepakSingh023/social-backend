# Interest Service

## Overview

The Interest Service is responsible for maintaining personalized user interest profiles used by the reel recommendation ecosystem.

The service is fully internal and is never accessed directly by frontend clients.

Its primary responsibility is to track user preferences based on reel engagement and maintain a continuously evolving semantic interest profile for every user.

These interest profiles are later consumed by the Reel Fetch Service to generate personalized reel recommendations.

---

## Responsibilities

The service is responsible for:

* User interest tracking
* Semantic interest management
* Interest score updates
* Time-based interest decay
* Engagement-based interest boosting
* User preference storage
* Recommendation support

---

## Architecture

The Interest Service sits between user engagement tracking and recommendation generation.

Interest updates originate from the View Service whenever users interact with reels.

Workflow:

```text
User Watches Reel
        |
        v
View Service
        |
        |-- Update View Count
        |-- Update Popularity Score
        |
        v
Reel Service
        |
        |-- Return Semantic Tags
        |
        v
View Service
        |
        v
Interest Service
        |
        |-- Update User Interests
```

The service itself does not calculate popularity and does not generate recommendations.

Its sole responsibility is maintaining user interests.

---

## Recommendation Flow

Interest profiles are consumed during reel feed generation.

Workflow:

```text
User Opens Reel Feed
        |
        v
Reel Fetch Service
        |
        v
Interest Service
        |
        |-- Return User Interests
        |
        v
Reel Fetch Service
        |
        v
Reel Service
        |
        |-- Fetch Personalized Reels
        |
        v
Return Feed
```

The Reel Fetch Service acts as the orchestration layer while the Interest Service provides personalization data.

---

## Data Model

### UserInterest

Stores a complete interest profile for a user.

Fields:

* User ID
* Interest map
* Last updated timestamp

Example:

```text
userId = user123

interests:
{
    "fitness": 0.82,
    "gaming": 0.41,
    "football": 0.65
}
```

Each user maintains a single document containing all semantic interests.

---

### InterestScore

Represents an individual semantic interest.

Fields:

* Score
* Last updated timestamp

Example:

```text
fitness

score = 0.82

lastUpdated = 2026-06-16T12:00:00Z
```

The timestamp enables time-based decay calculations.

---

## Interest Update Lifecycle

Whenever a user interacts with a reel:

1. View Service receives the interaction
2. View Service updates reel engagement metrics
3. Reel Service recalculates popularity
4. Reel Service returns semantic tags
5. View Service calls Interest Service
6. Interest Service updates user interests
7. Updated scores are persisted

This design keeps engagement processing and interest management separated.

---

## Supported Events

The scoring engine supports:

```text
WATCH_50
WATCH_90
LIKE
SHARE
```

Currently implemented:

```text
WATCH_50
WATCH_90
LIKE
```

Share functionality is planned but has not yet been implemented.

---

## Time-Based Interest Decay

Older interests gradually lose influence over recommendations.

Decay Formula:

```text
decayedScore =
currentScore × e^(-0.03 × hoursPassed)
```

Example:

```text
fitness = 0.90

After several days:

fitness = 0.74
```

This allows recommendations to adapt as user preferences evolve.

---

## Interest Boosting

After decay is applied, engagement events contribute additional weight.

Current boost values:

```text
WATCH_50 = 0.06

WATCH_90 = 0.12

LIKE = 0.20

SHARE = 0.30
```

Examples:

```text
User watches 50% of a football reel

football += 0.06
```

```text
User likes a football reel

football += 0.20
```

Higher engagement produces stronger interest signals.

---

## Score Normalization

Interest scores are constrained between:

```text
0.0 → 1.0
```

Normalization logic:

```text
score = max(0.0, min(1.0, score))
```

This prevents runaway score growth and keeps all interests within a consistent range.

---

## Semantic Tag System

The Interest Service never stores raw hashtags.

Instead, it stores semantic categories produced by the Reel Service.

Example:

```text
Raw Tags:

gym
bodybuilding
lifting
workout
```

Mapped to:

```text
fitness
```

Only the semantic category is persisted.

Advantages:

* Cleaner recommendation signals
* Reduced noise
* Better interest grouping
* Smaller user profiles

---

## Internal APIs

### Update Interest

Used by: View Service

Purpose:

* Apply decay
* Apply engagement boosts
* Update semantic interests
* Persist new scores

---

### Get Interest

Used by: Reel Fetch Service

Purpose:

* Retrieve a user's interest profile
* Support personalized reel recommendations

---

## Storage Strategy

Each user maintains a single MongoDB document.

Example:

```text
UserInterest
|
|-- fitness
|-- gaming
|-- football
|-- travel
|-- technology
```

Advantages:

* Fast reads
* Fast writes
* Simple lookups
* Minimal storage overhead

This structure allows recommendation systems to retrieve interests with a single database query.

---

## Scalability Characteristics

Interest updates are lightweight operations.

Each update performs:

```text
Read User Interest
        +
Apply Decay
        +
Apply Boost
        +
Save Document
```

Complexity:

```text
O(number_of_tags)
```

Only tags associated with the current reel interaction are updated.

This keeps processing costs low even at scale.

---

## Service Dependencies

### Incoming Calls

#### View Service

Used for:

* Interest updates
* User engagement tracking

#### Reel Fetch Service

Used for:

* Interest retrieval
* Personalized recommendation generation

---

### Outgoing Calls

The Interest Service does not depend on external services.

All processing is performed locally using stored interest data.

---

## Technology Stack

* Java 17
* Spring Boot
* Spring Data MongoDB
* MongoDB
* Spring Security
* Spring Aop

---

## Design Trade-Offs

### Semantic Interests Instead of Raw Tags

Advantages:

* Better recommendation quality
* Reduced duplication
* Easier category matching
* Cleaner user profiles

Trade-off:

* Semantic mappings must be maintained externally.

Currently semantic mappings are managed by the Reel Service.

A future improvement would be integrating AI-powered semantic classification.

---

### Time-Based Decay

Advantages:

* Adapts to changing user preferences
* Prevents stale recommendations
* Keeps interests relevant

Trade-off:

* Additional calculations during updates

The recommendation quality improvement outweighs the small computational overhead.

---

## Summary

The Interest Service acts as the personalization layer of the reel recommendation ecosystem.

It continuously updates user interests based on reel engagement events received through the View Service, applies time-based decay, boosts interests based on engagement strength, and stores semantic preference profiles for every user.

These profiles are later consumed by the Reel Fetch Service, which retrieves user interests and uses them when requesting personalized content from the Reel Service.

This separation allows engagement tracking, interest management, recommendation orchestration, and content retrieval to scale independently while still working together to deliver personalized reel feeds.
