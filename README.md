# Event Router

**Event Router** is a Java library for building event-driven systems that unifies
**local and remote event handling** behind a simple, annotation-based API.

It allows services to publish events without caring whether they are handled:
- locally within the same JVM, or
- remotely by other services via messaging systems such as Kafka.

The routing decision is handled centrally, keeping producers clean, decoupled,
and easy to test.

---

## Why Event Router?

In most microservice architectures:
- Local events are handled using framework-specific listeners (Spring, Micronaut, etc.)
- Remote events require explicit messaging logic (Kafka, RabbitMQ, etc.)
- Producers must know *how* and *where* events are delivered

**Event Router removes this burden.**

With Event Router:
- Producers simply publish events
- Handlers declare interest using annotations
- The router decides whether the event is handled locally or dispatched remotely

---

## Key Concepts

- **Annotation-based event handlers**  
  Register handlers using `@EventHandler` with no framework lock-in.

- **Unified local & remote routing**  
  The same event can be processed locally, remotely, or both.

- **Framework agnostic core**  
  Core routing logic is independent of Micronaut, Spring, or any messaging platform.

- **Pluggable messaging implementations**  
  Kafka, Micronaut, Spring, and other integrations live in separate modules.

---

## Typical Use Cases

- Event-driven microservices
- Domain events that may cross service boundaries
- Systems migrating from monolith → microservices
- Applications that want clean separation between business logic and messaging

---

## Modules

This project is a multi-module repository:

| Module | Description |
|------|------------|
| `event-router-core` | Core routing & annotation model |
| `event-router-kafka` | Kafka-based event transport |
| `event-router-micronaut` | Micronaut integration |
| `event-router-kafka-micronaut` | Kafka + Micronaut integration |

Additional integrations (e.g. Spring) can be added without changing the core.

---

## Status

⚠️ **Early-stage / pre-release**

This project is under active development and APIs may change until a stable release.
Feedback and design discussion are welcome.

---

## Documentation

- [Architecture Overview](docs/architecture.md)
- [Design Considerations](docs/design-considerations.md)
- [Comparison with Other Solutions](docs/architecture.md)

---

## License
Licensed under the Apache License, Version 2.0.

