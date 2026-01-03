# Design Considerations

The EventRouter design aims to balance simplicity, scalability, and operational clarity.  
This document outlines key trade-offs and alternative approaches considered during the design.

---

## Current Design Observations

### Single Producer, Multiple Topics
- Efficient use of resources
- Kafka producers scale well with batching and compression
- Avoids unnecessary complexity in producer management

### Single Consumer per Service Topic
- Preserves event ordering per service
- Simplifies failure handling
- Local multithreaded dispatch mitigates consumer bottlenecks

### Topic per Service ID
- Clear ownership model
- Avoids excessive topic creation
- Can become a bottleneck for very high-throughput services

---

## Alternative Approach: Topic per Event Type

### Advantages
- Increased parallelism
- Event-specific scaling
- Reduced filtering at the consumer

### Disadvantages
- Topic explosion risk
- Higher operational complexity
- Increased thread and resource usage

---

## Optimisations for the Current Model

### Partitioning
Partition `events_for_<serviceId>` by event type or routing key to increase throughput while keeping topic count low.

### Batching
Tune producer settings such as `linger.ms` and `batch.size` to maximise throughput.

### Consumer Scaling
Increase partitions and run multiple consumers in the same group when required.

### Asynchronous Local Dispatch
Ensure handlers avoid blocking where possible to minimise latency.

### Backpressure
Pause Kafka consumption when local queues exceed safe thresholds.

---

## When to Consider Event-Type Topics

- Extremely high event volume
- Event-specific SLAs
- Services interested in only a narrow subset of events

---

## Hybrid Strategy

A hybrid approach may provide the best balance:

1. Default to one topic per service
2. Introduce dedicated topics for high-throughput or latency-sensitive events
3. Use naming conventions such as:
`events_for_<serviceId>_<eventType>`

---

## Recommendations

- Start simple and evolve based on observed metrics
- Prefer partitioning over topic proliferation
- Monitor lag, throughput, and handler execution time
- Optimise only when real bottlenecks appear

