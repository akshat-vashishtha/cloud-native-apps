# Deployment & Cloud Infrastructure (Azure AKS)
Status: agreed   Owner: @Maya   Updated: 2026-10-18
Inputs: context/architecture.md, context/services/*.md

## Summary
Production-grade deployment and cloud infrastructure design on Azure Kubernetes Service (AKS). Defines cluster topology with Azure CNI Overlay networking, AKS Managed Istio Service Mesh for zero-trust mTLS, Azure Workload Identity with Key Vault Secret Store CSI, multi-stage non-root OCI container packaging, Horizontal Pod Autoscaling (HPA) and KEDA event-driven autoscaling based on Kafka consumer lag, and end-to-end OpenTelemetry observability.

---

## Decisions

- **DEP-001**: **Azure CNI Overlay & Ingress-NGINX**
  - *Why*: Azure CNI Overlay eliminates subnet IP exhaustion while providing native Kubernetes pod networking performance. Ingress-NGINX manages public SSL termination and routes `/api/*` and `/ws/*` to `Gateway-Service`.
  - *Rejected*: Basic Kubenet (limited Azure routing) or standard CNI (rapid IP address exhaustion).

- **DEP-002**: **Azure Workload Identity & Key Vault CSI Secret Provider**
  - *Why*: Eliminates long-lived static credentials. Pods use Azure Workload Identity (federated tokens) to mount secrets (HMAC secrets, RSA private keys, DB connection strings) directly from Azure Key Vault as volume mounts or environment variables.
  - *Rejected*: Hardcoding secrets in Kubernetes Secrets manifests or config files.

- **DEP-003**: **KEDA Event-Driven Autoscaling for Kafka Consumers**
  - *Why*: `KPI-Analytics-Service` scales dynamically based on Kafka topic lag (`banking.transactions.v1`), scaling pods up when transaction spikes occur and down when lag clears.
  - *Rejected*: Traditional CPU-only autoscaling for asynchronous stream consumers.

- **DEP-004**: **Multi-Stage Non-Root Distroless/Alpine OCI Containers**
  - *Why*: Small attack surface ($<180\text{MB}$ images), Eclipse Temurin 21 JRE, running under an unprivileged `appuser` (UID: 10001) with virtual threads enabled (`-XX:+UseZGC -Dspring.threads.virtual.enabled=true`).
  - *Rejected*: Fat root container images with full build toolchains.

- **DEP-005**: **OpenTelemetry Collector Sidecar & Prometheus Metrics**
  - *Why*: Standardized OTLP telemetry pipeline scraping Spring Boot Actuator endpoints (`/actuator/prometheus`) and exporting traces (`traceparent`) to Azure Monitor / Grafana Tempo.
  - *Rejected*: Vendor-locked proprietary monitoring agents.

- **DEP-006**: **Zero-Trust Hardening: Key Vault Secret Injection & Downstream Pod NetworkPolicy**
  - *Why*: Protects inter-service communication and eliminates shared-secret risks. The `internalHmacSecret` is injected dynamically at pod startup from Azure Key Vault via Kubernetes Secret Store CSI Driver (environment variable `INTERNAL_HMAC_SECRET`), completely removing it from `application.yml`. Simultaneously, Kubernetes `NetworkPolicy` locks down downstream pods (`Business-Operation`, `KPI-Analytics`, `Chat-Communication`), strictly dropping all network traffic not originating directly from `Gateway-Service` pod labels in `banking-edge`.
  - *Rejected*: Hardcoding HMAC secrets in plaintext configs or permitting unconstrained pod-to-pod network connectivity across namespaces.

- **DEP-007**: **Zero-Trust Transport Encryption via AKS Managed Istio Service Mesh (Strict mTLS)**
  - *Why*: Endorses Alex's architectural recommendation. Offloads transport-layer encryption and mutual identity verification from Spring Boot application code (eliminating Java KeyStore/TrustStore maintenance and manual certificate rotation) to AKS-managed Istio sidecars (`--enable-azure-service-mesh`). Enforces `PeerAuthentication` in `STRICT` mode across `banking-edge`, `banking-core`, and `banking-analytics`. Pod-to-pod TCP streams are automatically encrypted via mTLS using ephemeral SPIFFE identities, perfectly complementing the application-level `X-Internal-HMAC` header validation for defense-in-depth.
  - *Rejected*: In-app Java SSL configuration (`server.ssl.client-auth=need`) with manual PKCS12 keystore rotation, or unencrypted pod-to-pod plaintext in the cluster.

---

## Details

### 1. AKS Cluster Architecture & Node Pools

```
                                  [ Azure Front Door / Public IP ]
                                                │
                                                ▼
  ╔═════════════════════════════════════════════════════════════════════════════════════════════════════════════╗
  ║ AZURE KUBERNETES SERVICE (AKS) - VNet: 10.240.0.0/16                                                        ║
  ║                                                                                                             ║
  ║  ┌────────────────────────────────────────────────────────┐ ┌─────────────────────────────────────────────┐ ║
  ║  │ System Node Pool (agentpool)                           │ │ User Node Pool (workloadpool)               │ ║
  ║  │ • SKU: Standard_D2s_v5 (2 Nodes, Linux)                │ │ • SKU: Standard_D4s_v5 (Autoscaling: 3-10)  │ ║
  ║  │ • CoreDNS, Ingress-NGINX, CSI Drivers, Cert-Manager    │ │ • Microservice Workload Pods (Spring Boot)  │ ║
  ║  └──────────────────────────┬─────────────────────────────┘ └───────────────────────┬─────────────────────┘ ║
  ║                             │                                                       │                       ║
  ║                             ▼                                                       ▼                       ║
  ║  ┌───────────────────────────────────────────────────────────────────────────────────────────────────────┐ ║
  ║  │ KUBERNETES NAMESPACES & WORKLOAD LAYOUT                                                               │ ║
  ║  │                                                                                                       │ ║
  ║  │ • Namespace: banking-edge       ──► Gateway-Service (Replicas: 2-5, Port 8080)                        │ ║
  ║  │ • Namespace: banking-core       ──► User-Rbac-Svc (2), Business-Operation-Svc (3), Chat-Svc (2)       │ ║
  ║  │ • Namespace: banking-analytics  ──► KPI-Analytics-Svc (KEDA Scaled: 2-8 Replicas)                    │ ║
  ║  │ • Namespace: banking-ai         ──► Intelligence-Ai-Svc (2-4 Replicas, GPT-4 Client)                  │ ║
  ║  │ • Namespace: monitoring         ──► OpenTelemetry Collector, Prometheus, Grafana                      │ ║
  ║  └───────────────────────────────────────────────────────────────────────────────────────────────────────┘ ║
  ╚═════════════════════════════════════════════════════════════════════════════════════════════════════════════╝
```

---

### 2. Multi-Stage Dockerfile Blueprint (All Services)

```dockerfile
# Stage 1: Build stage with Maven/Gradle
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace
COPY . .
RUN ./mvnw clean package -DskipTests

# Stage 2: Hardened Runtime Container
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security: Create non-root unprivileged user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser:appgroup

# Copy compiled JAR
COPY --from=builder /workspace/target/*.jar app.jar

# Production JVM Tuning for Virtual Threads and Container Memory Limits
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseZGC -XX:+ZGenerational -Dspring.threads.virtual.enabled=true"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

---

### 3. KEDA Autoscaling for `KPI-Analytics-Service` (`keda-scaler.yaml`)

```yaml
apiVersion: keda.sh/v1alpha1
kind: ScaledObject
metadata:
  name: kpi-analytics-kafka-scaler
  namespace: banking-analytics
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: kpi-analytics-service
  minReplicaCount: 2
  maxReplicaCount: 8
  cooldownPeriod: 300
  pollingInterval: 15
  triggers:
    - type: apache-kafka
      metadata:
        bootstrapServers: kafka-cluster-kafka-bootstrap.banking-core.svc:9092
        consumerGroup: kpi-analytics-group
        topic: banking.transactions.v1
        lagThreshold: "100" # Scale up when consumer lag exceeds 100 messages
```

---

### 4. Kubernetes NetworkPolicy (Zero-Trust Cluster Perimeter)

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-only-gateway-ingress
  namespace: banking-core
spec:
  podSelector:
    matchLabels:
      tier: backend
  policyTypes:
    - Ingress
  ingress:
    - from:
        - namespaceSelector:
            matchLabels:
              kubernetes.io/metadata.name: banking-edge
          podSelector:
            matchLabels:
              app: gateway-service
      ports:
        - protocol: TCP
          port: 8080
        - protocol: TCP
          port: 9090 # gRPC
```

---

### 5. AKS Managed Istio Service Mesh & Strict mTLS (`istio-peer-authentication.yaml`)

```yaml
# 1. Enforce strict mTLS across all pod-to-pod communication in banking namespaces
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: default-strict-mtls
  namespace: banking-core
spec:
  mtls:
    mode: STRICT
---
# 2. Sidecar injection enabled via namespace revision label:
# kubectl label namespace banking-core istio.io/rev=asm-1-20
# kubectl label namespace banking-edge istio.io/rev=asm-1-20
```

> **Zero-Trust Synergy:**
> - **Transport Layer (L4)**: Istio Envoy sidecars establish encrypted, mutually-authenticated TLS sessions using dynamically rotated X.509 certs (Citadel/SPIFFE IDs). Plaintext in-cluster traffic is strictly dropped.
> - **Application Layer (L7)**: `InternalHmacSignerFilter` in Gateway and `InternalHmacFilter` in downstream services validate user identity, roles, and timestamp replay-protection with HMAC-SHA256 signatures.

---

## Plan
Approved: no

- [ ] 1. Multi-stage hardened Dockerfiles for all 6 microservices
- [ ] 2. Kubernetes manifests & Kustomize overlays (`base`, `dev`, `prod`)
- [ ] 3. Azure Workload Identity & Key Vault SecretProviderClass setup
- [ ] 4. Ingress-NGINX TLS routes for REST & STOMP WebSockets
- [ ] 5. KEDA Kafka consumer lag ScaledObject deployment
- [ ] 6. OpenTelemetry collector daemonset & Actuator Prometheus scraping config
- [ ] 7. AKS Managed Istio Service Mesh addon (`--enable-azure-service-mesh`) and STRICT PeerAuthentication manifests

## Code Progress
- Not started.

## Open questions
- None.

## Change requests (to upstream files)
- None.

## Next steps
- Review and discuss the Deployment & AKS plan with Maya.

