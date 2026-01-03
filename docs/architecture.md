# Architecture Overview

This document describes the high-level architecture of the EventRouter framework and how events are routed between microservices and local handlers.

## Overview

EventRouter provides a structured approach to handling **global events** in a microservices architecture.  
Events raised in one service can be routed to, and processed by, remote services while remaining decoupled from transport and framework-specific concerns.

The framework separates **global event delivery** from **local event dispatch**, allowing each concern to scale independently.

---

## Design Details

### 1. Topic Organisation

Each microservice owns a dedicated inbound topic named:

`events_for_<serviceId>`

- `<serviceId>` uniquely identifies the target microservice.
- The topic contains all global events addressed to that service.
- This approach avoids topic sprawl while maintaining clear routing boundaries.

---

### 2. Global Event Dispatching

`GlobalEventsProducer` is responsible for publishing global events to remote services.

Key characteristics:

- Single-threaded producer
- Uses a Kafka producer under the hood
- Determines destination services for each event
- Publishes events to one or more `events_for_<serviceId>` topics

A single producer instance can publish to multiple topics efficiently, leveraging Kafka’s batching and async IO.

---

### 3. Global Event Consumption

Each service runs a `GlobalEventsConsumer` responsible for consuming events from its inbound topic.

- Single-threaded Kafka consumer
- Consumes from `events_for_<serviceId>`
- Forwards events to the local event dispatcher for processing

This ensures that events targeting a service are consumed in a predictable order.

---

### 4. Local Event Dispatching

Once a global event is consumed, it is handed off to the local dispatcher:

- Each event type is mapped to a dedicated blocking queue
- Each queue is processed by its own worker thread
- Handlers are resolved via methods annotated with `@EventHandler`

This guarantees ordered handling per event type while allowing parallelism across different event types.

---

### 5. Multi-Threaded Local Processing

Local dispatching is intentionally multi-threaded:

- Prevents slow handlers from blocking unrelated events
- Allows CPU-bound and IO-bound handlers to coexist
- Keeps Kafka consumption lightweight and responsive

---

## Key Characteristics

### Simplicity
- One inbound topic per service
- Clear ownership of event destinations
- Minimal operational overhead

### Scalability
- Kafka partitioning can be used to scale throughput
- Local processing scales independently via multiple dispatcher threads

### Sequential Processing
- Events for a given service are consumed in the order they are produced
- Ordering guarantees are preserved at the service boundary

---

## Potential Improvements

While the current design is suitable for most workloads, further optimisations may be considered:

### High Event Volume
- Introduce partitions within `events_for_<serviceId>` to enable parallel consumption
- Split high-throughput event types into dedicated topics

### Non-Critical Events
- Allow certain event types (e.g. heartbeat or telemetry events) to bypass retries
- Reduce queue pressure for low-value messages

### Monitoring and Metrics
- Track consumer lag and throughput
- Expose dispatcher queue depth and handler execution time
- Use metrics to identify bottlenecks before scaling changes are required
