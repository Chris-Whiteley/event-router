# EventRouter Overview

EventRouter is a framework for routing domain events across both local and distributed boundaries in a consistent and declarative way.

It is designed for systems where:
- Some events are handled locally within a service
- Other events must be dispatched to remote services via messaging
- Event producers should not be coupled to routing or transport logic

---

## Key Features

### Unified Local and Global Dispatching
EventRouter provides a single abstraction for event dispatching.  
Producers raise events without needing to know whether handlers are local or remote.

### Decoupled Routing Logic
Routing decisions are handled centrally by the EventRouter infrastructure, keeping producers simple, testable, and free of transport concerns.

### Framework and Transport Agnostic
The core routing model is independent of:
- Messaging platforms (Kafka, RabbitMQ, etc.)
- Application frameworks (Micronaut, Spring, etc.)

Integrations are provided via dedicated modules.

### Declarative Event Handling
Handlers are registered using annotations such as `@EventHandler`, enabling:
- Clear intent
- Minimal boilerplate
- Automatic handler discovery

---

## Comparison with Existing Solutions

### Micronaut / Spring Events
- Excellent for local event handling
- No built-in abstraction for routing events across services
- Global dispatch must be implemented separately

EventRouter treats local and global handlers uniformly.

### Messaging Platforms (Kafka, RabbitMQ)
- Provide durable global messaging
- Require producers to integrate directly with the messaging API
- Do not manage local event dispatch

EventRouter abstracts messaging details away from producers.

### CQRS / Event Sourcing Frameworks
- Often tied to specific architectural patterns
- Can introduce significant conceptual overhead

EventRouter is intentionally general-purpose and lightweight.

---

## Typical Use Cases

- Event-driven microservices
- Hybrid local + distributed event handling
- Systems requiring flexible routing rules
- Teams seeking a clean separation between event production and delivery

---

## Key Differentiator

EventRouter’s primary differentiator is its **unified abstraction** for local and global event routing.

This allows:
- Consistent event handling semantics
- Reduced coupling between services
- Easier evolution of routing strategies over time
