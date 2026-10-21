---
name: spring-kafka
description: Guide Spring Kafka integration for consumers, producers, and error handling
version: 0.1.0
tags: [kafka, messaging, streaming, consumers, producers]
triggers:
  - implementing Kafka consumers or producers
  - configuring Spring Kafka listener containers
  - handling Kafka deserialization or processing errors
  - testing Kafka integration
  - configuring consumer groups and topic subscriptions
spring_versions:
  boot: ">=3.2"
  project: "Spring Kafka >=3.1"
---

# Spring Kafka

Conventions for integrating Apache Kafka with Spring Boot. Covers consumer and producer configuration, error handling strategies, serialization, and testing with embedded Kafka. Use this skill when building event-driven systems with Kafka.

## When to Use

- Creating `@KafkaListener` consumers
- Configuring `KafkaTemplate` for producing messages
- Handling deserialization errors and dead letter topics
- Setting up consumer error handling and retry policies
- Testing with `@EmbeddedKafka` or Testcontainers
- Configuring Avro serialization with Schema Registry

<!-- TODO: Full content to be authored -->
