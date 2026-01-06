# Event Router – Core

`event-router-core` contains the **framework- and transport-agnostic core** of the
Event Router library.

It provides the annotation model, routing logic, and handler abstractions that
allow events to be published without the producer needing to know *how* or *where*
they are handled.

This module has **no dependency on Kafka, Micronaut, Spring, or any other framework**.

---

## Responsibilities

The core module is responsible for:

- Defining the **event and handler model**
- Discovering and registering handlers
- Routing events to matching handlers
- Asynchronous event dispatch
- Events may be handled
  - locally 
  - remotely
  - or both
- Remaining completely decoupled from transport concerns

---

## Key Concepts

---

### Event identity

- **Events are identified by name**

- The event name is **required and must be unique** within the routing domain

- The Java type is *not* how routing is determined

- Routing is always done by **event name**, not by Java class.

---

### Event payload

An event **may or may not** carry a payload:

- Some events are just signals
(e.g. `ON_STARTUP`, `HEARTBEAT`, `CACHE_REFRESH`)

- Some events carry data
(e.g. `ORDER_PLACED`, `USER_REGISTERED`)

Both are valid and first-class.

---

### Event handlers

- A handler **always handles a specific named event**

- This is declared via `@EventHandler(name = "...")`

- The method signature determines whether it receives:

    - no arguments (signal-style event)

    - or an `Event<?>` carrying a payload


---
### Local vs global handling

- By default, handlers are local only

- To receive events from other services, a handler must explicitly opt in:


```java
@EventHandler(name = "ORDER_PLACED", access = Access.GLOBAL)

```


This makes *distribution explicit*, not accidental.

---
### Event Dispatch Model

Event Router uses an **asynchronous dispatch model**.

When an event is published, the router determines which handlers should receive it
and dispatches the event asynchronously to all matching local and/or remote handlers.

Event producers do not block waiting for handlers to execute and are not coupled
to handler threading, timing, or delivery mechanisms.
---
### Dispatch Notes

The `dispatchEvent(...)` method returns a `DispatchNote`:

```java
DispatchNote note = eventDispatcher.dispatchEvent(event);

```


A `DispatchNote` provides **visibility into routing decisions**, including:

- Which local handler endpoints the event will be dispatched to

- Which remote services the event will be sent to

This information is intended for diagnostics, logging, and observability.

It does **not** indicate:

- when handlers execute

- whether execution succeeds

- how handlers are scheduled or threaded

- delivery guarantees for remote handlers

Execution remains asynchronous and decoupled from the producer.

---

### Domains and Routing Boundaries

Event Router uses the concept of a **domain** to define routing boundaries between services.

A **domain** is a logical, hierarchical identifier that controls **which services can exchange events**.

#### Domain Basics

* Every service has:

    * a **service id** (unique identifier)
    * a **domain** (routing boundary)
* A domain:

    * must be **non-blank**
    * is **free-form** (no predefined values)
    * uses **dot notation** to express hierarchy

Examples:

```
company
company.us
company.us.ca
company.uk
company.eu
```

The meaning of a domain is entirely up to you.
It may represent organisations, regions, tenants, environments, or any other logical grouping.

---

### Domain Hierarchy Rules

Domains form a **hierarchical tree**.

Event routing is allowed when the sender and receiver are in the **same domain branch**.

#### Allowed routing

* Same domain
  `company.us` → `company.us`
* Parent to child
  `company` → `company.us`
* Child to parent
  `company.us.ca` → `company.us`
* Same branch
  `company.us` → `company.us.ca`

#### Disallowed routing

* Between sibling domains
  `company.us` → `company.uk`
* Between unrelated branches
  `company.eu` → `company.us.ca`

Sibling domains are intentionally **isolated**.

---

### Why Domains Exist

Domains allow Event Router to:

* Enforce **clear routing boundaries**
* Prevent accidental cross-tenant or cross-region event leakage
* Support large, multi-service topologies
* Model real-world organisational hierarchies
* Decouple routing rules from infrastructure and deployment

All routing decisions are made **centrally** by the router — producers and handlers remain unaware of each other.

---

### Summary

* Domains define **who can talk to whom**
* Domains are **hierarchical**
* Domains are **required** and **non-blank**
* Routing is allowed only within the same domain branch
* Producers remain fully decoupled from handlers

---

### Design Intent

Event Router intentionally separates:

- **Routing awareness** (what the router plans to do)

- **Execution semantics** (how and when handlers actually run)

This keeps event producers informed without coupling them to infrastructure,
threading, or transport concerns.

---
### What This Module Does Not Do

This module deliberately does not:

- Talk to Kafka or any messaging system

- Integrate with Micronaut, Spring, or other frameworks

- Perform serialization or deserialization

- Manage application lifecycle or dependency injection

Those concerns live in separate modules.

---
### Usage

Most users will not depend on `event-router-core` directly.

Instead, you will typically use the integration modules, such as
`event-router-micronaut` and `event-router-kafka-micronaut`.

### Status

⚠️ Early-stage / pre-release

APIs may change until a stable release is published.


    