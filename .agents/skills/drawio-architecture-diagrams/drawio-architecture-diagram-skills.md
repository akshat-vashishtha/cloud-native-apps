---
name: drawio-architecture-diagrams
description: >
  Comprehensive skill for generating professional, publication-quality software architecture 
  diagrams as draw.io XML. Covers architectural thinking (C4 model, HLD/LLD, layered/microservice 
  patterns), visual design principles (color palettes, alignment, hierarchy, annotations, legends), 
  zero-overlap edge routing (anti-collision, multi-track corridors, port staggering, arc jumps), and 
  the complete draw.io XML format reference. Activate this skill when asked to create architecture 
  diagrams from any codebase context.
---

# Draw.io Architecture Diagram Design Skill

> **When to use:** Activate this skill whenever asked to design, create, or generate a software architecture diagram (HLD, LLD, system context, deployment, data flow, etc.) from a codebase or system description. The output is a `.drawio` XML file the user can open in draw.io / diagrams.net.

---

## Part A — Architectural Thinking (What to Diagram)

### A.1 The C4 Model — Zoom Levels

Before drawing, decide the **zoom level**. Never put everything on one canvas.

| Level | Name | Shows | Audience | When to Use |
|-------|------|-------|----------|-------------|
| **L1** | System Context | Your system as a single box + external actors, APIs, third-party systems | Business stakeholders, PMs, new joiners | Always — the mandatory starting point |
| **L2** | Container | Individual deployable units (services, databases, message brokers, frontends, gateways) | Developers, DevOps, Architects | The primary "HLD" diagram |
| **L3** | Component | Internal modules of ONE complex service (controllers, services, repositories, event handlers) | Developers on that service | Only for critical/complex services |
| **L4** | Code | Classes, interfaces, enums, function signatures | Developers (rarely drawn) | Auto-generate from IDE, don't draw manually |

**Decision rule:** If there are 50+ services, do NOT put all on one L2 diagram. Break by bounded context / business domain.

### A.2 HLD vs. LLD

| Dimension | High-Level Design (HLD) | Low-Level Design (LLD) |
|-----------|------------------------|------------------------|
| **Scope** | System-wide: macro components, tech stack, external integrations, network topology | Internal: algorithms, class design, DB schemas, API specs, state machines |
| **C4 Mapping** | Level 1 (Context) + Level 2 (Container) | Level 3 (Component) + Level 4 (Code) |
| **Audience** | Enterprise Architects, DevOps, Product, Security | Engineers, QA, Tech Leads |
| **Key Artifacts** | Data flow diagrams, network topologies, auth flows, DR plans | OpenAPI specs, sequence diagrams, class diagrams, ERDs |

### A.3 Supplementary Diagram Types

Beyond static box-and-line diagrams, consider:

| Type | Purpose | When |
|------|---------|------|
| **Dynamic / Sequence** | Trace a single request across services step-by-step | Explaining async flows, debugging paths |
| **Deployment** | Map containers to infrastructure (K8s pods, cloud regions, VMs) | DevOps / infra reviews |
| **Data Flow** | Show how data transforms and moves through the system | Data engineering, ETL pipelines, GDPR audits |

### A.4 Architectural Content Checklist

Before generating XML, extract and confirm these from the codebase context:

- [ ] **Tiers / Layers** — How many? (Client → Edge → Services → Data)
- [ ] **Components per tier** — What are the deployable units?
- [ ] **Communication protocols** — REST, gRPC, WebSocket, STOMP, Kafka, AMQP?
- [ ] **Sync vs Async** — Which connections are synchronous? Which are event-driven?
- [ ] **Data stores** — Databases, caches, object stores, search engines?
- [ ] **External systems** — Third-party APIs, legacy systems, SaaS integrations?
- [ ] **Cross-cutting concerns** — Auth, logging, tracing, rate limiting, circuit breakers?
- [ ] **Deployment topology** — Cloud provider, regions, clusters, replicas?
- [ ] **Tech stack per component** — Language, framework, version?

---

## Part B — Visual Design Principles (How to Design)

### B.1 Layout & Directional Flow

| Pattern | Flow Direction | Best For |
|---------|---------------|----------|
| **Left-to-Right** | `Clients → Gateway → Services → Kafka → Data` | Microservice HLD, request flow diagrams |
| **Top-to-Bottom** | `Client → CDN → LB → API → Services → DB` | Cloud infrastructure, deployment diagrams |
| **Inside-Out (Nested)** | Outer = Cloud → Inner = Cluster → Innermost = Pods | Enterprise system architecture, K8s deployments |

**Rules:**
- Pick ONE primary flow direction and stick to it for the entire diagram.
- Annotate the flow direction at the top of the diagram (e.g., "REQUEST FLOW →").
- If there's a return flow, annotate it separately (e.g., "← RESPONSE FLOW" at bottom).

### B.2 Swim Lanes & Boundaries

Group components into **vertical or horizontal columns/bands** by architectural tier or business domain.

**By Tier (most common for HLD):**
```
Column 1: EXTERNAL SYSTEMS    (left edge)
Column 2: CLIENTS             
Column 3: EDGE & BFF          (gateway, CDN, auth)
Column 4: APPLICATION SERVICES (core microservices)
Column 5: DATA STORES          (right edge)
```

**By Domain (for large systems):**
```
Band 1: User Management Domain
Band 2: Billing Domain
Band 3: Inventory Domain
```

**Visual rules for swim lanes:**
- Use large, lightly-tinted container rectangles (5–10% opacity fill).
- Add a bold title label at the top of each column/band.
- Separate columns with consistent horizontal spacing (≥40px gaps).

### B.3 Color Palette System

Limit to **5–7 base colors**, each assigned a semantic meaning. Never use color randomly.

#### Recommended Semantic Palette

| Role | Fill Color | Stroke Color | Usage |
|------|-----------|--------------|-------|
| **Primary Backend Services** | `#d5e8d4` (soft green) | `#82b366` | Spring Boot, core microservices |
| **Frontend / BFF** | `#dae8fc` (soft blue) | `#6c8ebf` | Next.js, React, web clients |
| **AI / ML Services** | `#fff2cc` (soft yellow) | `#d6b656` | Python, FastAPI, ML models |
| **External Systems** | `#f8cecc` (soft red/pink) | `#b85450` | Third-party APIs, legacy systems |
| **Data Stores** | `#e1d5e7` (soft purple) | `#9673a6` | MongoDB, PostgreSQL, Redis, S3 |
| **Messaging / Event Bus** | `#fff0b3` (warm yellow) | `#d4a017` | Kafka, RabbitMQ, SQS |
| **Edge / Infrastructure** | `#f5f5f5` (light gray) | `#666666` | API Gateway, CDN, Load Balancer |

**Additional rules:**
- Use **darker shades** for component borders, **lighter shades** for fills.
- Use **white (`#FFFFFF`)** or **transparent (`none`)** for text-only labels and annotations.
- Use **monochromatic depth**: lighter shades for nested children, darker for parent boundaries.

### B.4 Typography Hierarchy

| Element | Font Size | Font Style | Example |
|---------|-----------|------------|---------|
| **Diagram title** | 20–24px | Bold | "6E Converse — Microservice Architecture HLD" |
| **Diagram subtitle** | 12–14px | Regular, gray | "Left-to-Right: External → Clients → Gateway → Services → Data" |
| **Column/Tier headers** | 14–16px | Bold, uppercase | "APPLICATION SERVICES" |
| **Component name** | 12–14px | Bold | "chat-service" |
| **Component details** | 9–11px | Regular/Italic | "STOMP WebSocket\nMessage CRUD · ACK · Pin\n300K+ messages/day\n2–3 replicas" |
| **Arrow/Edge labels** | 9–11px | Regular | "REST", "Kafka topic", "gRPC" |
| **Legend items** | 10–11px | Regular | "Spring Boot 3.5 (Java 21)" |

### B.5 Connector (Arrow) Standards

| Connection Type | Line Style | Dash Pattern | Arrow Head | Usage |
|----------------|-----------|--------------|------------|-------|
| **Sync REST / gRPC** | Solid | `0` (none) | `classic` | Standard request-response calls |
| **Async Event (Kafka/MQ)** | Dashed | `dashPattern=8 8;` | `open` or `async` | Pub/Sub, event-driven messaging |
| **WebSocket / STOMP** | Dashed | `dashPattern=4 4;` | `classic` | Bidirectional real-time connections |
| **Database read/write** | Solid | `0` | `classic` | Service → Data store |
| **External integration** | Dotted | `dashPattern=2 6;` | `block` | Inbound from third-party systems |

**Rules:**
- NEVER leave an arrow unlabeled. Every connector must show the protocol or purpose.
- Use **orthogonal routing** (`edgeStyle=orthogonalEdgeStyle`) for clean right-angle paths.
- Use **line jumps** (`jumpStyle=arc;jumpSize=6;`) when lines must cross.
- Use **thicker lines** (`strokeWidth=2–3`) for high-throughput primary data paths.
- Incoming traffic enters from **top or left**; outgoing exits from **bottom or right**.

### B.6 Component Box Anatomy

Every service/component box should contain a structured internal layout:

```
┌────────────────────────────┐
│  service-name (bold)       │ ← Name (kebab-case for services)
│                            │
│  • Key responsibility 1    │ ← 2–4 bullet points of what it does
│  • Key responsibility 2    │
│  • Tech detail             │
│  • Scale: 2–3 replicas     │ ← Replica / scaling info
└────────────────────────────┘
```

Use `html=1;` and HTML formatting inside the `value` attribute for rich multi-line content:
```html
<b>chat-service</b><br><hr size="1"><font style="font-size:10px">STOMP WebSocket<br>Message CRUD · ACK · Pin<br>300K+ messages/day<br><i>2–3 replicas</i></font>
```

### B.7 Data Store Shapes

Use **cylinder shapes** for databases and data stores:
- `shape=cylinder3;` for standard databases
- Include: DB name, engine type, collection/table count, size estimates

### B.8 Legend Box

**Every diagram MUST have a legend.** Place it in the **top-right** or **bottom-right** corner inside a clearly bordered container.

A legend must decode:
1. **Color swatches** → what each fill color represents (tech stack or tier)
2. **Line styles** → solid = sync, dashed = async, dotted = external
3. **Arrow types** → what different arrowheads mean
4. **Shape types** → rectangle = service, cylinder = database, cloud = managed service

### B.9 Callout / Annotation Boxes

Use separate floating boxes (outside the main flow) for:
- **Shared Patterns** — "Circuit Breaker", "CQRS", "Event Sourcing", etc.
- **Scale & Performance** — "400 concurrent users", "99.9% SLA", etc.
- **Schema Patterns** — "Polymorphic (msg_type, event_src)", "TTL indexes"
- **Cross-Service Patterns** — "Chat → Flights: GET flights", "with Circuit Breaker + cache"

Style these with a **distinct border color** (e.g., dashed border, light background) to differentiate from architectural components.

### B.10 Alignment & Spacing Rules

| Rule | Standard |
|------|----------|
| **Grid snap** | Always enabled. `gridSize=10` |
| **Component sizing** | All components in the same tier should have **identical width**. Height can vary. |
| **Horizontal gap between columns** | 60–100px consistent gap (serves as dedicated arrow routing corridors) |
| **Vertical gap between components** | 30–50px consistent gap (serves as inter-component routing gutters) |
| **Container padding** | ≥20px between container edge and child components |
| **Arrow start/end** | Always connect to specific sides based on target relative direction |
| **Distribute evenly** | Components in the same column should be vertically distributed with equal spacing |

### B.11 Zero-Overlap & Clean Arrow Separation Architecture (Anti-Collision Rules)

> **CRITICAL REQUIREMENT:** In AI-generated architecture diagrams, the #1 visual defect is **overlapping arrows**, **arrows piercing through component boxes**, and **tangled spaghetti crossings**. Every line MUST have a clean, dedicated separation of concern. Follow these 8 anti-collision commandments strictly:

#### 1. Port Staggering (Never Stack Arrows on the Same Port)
When multiple arrows enter or exit the same side of a component box, **NEVER** attach them all to the midpoint `(0.5)`. Stagger the connection ports along the edge face so each line has its own dedicated connection point:

| Connections on Same Face ($N$) | Assigned Port Coordinates (`exitY` or `entryY` for L/R faces) |
|--------------------------------|---------------------------------------------------------------|
| **1 connection** | `0.50` (Center) |
| **2 connections** | `0.30` (Upper), `0.70` (Lower) |
| **3 connections** | `0.20` (Upper), `0.50` (Center), `0.80` (Lower) |
| **4 connections** | `0.15`, `0.38`, `0.62`, `0.85` |

*(For top or bottom faces, apply the same ratios to `exitX` or `entryX`.)*

#### 2. Multi-Track Routing Corridors (Never Share Coordinate Tracks)
The empty space between columns (60–100px) is a **Routing Corridor**. When multiple vertical lines travel between the same tiers:
- Allocate dedicated parallel **Tracks** separated by at least **20px–30px**.
- **Line 1 (near source):** `x = source_right + 25px`
- **Line 2 (middle):** `x = source_right + 50px`
- **Line 3 (near target):** `x = source_right + 75px`
- **RULE:** No two parallel line segments may share the exact same X or Y coordinate.

#### 3. Component Avoidance & Box Bypass (Overpass / Underpass)
**NEVER allow an arrow to cut across a box.** If a line connects Component A in Column 1 to Component C in Column 3, skipping Component B in Column 2:
- **DO NOT** let draw.io auto-route across Component B.
- Use explicit waypoints `<Array as="points">` to route through a dedicated bypass corridor:
  - **Overpass:** Route above the entire column: `y = min(column_boxes.y) - 40px`
  - **Underpass:** Route below the entire column: `y = max(column_boxes.bottom) + 40px`
  - Or route through the clean horizontal gutter between two boxes in Column 2 where no component exists.

#### 4. Functional Separation of Flow Channels (Separation of Concern)
Group connection types into distinct spatial channels across the canvas:
- **Upper Skyway (Top corridor):** Ingress traffic, client requests, API Gateway dispatch, and Auth token validation.
- **Main Street (Center level):** Direct synchronous East-West microservice orchestration (REST / gRPC).
- **Lower Underground (Bottom corridor):** Asynchronous event streams (Kafka / RabbitMQ), background event handlers, webhook callbacks, return response flows, and audit/telemetry logs.
- **Far Right Channel:** Database replication, cache synchronization, and cold storage persistence.

#### 5. Planar Component Ordering (Eliminate "X" Crossings by Layout)
Before drawing any lines, arrange boxes vertically to match their communication targets:
- If Service 1 calls Database A, and Service 2 calls Database B:
  - Place Service 1 and Database A on **Row 1 (Top)**.
  - Place Service 2 and Database B on **Row 2 (Bottom)**.
  - **Result:** Two clean horizontal parallel lines with ZERO crossings.
  - ❌ *Anti-Pattern:* Placing DB B on Row 1 and DB A on Row 2 creates an unavoidable diagonal "X" collision!

#### 6. Arc Jumps for Unavoidable Crossings (`jumpStyle=arc`)
When two perpendicular lines MUST cross:
- Add `jumpStyle=arc;jumpSize=6;` to the crossing edge style.
- Draw.io will automatically render a curved bridge/arc over the intersected line, visually separating the two signals.
- Always apply the jump to the secondary / async / cross-tier line, leaving the primary sync line continuous.

#### 7. Label Masking & Offset (No Label Collisions)
- Every edge label MUST have `labelBackgroundColor=#FFFFFF;` (or the canvas background color). This creates an opaque mask behind the text so the line never cuts through the label.
- When an edge turns or crosses near a junction, offset the label toward the source (`relative="1"; x="-0.3"`) or toward the target (`x="0.3"`), never right on top of a 90° bend or intersection.

#### 8. Perimeter Jetty Enforcement
Always specify `jettySize=auto;` or explicit `jettySize=20;` in edge styles. This guarantees that edges extend straight out from the box by at least 20px before making any turn, preventing ugly corner-clipping and edge collisions with the component's own border.

---

## Part C — Draw.io XML Reference (How to Generate)

### C.1 File Structure Template

```xml
<mxfile host="app.diagrams.net" compressed="false" type="device">
  <diagram id="diagram-1" name="Architecture">
    <mxGraphModel dx="1422" dy="762" grid="1" gridSize="10" guides="1" tooltips="1"
                  connect="1" arrows="1" fold="1" page="1" pageScale="1"
                  pageWidth="1654" pageHeight="1169" math="0" shadow="0">
      <root>
        <!-- Mandatory: Root container -->
        <mxCell id="0" />
        <!-- Mandatory: Default layer -->
        <mxCell id="1" parent="0" />

        <!-- Optional: Additional layers -->
        <mxCell id="layer-bg" value="Background" parent="0" />
        <mxCell id="layer-main" value="Main" parent="0" />
        <mxCell id="layer-annotations" value="Annotations" parent="0" />

        <!-- ALL shapes and edges below -->
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>
```

**Critical rules:**
- `compressed="false"` — always, for programmatic generation.
- `id="0"` and `id="1"` are mandatory and must be the first two cells.
- All IDs must be unique strings across the entire file.
- Use descriptive IDs (e.g., `svc-chat`, `db-mongo`, `edge-chat-to-mongo`) not numeric counters.
- `pageWidth="1654" pageHeight="1169"` for A3 landscape (recommended for complex architectures).

### C.2 Page Size Reference

| Page Size | Width | Height | Best For |
|-----------|-------|--------|----------|
| A4 Landscape | 1169 | 827 | Simple diagrams, L3 components |
| A3 Landscape | 1654 | 1169 | Standard HLD, most microservice diagrams |
| A2 Landscape | 2339 | 1654 | Large enterprise architectures |
| Custom Wide | 2400 | 1200 | Very wide L→R flow diagrams |

### C.3 Layer Strategy

Use layers to separate concerns and enable toggling visibility:

| Layer ID | Name | Contains | Z-Order |
|----------|------|----------|---------|
| `layer-bg` | Background | Tier columns, swim lane backgrounds, region boundaries | Bottom |
| `layer-main` | Main | All service boxes, data stores, edge components | Middle |
| `layer-edges` | Connections | All arrows and connectors | Above Main |
| `layer-annotations` | Annotations | Legend, callouts, pattern boxes, title | Top |

```xml
<!-- Layer definition -->
<mxCell id="layer-bg" value="Background" parent="0" />

<!-- Shape on this layer -->
<mxCell id="col-services" value="APPLICATION SERVICES"
        style="rounded=0;whiteSpace=wrap;html=1;fillColor=#f5f5f5;strokeColor=#CCCCCC;
               opacity=30;fontStyle=1;fontSize=14;verticalAlign=top;align=center;"
        vertex="1" parent="layer-bg">
  <mxGeometry x="500" y="40" width="400" height="900" as="geometry" />
</mxCell>
```

### C.4 Container / Group Pattern

To nest children inside a parent (e.g., services inside a K8s cluster):

```xml
<!-- Parent container -->
<mxCell id="cluster-aks" value="AKS Cluster"
        style="swimlane;whiteSpace=wrap;html=1;container=1;collapsible=0;
               fillColor=#EBF5FB;strokeColor=#2E86C1;rounded=1;arcSize=8;
               fontStyle=1;fontSize=14;startSize=30;"
        vertex="1" parent="1">
  <mxGeometry x="100" y="100" width="600" height="400" as="geometry" />
</mxCell>

<!-- Child inside container (coordinates RELATIVE to parent's top-left) -->
<mxCell id="svc-order" value="order-service"
        style="rounded=1;whiteSpace=wrap;html=1;fillColor=#d5e8d4;strokeColor=#82b366;"
        vertex="1" parent="cluster-aks">
  <mxGeometry x="40" y="60" width="160" height="80" as="geometry" />
</mxCell>
```

**Key:** Child coordinates are **local** to the parent container's origin.

### C.5 Shape Styles Quick Reference

#### Standard Service Box
```
rounded=1;whiteSpace=wrap;html=1;fillColor=#d5e8d4;strokeColor=#82b366;
fontColor=#333333;fontSize=12;fontStyle=1;verticalAlign=top;spacing=8;
```

#### Database Cylinder
```
shape=cylinder3;whiteSpace=wrap;html=1;boundedLbl=1;backgroundOutline=1;size=15;
fillColor=#e1d5e7;strokeColor=#9673a6;fontSize=11;verticalAlign=top;spacing=6;
```

#### External System Box
```
rounded=0;whiteSpace=wrap;html=1;fillColor=#f8cecc;strokeColor=#b85450;
fontColor=#333333;fontSize=12;fontStyle=1;dashed=1;dashPattern=8 8;
```

#### API Gateway / Edge
```
rounded=1;whiteSpace=wrap;html=1;fillColor=#f5f5f5;strokeColor=#666666;
fontColor=#333333;fontSize=12;fontStyle=1;shadow=1;
```

#### Kafka / Message Broker
```
shape=hexagon;perimeter=hexagonPerimeter2;whiteSpace=wrap;html=1;
fillColor=#fff0b3;strokeColor=#d4a017;fontSize=12;fontStyle=1;
```

#### Text-Only Label (No border, no fill)
```
text;html=1;align=center;verticalAlign=middle;resizable=0;points=[];
autosize=1;strokeColor=none;fillColor=none;fontSize=12;fontColor=#666666;
```

#### Legend Container
```
rounded=1;whiteSpace=wrap;html=1;fillColor=#FFFFFF;strokeColor=#333333;
strokeWidth=2;fontSize=11;verticalAlign=top;spacing=8;fontStyle=1;
shadow=1;arcSize=6;
```

#### Callout / Annotation Box
```
rounded=1;whiteSpace=wrap;html=1;fillColor=#FFF8E1;strokeColor=#F9A825;
strokeWidth=1;dashed=1;dashPattern=4 4;fontSize=10;verticalAlign=top;
spacing=6;fontStyle=0;align=left;
```

#### Swim Lane Column Background
```
rounded=0;whiteSpace=wrap;html=1;fillColor=#f9f9f9;strokeColor=#E0E0E0;
strokeWidth=1;opacity=50;fontStyle=1;fontSize=14;verticalAlign=top;
align=center;spacingTop=8;
```

### C.6 Edge Styles Quick Reference

> **Standard Edge Baseline:** Every edge MUST use `edgeStyle=orthogonalEdgeStyle;rounded=1;orthogonalLoop=1;jettySize=auto;jumpStyle=arc;jumpSize=6;labelBackgroundColor=#FFFFFF;`. The `jumpStyle=arc;` ensures that whenever another line crosses this edge, draw.io automatically draws a clean overpass arc rather than a messy intersecting collision!

#### Sync REST Call (Solid Arrow + Arc Jumps)
```xml
<mxCell id="edge-1" value="REST (HTTP/2)"
        style="edgeStyle=orthogonalEdgeStyle;rounded=1;orthogonalLoop=1;
               jettySize=auto;jumpStyle=arc;jumpSize=6;html=1;
               strokeColor=#333333;strokeWidth=1.5;fontColor=#333333;
               fontSize=10;labelBackgroundColor=#FFFFFF;
               endArrow=classic;endFill=1;"
        edge="1" source="svc-a" target="svc-b" parent="1">
  <mxGeometry relative="1" as="geometry" />
</mxCell>
```

#### Async Kafka / Event (Dashed Arrow + Arc Jumps)
```xml
<mxCell id="edge-2" value="Kafka: orders-topic"
        style="edgeStyle=orthogonalEdgeStyle;rounded=1;orthogonalLoop=1;
               jettySize=auto;jumpStyle=arc;jumpSize=6;html=1;
               strokeColor=#d4a017;strokeWidth=1.5;dashed=1;dashPattern=8 8;
               fontColor=#d4a017;fontSize=10;labelBackgroundColor=#FFFFFF;
               endArrow=open;endFill=0;"
        edge="1" source="svc-a" target="svc-b" parent="1">
  <mxGeometry relative="1" as="geometry" />
</mxCell>
```

#### WebSocket / STOMP (Bidirectional Dashed + Arc Jumps)
```xml
<mxCell id="edge-3" value="STOMP/WS"
        style="edgeStyle=orthogonalEdgeStyle;rounded=1;orthogonalLoop=1;
               jettySize=auto;jumpStyle=arc;jumpSize=6;html=1;
               strokeColor=#2E86C1;strokeWidth=1.5;dashed=1;dashPattern=4 4;
               fontColor=#2E86C1;fontSize=10;labelBackgroundColor=#FFFFFF;
               startArrow=classic;startFill=1;endArrow=classic;endFill=1;"
        edge="1" source="svc-a" target="svc-b" parent="1">
  <mxGeometry relative="1" as="geometry" />
</mxCell>
```

#### External System Inbound (Dotted + Arc Jumps)
```xml
<mxCell id="edge-4" value="Webhooks"
        style="edgeStyle=orthogonalEdgeStyle;rounded=1;orthogonalLoop=1;
               jettySize=auto;jumpStyle=arc;jumpSize=6;html=1;
               strokeColor=#b85450;strokeWidth=1;dashed=1;dashPattern=2 6;
               fontColor=#b85450;fontSize=10;labelBackgroundColor=#FFFFFF;
               endArrow=block;endFill=1;"
        edge="1" source="ext-sys" target="svc-a" parent="1">
  <mxGeometry relative="1" as="geometry" />
</mxCell>
```

### C.7 Fixed Connection Points & Multi-Port Staggering

Control exactly where arrows attach to shapes to prevent multiple lines stacking on the same spot:

```
Single Center Port:
exitX=1;exitY=0.5;exitDx=0;exitDy=0;    → exits from right-center
entryX=0;entryY=0.5;entryDx=0;entryDy=0; → enters at left-center
```

#### Staggered Multi-Port Formula:
When 2 or more arrows connect to the same side of a component, **assign distinct ports**:

```xml
<!-- Source Box: 2 Outgoing Arrows (Staggered on Right Edge) -->
<!-- Arrow 1 (Upper Port: Y=0.33) -->
style="...;exitX=1;exitY=0.33;exitDx=0;exitDy=0;..."

<!-- Arrow 2 (Lower Port: Y=0.67) -->
style="...;exitX=1;exitY=0.67;exitDx=0;exitDy=0;..."

<!-- Target Box: 2 Incoming Arrows (Staggered on Left Edge) -->
<!-- Arrow 1 entering Upper Port (Y=0.33) -->
style="...;entryX=0;entryY=0.33;entryDx=0;entryDy=0;..."

<!-- Arrow 2 entering Lower Port (Y=0.67) -->
style="...;entryX=0;entryY=0.67;entryDx=0;entryDy=0;..."
```

### C.8 Manual Waypoints for Multi-Track Corridors & Box Bypass

When multiple lines travel down the same corridor, or when an edge must bypass intermediate boxes, use explicit waypoints (`<Array as="points">`).

#### Pattern A: Parallel Lines in Same Corridor (Multi-Track Staggering)
Line 1 and Line 2 run between Column 2 (`x=400`) and Column 3 (`x=600`). Corridor is `x=400..600`.
- Line 1 uses Track 1 (`x=460`):
```xml
<mxCell id="edge-track-1" edge="1" source="svc-1" target="db-1" parent="1"
        style="edgeStyle=orthogonalEdgeStyle;rounded=1;jettySize=auto;jumpStyle=arc;jumpSize=6;html=1;exitX=1;exitY=0.35;entryX=0;entryY=0.5;">
  <mxGeometry relative="1" as="geometry">
    <Array as="points">
      <mxPoint x="460" y="180" />
      <mxPoint x="460" y="450" />
    </Array>
  </mxGeometry>
</mxCell>
```
- Line 2 uses Track 2 (`x=500`) — **40px separation, zero overlap!**:
```xml
<mxCell id="edge-track-2" edge="1" source="svc-2" target="db-2" parent="1"
        style="edgeStyle=orthogonalEdgeStyle;rounded=1;jettySize=auto;jumpStyle=arc;jumpSize=6;html=1;exitX=1;exitY=0.65;entryX=0;entryY=0.5;">
  <mxGeometry relative="1" as="geometry">
    <Array as="points">
      <mxPoint x="500" y="240" />
      <mxPoint x="500" y="550" />
    </Array>
  </mxGeometry>
</mxCell>
```

#### Pattern B: Box Bypass (Overpass / Underpass Corridor)
Service in Column 1 (`x=100`) talks directly to Database in Column 3 (`x=700`), bypassing API Services in Column 2 (`x=400`, height 100..400).
Route the edge through the **Underpass Corridor** (`y=480`) below all Column 2 boxes:
```xml
<mxCell id="edge-bypass" value="Async Audit Stream" edge="1" source="client-gateway" target="audit-store" parent="1"
        style="edgeStyle=orthogonalEdgeStyle;rounded=1;jettySize=20;jumpStyle=arc;jumpSize=6;html=1;
               strokeColor=#7E57C2;dashed=1;dashPattern=6 6;labelBackgroundColor=#FFFFFF;
               exitX=0.5;exitY=1;entryX=0.5;entryY=1;">
  <mxGeometry relative="1" as="geometry">
    <Array as="points">
      <mxPoint x="200" y="480" />  <!-- Drops into underpass -->
      <mxPoint x="800" y="480" />  <!-- Navigates cleanly under Column 2 -->
    </Array>
  </mxGeometry>
</mxCell>
```

### C.9 Anti-Collision Separation-of-Concerns Complete Pattern

Here is the complete template showing how a central API Gateway routes traffic cleanly to two separate services without line overlap or port collision:

```xml
<!-- ═══ SOURCE: API Gateway (2 Outgoing Connections Staggered) ═══ -->
<!-- Outgoing 1: To Order Service (Exits upper right face at Y=0.33) -->
<mxCell id="edge-gw-to-order" value="REST: /orders"
        style="edgeStyle=orthogonalEdgeStyle;rounded=1;orthogonalLoop=1;jettySize=20;
               jumpStyle=arc;jumpSize=6;html=1;strokeColor=#333333;strokeWidth=1.5;
               fontSize=10;labelBackgroundColor=#FFFFFF;endArrow=classic;endFill=1;
               exitX=1;exitY=0.33;entryX=0;entryY=0.5;"
        edge="1" source="gateway" target="svc-order" parent="1">
  <mxGeometry relative="1" as="geometry" />
</mxCell>

<!-- Outgoing 2: To Chat Service (Exits lower right face at Y=0.67) -->
<mxCell id="edge-gw-to-chat" value="STOMP: /ws"
        style="edgeStyle=orthogonalEdgeStyle;rounded=1;orthogonalLoop=1;jettySize=20;
               jumpStyle=arc;jumpSize=6;html=1;strokeColor=#2E86C1;strokeWidth=1.5;
               dashed=1;dashPattern=4 4;fontSize=10;labelBackgroundColor=#FFFFFF;
               endArrow=classic;endFill=1;exitX=1;exitY=0.67;entryX=0;entryY=0.5;"
        edge="1" source="gateway" target="svc-chat" parent="1">
  <mxGeometry relative="1" as="geometry" />
</mxCell>
```

### C.10 HTML Value Formatting for Rich Labels

Use HTML inside the `value` attribute for multi-line, styled component boxes:

```xml
<mxCell id="svc-chat" vertex="1" parent="1"
        value="&lt;b&gt;chat-service&lt;/b&gt;&lt;br&gt;&lt;hr size=&quot;1&quot;/&gt;&lt;font style=&quot;font-size:10px&quot;&gt;STOMP WebSocket&lt;br&gt;Message CRUD · ACK · Pin&lt;br&gt;E2E CDRS Encryption&lt;br&gt;300K+ 1:1 messages/day&lt;br&gt;&lt;i&gt;2–3 replicas&lt;/i&gt;&lt;/font&gt;"
        style="rounded=1;whiteSpace=wrap;html=1;fillColor=#d5e8d4;strokeColor=#82b366;
               fontSize=12;verticalAlign=top;spacing=8;">
  <mxGeometry x="460" y="200" width="200" height="140" as="geometry" />
</mxCell>
```

**Important:** HTML inside XML attributes must be entity-encoded:
- `<` → `&lt;`
- `>` → `&gt;`
- `"` → `&quot;`
- `&` → `&amp;`

---

## Part D — Generation Workflow (Step-by-Step)

When asked to create an architecture diagram from a codebase, follow this exact workflow:

### Step 1: Analyze the Codebase
- Identify all deployable units (services, frontends, databases, brokers).
- Map communication protocols between them.
- Identify external systems.
- Note the tech stack for each component.

### Step 2: Choose Diagram Type & Zoom Level
- Ask/decide: Is this HLD (L1/L2) or LLD (L3)?
- Choose flow direction: Left-to-Right or Top-to-Bottom.

### Step 3: Define the Layout Grid
- Assign tier columns (External → Edge → Services → Data).
- Calculate required canvas size.
- Determine how many components per column.

### Step 4: Assign Visual Identity
- Map each tech stack / tier to a color from the semantic palette.
- Define which line styles represent which protocols.

### Step 5: Generate the XML
Build the XML in this order:
1. File structure (`<mxfile>`, `<diagram>`, `<mxGraphModel>`, `<root>`)
2. Mandatory cells (`id="0"`, `id="1"`)
3. Layer definitions
4. Background swim lane columns (on background layer)
5. Diagram title and subtitle (on annotation layer)
6. Component boxes — left to right, top to bottom (on main layer)
   - *Planar order check:* Order boxes vertically to align with target components to avoid "X" crossings.
7. Data store cylinders (on main layer)
8. External system boxes (on main layer)
9. Edge connectors — sync first, then async:
   - Assign staggered ports (`exitY`, `entryY`) for boxes with multiple connections.
   - Allocate distinct coordinate tracks (separated by ≥20px) in corridors.
   - Insert explicit waypoints for non-adjacent bypasses around boxes.
   - Include `jumpStyle=arc;jumpSize=6;labelBackgroundColor=#FFFFFF;` on every edge.
10. Legend box (on annotation layer)
11. Callout / annotation boxes (on annotation layer)

### Step 6: Validate (Zero-Overlap & Quality Audit)
- **Zero Box Occlusion:** Verify NO arrow cuts across or pierces any component box.
- **Port Staggering:** Multiple connections to the same box have distinct, staggered ports (never all at `0.5`).
- **Track Separation:** Parallel lines in routing corridors maintain at least 20px–30px separation (no collinear superimposition).
- **Line Jumps:** Every edge has `jumpStyle=arc;jumpSize=6;` so intersections render clean bridges.
- **Label Masking:** Every edge label has `labelBackgroundColor=#FFFFFF;` and is clearly readable.
- **Protocol Labeled:** Every arrow has a clear protocol/purpose label (REST, Kafka, gRPC, etc.).
- **Rich Component Values:** Every component has a descriptive multi-line HTML value.
- **Legend Completeness:** Legend decodes all colors and line types used.
- **Alignment:** All components in the same tier share identical widths and vertical alignment.
- **No Orphan Nodes:** Every deployable unit is connected.

### Step 7: Save
- Save as `{diagram-name}.drawio` in the project's `docs/` or `architecture/` directory.
- The file opens directly in draw.io / diagrams.net.

---

## Part E — Anti-Patterns (What NOT To Do)

| ❌ Anti-Pattern | ✅ Correct Approach |
|----------------|-------------------|
| **Arrows overlapping / piercing through boxes** | Dedicated overpass/underpass bypass corridors with explicit waypoints |
| **Multiple arrows clumping on the same port `(0.5)`** | Stagger ports along face (`exitY=0.25`, `0.5`, `0.75` for $N=3$) |
| **Parallel lines superimposing on the same coordinate track** | Multi-track corridor routing with ≥20px separation between lines |
| **Flat messy crossings where lines intersect** | `jumpStyle=arc;jumpSize=6;` to render curved overpass bridges |
| **Arrow labels overlapping lines or other labels** | `labelBackgroundColor=#FFFFFF;` mask and offset positions (`x="-0.3"` or `0.3`) |
| Rainbow of random colors with no meaning | Semantic color palette: each color = a role/tier |
| Unlabeled arrows | Every arrow labeled with protocol (REST, Kafka, gRPC) |
| Everything on one diagram regardless of complexity | Separate L1/L2/L3 diagrams for different audiences |
| Tiny font that's unreadable when exported | Minimum 10px for details, 12px for names, 14px for titles |
| Diagonal arrows crossing the entire canvas | Orthogonal routing with waypoints and line jumps |
| No legend | Legend box always present in a corner |
| Components randomly scattered with no alignment | Grid-snapped, column-aligned, evenly distributed |
| Using generic rectangles for everything | Cylinders for DBs, hexagons for brokers, clouds for managed services |
| Hardcoded pixel coordinates that break on resize | Use containers with relative child positioning |
| Compressed XML that can't be diffed in Git | `compressed="false"` always |

---

## Part F — Complete Minimal Example

Below is a minimal but complete draw.io XML showing the patterns in action:

```xml
<mxfile host="app.diagrams.net" compressed="false">
  <diagram id="arch-hld" name="HLD">
    <mxGraphModel dx="1422" dy="762" grid="1" gridSize="10" guides="1"
                  tooltips="1" connect="1" arrows="1" fold="1" page="1"
                  pageScale="1" pageWidth="1654" pageHeight="1169"
                  math="0" shadow="0">
      <root>
        <mxCell id="0" />
        <mxCell id="1" parent="0" />

        <!-- ═══ TITLE ═══ -->
        <mxCell id="title" value="&lt;b&gt;System Architecture — HLD&lt;/b&gt;&lt;br&gt;&lt;font style=&quot;font-size:11px;color:#888&quot;&gt;Left-to-Right: Clients → Gateway → Services → Data&lt;/font&gt;"
                style="text;html=1;align=center;verticalAlign=middle;resizable=0;
                       points=[];autosize=1;strokeColor=none;fillColor=none;fontSize=18;"
                vertex="1" parent="1">
          <mxGeometry x="500" y="20" width="500" height="50" as="geometry" />
        </mxCell>

        <!-- ═══ SWIM LANE BACKGROUNDS ═══ -->
        <mxCell id="col-clients" value="CLIENTS"
                style="rounded=0;whiteSpace=wrap;html=1;fillColor=#f9f9f9;strokeColor=#E0E0E0;
                       opacity=40;fontStyle=1;fontSize=13;verticalAlign=top;align=center;"
                vertex="1" parent="1">
          <mxGeometry x="40" y="80" width="200" height="500" as="geometry" />
        </mxCell>

        <mxCell id="col-edge" value="EDGE"
                style="rounded=0;whiteSpace=wrap;html=1;fillColor=#f9f9f9;strokeColor=#E0E0E0;
                       opacity=40;fontStyle=1;fontSize=13;verticalAlign=top;align=center;"
                vertex="1" parent="1">
          <mxGeometry x="280" y="80" width="200" height="500" as="geometry" />
        </mxCell>

        <mxCell id="col-services" value="SERVICES"
                style="rounded=0;whiteSpace=wrap;html=1;fillColor=#f9f9f9;strokeColor=#E0E0E0;
                       opacity=40;fontStyle=1;fontSize=13;verticalAlign=top;align=center;"
                vertex="1" parent="1">
          <mxGeometry x="520" y="80" width="250" height="500" as="geometry" />
        </mxCell>

        <mxCell id="col-data" value="DATA STORES"
                style="rounded=0;whiteSpace=wrap;html=1;fillColor=#f9f9f9;strokeColor=#E0E0E0;
                       opacity=40;fontStyle=1;fontSize=13;verticalAlign=top;align=center;"
                vertex="1" parent="1">
          <mxGeometry x="810" y="80" width="200" height="500" as="geometry" />
        </mxCell>

        <!-- ═══ CLIENT ═══ -->
        <mxCell id="client-web" value="&lt;b&gt;Web App&lt;/b&gt;&lt;br&gt;&lt;font style=&quot;font-size:10px&quot;&gt;Next.js 15&lt;br&gt;React 19&lt;/font&gt;"
                style="rounded=1;whiteSpace=wrap;html=1;fillColor=#dae8fc;strokeColor=#6c8ebf;
                       fontSize=12;verticalAlign=middle;spacing=6;"
                vertex="1" parent="1">
          <mxGeometry x="70" y="200" width="140" height="70" as="geometry" />
        </mxCell>

        <!-- ═══ EDGE ═══ -->
        <mxCell id="gateway" value="&lt;b&gt;API Gateway&lt;/b&gt;&lt;br&gt;&lt;font style=&quot;font-size:10px&quot;&gt;Spring Cloud Gateway&lt;br&gt;Rate Limiting&lt;br&gt;JWT Auth&lt;/font&gt;"
                style="rounded=1;whiteSpace=wrap;html=1;fillColor=#f5f5f5;strokeColor=#666666;
                       fontSize=12;verticalAlign=top;spacing=6;shadow=1;"
                vertex="1" parent="1">
          <mxGeometry x="300" y="180" width="160" height="100" as="geometry" />
        </mxCell>

        <!-- ═══ SERVICE ═══ -->
        <mxCell id="svc-order" value="&lt;b&gt;order-service&lt;/b&gt;&lt;br&gt;&lt;hr size=&quot;1&quot;/&gt;&lt;font style=&quot;font-size:10px&quot;&gt;Order CRUD&lt;br&gt;Payment Integration&lt;br&gt;Spring Boot 3.5&lt;br&gt;&lt;i&gt;2–3 replicas&lt;/i&gt;&lt;/font&gt;"
                style="rounded=1;whiteSpace=wrap;html=1;fillColor=#d5e8d4;strokeColor=#82b366;
                       fontSize=12;verticalAlign=top;spacing=6;"
                vertex="1" parent="1">
          <mxGeometry x="550" y="170" width="190" height="120" as="geometry" />
        </mxCell>

        <!-- ═══ DATA STORE ═══ -->
        <mxCell id="db-mongo" value="&lt;b&gt;MongoDB&lt;/b&gt;&lt;br&gt;&lt;font style=&quot;font-size:10px&quot;&gt;orders_db&lt;br&gt;3 collections&lt;br&gt;Atlas 7.x&lt;/font&gt;"
                style="shape=cylinder3;whiteSpace=wrap;html=1;boundedLbl=1;
                       backgroundOutline=1;size=15;fillColor=#e1d5e7;strokeColor=#9673a6;
                       fontSize=12;verticalAlign=top;spacing=6;"
                vertex="1" parent="1">
          <mxGeometry x="840" y="180" width="140" height="100" as="geometry" />
        </mxCell>

        <!-- ═══ EDGES (With Anti-Collision & Arc Jumps) ═══ -->
        <mxCell id="e-client-gw" value="HTTPS"
                style="edgeStyle=orthogonalEdgeStyle;rounded=1;orthogonalLoop=1;
                       jettySize=auto;jumpStyle=arc;jumpSize=6;html=1;strokeColor=#333333;strokeWidth=1.5;
                       fontSize=10;labelBackgroundColor=#FFFFFF;endArrow=classic;endFill=1;
                       exitX=1;exitY=0.5;entryX=0;entryY=0.5;"
                edge="1" source="client-web" target="gateway" parent="1">
          <mxGeometry relative="1" as="geometry" />
        </mxCell>

        <mxCell id="e-gw-svc" value="REST"
                style="edgeStyle=orthogonalEdgeStyle;rounded=1;orthogonalLoop=1;
                       jettySize=auto;jumpStyle=arc;jumpSize=6;html=1;strokeColor=#333333;strokeWidth=1.5;
                       fontSize=10;labelBackgroundColor=#FFFFFF;endArrow=classic;endFill=1;
                       exitX=1;exitY=0.5;entryX=0;entryY=0.5;"
                edge="1" source="gateway" target="svc-order" parent="1">
          <mxGeometry relative="1" as="geometry" />
        </mxCell>

        <mxCell id="e-svc-db" value="MongoDB Driver"
                style="edgeStyle=orthogonalEdgeStyle;rounded=1;orthogonalLoop=1;
                       jettySize=auto;jumpStyle=arc;jumpSize=6;html=1;strokeColor=#9673a6;strokeWidth=1.5;
                       fontSize=10;labelBackgroundColor=#FFFFFF;endArrow=classic;endFill=1;
                       exitX=1;exitY=0.5;entryX=0;entryY=0.5;"
                edge="1" source="svc-order" target="db-mongo" parent="1">
          <mxGeometry relative="1" as="geometry" />
        </mxCell>

        <!-- ═══ LEGEND ═══ -->
        <mxCell id="legend" value="&lt;b&gt;Legend&lt;/b&gt;&lt;br&gt;&lt;hr size=&quot;1&quot;/&gt;&lt;font style=&quot;font-size:10px&quot;&gt;🟩 Spring Boot Service&lt;br&gt;🟦 Next.js Frontend&lt;br&gt;🟪 Data Store&lt;br&gt;⬜ Edge / Infra&lt;br&gt;&lt;br&gt;── Sync REST&lt;br&gt;- - Async Kafka&lt;/font&gt;"
                style="rounded=1;whiteSpace=wrap;html=1;fillColor=#FFFFFF;strokeColor=#333333;
                       strokeWidth=2;fontSize=12;verticalAlign=top;spacing=8;
                       fontStyle=1;shadow=1;align=left;"
                vertex="1" parent="1">
          <mxGeometry x="1050" y="80" width="180" height="200" as="geometry" />
        </mxCell>

      </root>
    </mxGraphModel>
  </diagram>
</mxfile>
```

---

## Part G — Diagram Quality Checklist

Use this before delivering any generated diagram:

### Structure
- [ ] Correct C4 level chosen for the audience
- [ ] Single consistent flow direction (L→R or T→B)
- [ ] Components grouped into clear tier columns / swim lanes
- [ ] Containers used for cluster/region/VPC nesting

### Visual
- [ ] Semantic color palette (≤7 colors, each with a meaning)
- [ ] Typography hierarchy respected (title > header > name > detail)
- [ ] All components in same tier have identical width
- [ ] Consistent spacing (equal gaps between components)
- [ ] Grid snapping enabled (`grid="1" gridSize="10"`)

### Connections & Anti-Collision (Zero Overlap)
- [ ] **Zero Box Occlusion:** No arrow pierces or cuts across any component box (explicit overpass/underpass waypoints used)
- [ ] **Port Staggering:** Multiple connections to the same box use distinct ports (`0.25`, `0.5`, `0.75`), never all at `0.5`
- [ ] **Track Separation:** Parallel lines in routing corridors maintain ≥20px track separation (never collinear)
- [ ] **Arc Jumps:** Every crossing edge has `jumpStyle=arc;jumpSize=6;` to render curved overpass bridges
- [ ] **Label Masking:** Every edge label has `labelBackgroundColor=#FFFFFF;` to prevent lines running through text
- [ ] **Planar Ordering:** Components ordered vertically to match target topology and eliminate unnecessary crossings
- [ ] **Flow Separation:** Ingress/Sync/Async/Data channels segregated into distinct vertical bands
- [ ] Every arrow is labeled with protocol/purpose
- [ ] Solid lines for sync, dashed for async, dotted for external
- [ ] Orthogonal routing (`orthogonalEdgeStyle`) — no diagonal chaos
- [ ] Arrow direction follows the declared flow

### Metadata
- [ ] Diagram title with subtitle explaining flow direction
- [ ] Legend present and decodes ALL colors + line styles
- [ ] Callout boxes for patterns, scale info, notes
- [ ] Component boxes contain: name, responsibilities, tech, replicas

### Technical (XML)
- [ ] `compressed="false"` set
- [ ] Mandatory cells `id="0"` and `id="1"` present
- [ ] All IDs are unique and descriptive
- [ ] HTML in `value` attributes is properly entity-encoded
- [ ] Child coordinates are relative to parent containers
