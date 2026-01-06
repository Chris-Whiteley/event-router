# Event Router Kafka Micronaut

**Event Router Kafka Micronaut** provides **Micronaut auto-configuration** that wires the Event Router to **Apache Kafka**.

It bridges:

* the **framework-agnostic core routing model** (`event-router-core`)
* the **Kafka transport implementation** (`event-router-kafka`)
* Micronaut’s **dependency injection and configuration system**

This module contains **no routing logic of its own** — it exists purely to make
Kafka-backed event routing work seamlessly inside Micronaut applications.

---

## What This Module Provides

This module:

* Registers Kafka **producers and consumers** as Micronaut beans
* Exposes Kafka configuration via Micronaut configuration properties
* Wires remote event dispatch and consumption into the Event Router runtime
* Enables **service-to-service event routing** using Kafka with zero manual wiring

All beans are created automatically when the module is on the classpath.

---

## What This Module Does Not Do

This module deliberately does not:

* Define routing rules or event semantics
* Implement event dispatch logic
* Provide Kafka topic management or operational concerns
* Perform business-level serialization beyond the Event Router envelope

Those concerns live in:

* `event-router-core`
* `event-router-kafka`
* application-level configuration

---

## Provided Beans

When included in a Micronaut application, this module registers the following beans:

### Kafka Properties

A shared `Properties` instance derived from Micronaut configuration:

* `event-router.kafka.bootstrap-servers`
* `event-router.kafka.extra-properties`

Used by all Kafka producers and consumers.

---

### Event Router Kafka Components

The following Event Router Kafka components are exposed as Micronaut singletons:

* `Producer<EventsHandledByService>`
* `ClosableConsumer<EventsHandledByService>`
* `Producer<RemoteServiceEvent>`
* `ClosableConsumer<NamedEvent>`

These are used internally by the Event Router to:

* Publish service capabilities
* Discover remote handlers
* Send and receive routed events

---

## Configuration

Kafka configuration is provided using Micronaut configuration properties.

### Example (`application.yml`)

```yaml
event-router:
  kafka:
    bootstrap-servers: localhost:9092
    extra-properties:
      security.protocol: PLAINTEXT
```

All values under `extra-properties` are passed directly to the Kafka client,
allowing full control without code changes.

---

## Typical Usage

You do **not** interact with this module directly.

Instead:

1. Add the dependency
2. Configure Kafka
3. Use Event Router annotations and APIs as normal

The Micronaut runtime will automatically wire everything together.

---

## Module Dependencies

This module depends on:

* `event-router-micronaut`
* `event-router-kafka`
* Micronaut runtime and context
* Kafka client libraries (via `event-router-kafka`)

---

## When to Use This Module

Use `event-router-kafka-micronaut` when:

* You are building a **Micronaut application**
* You want **Kafka-backed event routing**
* You want **zero manual Kafka wiring**
* You want routing logic kept out of application code

---

## Related Modules

| Module                         | Purpose                            |
| ------------------------------ | ---------------------------------- |
| `event-router-core`            | Core routing model and annotations |
| `event-router-kafka`           | Kafka transport implementation     |
| `event-router-micronaut`       | Micronaut integration              |
| `event-router-kafka-micronaut` | Kafka + Micronaut integration      |

---

## Status

⚠️ **Early-stage / pre-release**

APIs and configuration may evolve as the routing model matures.