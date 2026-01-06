
# Event Router Micronaut

**Event Router Micronaut** provides **Micronaut auto-configuration** that wires the Event Router **core runtime** into a Micronaut application.

It bridges:

* the **framework-agnostic routing model** (`event-router-core`)
* Micronaut’s **dependency injection and lifecycle system**

This module contains **no routing or transport logic of its own** — it exists purely to integrate the Event Router runtime cleanly into Micronaut applications.

---

## What This Module Provides

This module:

* Discovers event handler beans managed by Micronaut
* Registers local and global event handlers at application startup
* Wires the Event Router dispatcher into the Micronaut lifecycle
* Integrates Event Router startup with Micronaut’s `StartupEvent`
* Bridges Micronaut’s `BeanContext` into the Event Router runtime

All components are created automatically when the module is on the classpath.

---

## What This Module Does Not Do

This module deliberately does not:

* Define routing rules or event semantics
* Implement transport or messaging logic (Kafka, HTTP, etc.)
* Provide event serialization or payload schemas
* Manage application business logic

Those concerns live in:

* `event-router-core`
* transport-specific modules (e.g. `event-router-kafka`)
* application-level code

---

## Provided Beans

When included in a Micronaut application, this module registers the following beans:

---

### Core Routing Components

The Event Router runtime components are exposed as Micronaut singletons:

* `EventHandlers`
* `EventDispatcher`
* `LocalHandlerFactory`
* `Startup`

These form the in-process routing engine used by the Event Router.

---

### Global Event Coordination

The following beans enable cross-service coordination:

* `GlobalEventsProducer`
* `GlobalEventsConsumer`
* `HandledProducer`
* `HandlersRegistrar`

These components are used to:

* Publish which events a service can handle
* Discover handlers in other services
* Register and unregister remote handlers dynamically

Transport is provided by a separate module (e.g. Kafka).

---

### Micronaut Integration Components

Micronaut-specific integration is provided via:

* `MicronautBeanSupplier`
* `EventRouterMicronautStartup`

These components:

* Expose Micronaut-managed beans to the Event Router
* Trigger Event Router initialization on Micronaut startup

---

## Application Startup Integration

This module listens for Micronaut’s `StartupEvent` and initializes the Event Router automatically:

```text
Micronaut started → Event Router initialized → handlers registered → routing active
```

No manual bootstrap code is required.

---

## Typical Usage

You do **not** interact with this module directly.

Instead:

1. Add the dependency
2. Define event handlers using Event Router annotations
3. (Optionally) add a transport module (e.g. Kafka)

The Micronaut runtime will automatically wire and start the Event Router.

---

## Module Dependencies

This module depends on:

* `event-router-core`
* Micronaut runtime and context

Transport-specific functionality is provided by additional modules.

---

## When to Use This Module

Use `event-router-micronaut` when:

* You are building a **Micronaut application**
* You want **automatic discovery of event handlers**
* You want **Event Router startup managed by Micronaut**
* You want routing logic kept out of application bootstrap code

---

## Related Modules

| Module                         | Purpose                            |
| ------------------------------ | ---------------------------------- |
| `event-router-core`            | Core routing model and annotations |
| `event-router-micronaut`       | Micronaut integration              |
| `event-router-kafka`           | Kafka transport implementation     |
| `event-router-kafka-micronaut` | Kafka + Micronaut integration      |

---

## Status

⚠️ **Early-stage / pre-release**

APIs and behaviour may evolve as the routing and integration model matures.

---

### Why this works well

* Matches your Kafka Micronaut README structurally
* Clearly separates **framework integration** from **transport**
* Avoids leaking internal class names unless helpful
* Reinforces “drop-in, no manual wiring” as the value proposition
