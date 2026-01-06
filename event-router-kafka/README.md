# Event Router – Kafka

`event-router-kafka` provides a **Kafka-based transport implementation** for the
Event Router library.

It allows events published through the Event Router to be **dispatched to and
consumed from Kafka**, without the producer or handler needing to interact with
Kafka APIs directly.

---

## Responsibilities

This module is responsible for:

- Publishing routed events to Kafka topics
- Consuming events from Kafka and re-injecting them into the Event Router
- Mapping event names to Kafka topics
- Bridging the Event Router abstraction with Kafka messaging

---

## Design Goals

- Keep Kafka-specific logic out of application code
- Avoid leaking Kafka APIs into business logic
- Allow local and remote handlers to coexist transparently
- Support incremental adoption of Kafka in existing systems

---

## How It Fits In

In a typical setup:

1. Application code publishes an event via Event Router
2. The router decides whether the event should be:
   - handled locally
   - dispatched remotely via Kafka
3. This module performs the Kafka-specific work
4. Consuming services receive the event and route it locally again

The application never needs to:
- Create Kafka producers or consumers
- Manage serialization directly
- Know which service handles which event

---

## What This Module Does *Not* Do

This module does **not**:

- Provide framework integration (see Micronaut module)
- Manage Kafka infrastructure or topic creation
- Define application configuration conventions
- Replace Kafka Streams or low-level Kafka APIs

---

## Typical Usage

This module is typically used:
 
- alongside `event-router-core`
- and a framework integration such as `event-router-micronaut`
- via a integration module such as `event-router-kafka-micronaut`

---

## Status

⚠️ **Early-stage / pre-release**

Kafka integration APIs may evolve as the project matures.
