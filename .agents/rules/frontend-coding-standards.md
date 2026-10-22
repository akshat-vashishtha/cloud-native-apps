# Next.js & Strict TypeScript Frontend Standards

> **Purpose:** Full frontend architecture & code standards for this repository.  
> **Scope:** The `web/` SPA and any future React/Next.js frontend.  
> **Source of truth for structure:** Verbatim copy of [coding-standard.md](./coding-standard.md) (sections 1–6 below), plus project-specific addenda (section 7).  
> Agents and developers MUST follow this document for all frontend work.

---

## 1. Architectural Overview

The frontend follows a **Clean Layered Architecture** with strict separation of concerns:

```
┌───────────────────────────────────────────────────────────────┐
│                       App / Route Layer                       │
│                        (src/app/*)                            │
│   • Next.js route pages                                       │
│   • Only job: call the Facade hook, pass state to View        │
└────────────────────────────────┬──────────────────────────────┘
                                 │
┌────────────────────────────────▼──────────────────────────────┐
│                     UI / Presentation Layer                    │
│                         (src/ui/*)                            │
│   • Stateless, declarative React components                   │
│   • Receives all data & callbacks via props                   │
│   • Has ZERO knowledge of APIs, URLs, HTTP, or services       │
└────────────────────────────────▲──────────────────────────────┘
                                 │  state + handlers
                                 │
┌────────────────────────────────┴──────────────────────────────┐
│                         Core Layer                            │
│                        (src/core/*)                           │
│                                                               │
│   ┌─────────────────────────────────────────────────────────┐ │
│   │ controller_hooks/                                       │ │
│   │   ├── operations/    (atomic React hooks per action)    │ │
│   │   └── facades/       (page-level orchestrator hooks)    │ │
│   ├─────────────────────────────────────────────────────────┤ │
│   │ validator/           (pure TS validation logic)         │ │
│   ├─────────────────────────────────────────────────────────┤ │
│   │ service/             (interface + implementation)       │ │
│   ├─────────────────────────────────────────────────────────┤ │
│   │ client/              (HTTP transport + error parsing)   │ │
│   ├─────────────────────────────────────────────────────────┤ │
│   │ dto/                 (request/response data contracts)  │ │
│   ├─────────────────────────────────────────────────────────┤ │
│   │ types/               (domain enums & literal types)     │ │
│   └─────────────────────────────────────────────────────────┘ │
└───────────────────────────────────────────────────────────────┘
```

### Core Principles

| # | Principle | What it means |
|---|-----------|---------------|
| 1 | **Separation of Concerns** | UI components never import services, HTTP clients, or know API URLs. Core layer never renders JSX (except `controller_hooks`). |
| 2 | **Single Class / Interface / Enum per File** | Every file exports exactly **one** primary artifact. File name matches the artifact name in PascalCase. |
| 3 | **Dependency Inversion** | Upper layers depend on **interfaces** (e.g., `{Entity}Service`), not on concrete implementations (`{Entity}ServiceImpl`). |
| 4 | **Framework-Agnostic Core** | Everything inside `types/`, `dto/`, `service/`, `client/`, and `validator/` is **pure TypeScript** — no React, no hooks, no JSX. Only `controller_hooks/` uses React. |
| 5 | **Contract Alignment** | DTOs and enums mirror backend API contracts (e.g., Spring Boot REST) exactly 1:1. |

---

## 2. Universal File Creation Rules

These rules apply **across all subdirectories** in `src/core/`.

### Rule 1: Single Artifact per File
Every `.ts` file must contain exactly **one** top-level export:
- One `enum`, OR
- One `interface`, OR
- One `class`

**Never** combine multiple enums, interfaces, or classes in a single file.

### Rule 2: File Name = Artifact Name
The filename must exactly match the exported artifact name, in **PascalCase**, with a `.ts` extension.

| Artifact | Filename |
|----------|----------|
| `enum OrderStatus` | `OrderStatus.ts` |
| `interface ProductResponseDto` | `ProductResponseDto.ts` |
| `class PaymentService` | `PaymentService.ts` |
| `function useCreateOrder` | `useCreateOrder.ts` |

### Rule 3: Named Exports Only
Always use `export` (named exports). Never use `export default` for core layer artifacts.

```typescript
// ✅ Correct
export enum OrderStatus { ... }
export interface ProductResponseDto { ... }
export class PaymentServiceImpl implements PaymentService { ... }

// ❌ Wrong
export default class PaymentServiceImpl { ... }
```

### Rule 4: Path Aliases for Imports
All cross-directory imports within `src/` must use the `@/` path alias. Never use relative paths that cross directory boundaries.

```typescript
// ✅ Correct
import { OrderStatus } from "@/core/types/OrderStatus";
import { OrderResponseDto } from "@/core/dto/OrderResponseDto";

// ❌ Wrong
import { OrderStatus } from "../../types/OrderStatus";
```

### Rule 5: No Circular Dependencies
Dependencies must flow **downward** through the layers. The dependency direction is:

```
controller_hooks → service → client → dto → types
       ↓
   validator → dto → types
```

A file in `types/` must **never** import from `dto/`, `service/`, or `controller_hooks/`.

---

## 3. Layer-by-Layer Standards

> In all templates below, `{Entity}` is a placeholder for your domain name (e.g., `User`, `Product`, `Order`, `Invoice`).

---

### 3.1 `types/` — Domain Enums & Literal Types

#### Purpose
Define categorical domain values — statuses, roles, categories, priorities — as TypeScript enums. This eliminates magic strings across the entire codebase.

#### File Rules

| Rule | Standard |
|------|----------|
| **One enum per file** | `{EnumName}.ts` contains exactly one `export enum {EnumName}` |
| **Naming** | PascalCase noun describing the category (e.g., `OrderStatus`, `PaymentMethod`, `UserRole`) |
| **Values** | Always assign **explicit string literals** matching backend API values |
| **Imports from** | Nothing. Types are leaf nodes — they import from no other core package |
| **Imported by** | `dto/`, `validator/`, `service/`, `controller_hooks/`, `ui/` |

#### Template

```typescript
// src/core/types/{EnumName}.ts

export enum {EnumName} {
    VALUE_ONE = "VALUE_ONE",
    VALUE_TWO = "VALUE_TWO",
    VALUE_THREE = "VALUE_THREE",
}
```

#### Example

```typescript
// src/core/types/OrderStatus.ts
export enum OrderStatus {
    PENDING = "PENDING",
    CONFIRMED = "CONFIRMED",
    SHIPPED = "SHIPPED",
    DELIVERED = "DELIVERED",
    CANCELLED = "CANCELLED",
}
```

#### ❌ Anti-Patterns
```typescript
// ❌ Numeric enum (breaks JSON serialization)
export enum OrderStatus {
    PENDING,     // 0 at runtime — not what the API sends
    CONFIRMED,   // 1
}

// ❌ Multiple enums in one file
export enum OrderStatus { ... }
export enum PaymentStatus { ... }  // Must be its own file
```

---

### 3.2 `dto/` — Data Transfer Objects

#### Purpose
Define the exact shape of data exchanged with backend APIs. These are **contracts** — they must mirror the backend request/response JSON structures field-for-field.

#### File Rules

| Rule | Standard |
|------|----------|
| **One interface per file** | Each file exports exactly one `interface` |
| **Always `interface`, never `class`** | DTOs are pure data shapes. Using `interface` means zero runtime overhead and natural JSON compatibility (`as` type assertion is sufficient) |
| **Naming convention** | `{Entity}{Action}RequestDto.ts` for requests, `{Entity}ResponseDto.ts` for responses |
| **Use domain enums** | Categorical fields must reference enums from `types/`, never raw strings |
| **Dates & timestamps** | Represented as `string` (ISO 8601 format), not `Date` objects |
| **Imports from** | `types/` only |
| **Imported by** | `service/`, `validator/`, `controller_hooks/`, `ui/` |

#### Naming Convention Table

| DTO Purpose | Filename | Interface Name |
|-------------|----------|----------------|
| Create request body | `{Entity}CreateRequestDto.ts` | `{Entity}CreateRequestDto` |
| Update request body | `{Entity}UpdateRequestDto.ts` | `{Entity}UpdateRequestDto` |
| API response body | `{Entity}ResponseDto.ts` | `{Entity}ResponseDto` |
| Standardized error | `ErrorResponseDto.ts` | `ErrorResponseDto` |

#### Templates

**Response DTO:**
```typescript
// src/core/dto/{Entity}ResponseDto.ts

import { {EntityRole} } from "@/core/types/{EntityRole}";
import { {EntityStatus} } from "@/core/types/{EntityStatus}";

export interface {Entity}ResponseDto {
    id: string;
    // ... domain fields ...
    role: {EntityRole};
    status: {EntityStatus};
    createdAt: string;     // ISO 8601
    updatedAt: string;     // ISO 8601
}
```

**Create Request DTO:**
```typescript
// src/core/dto/{Entity}CreateRequestDto.ts

import { {EntityRole} } from "@/core/types/{EntityRole}";

export interface {Entity}CreateRequestDto {
    // Only fields required to create.
    // Never include: id, createdAt, updatedAt, status (server-assigned fields)
    name: string;
    email: string;
    role: {EntityRole};
}
```

**Update Request DTO:**
```typescript
// src/core/dto/{Entity}UpdateRequestDto.ts

import { {EntityRole} } from "@/core/types/{EntityRole}";
import { {EntityStatus} } from "@/core/types/{EntityStatus}";

export interface {Entity}UpdateRequestDto {
    // Only mutable fields. Never include: id (passed as path param), createdAt, updatedAt
    name: string;
    role: {EntityRole};
    status: {EntityStatus};
}
```

**Error Response DTO (one per project — shared):**
```typescript
// src/core/dto/ErrorResponseDto.ts

export interface ErrorResponseDto {
    timestamp: string;
    status: number;
    error: string;
    message: string;
    path: string;
    fieldErrors?: Record<string, string>;   // field-level validation errors from backend
}
```

#### ❌ Anti-Patterns
```typescript
// ❌ Using class instead of interface
export class OrderResponseDto {           // Creates runtime overhead for pure data
    constructor(public id: string) {}
}

// ❌ Using raw string instead of enum
export interface OrderResponseDto {
    status: string;                        // Should be: status: OrderStatus
}

// ❌ Multiple DTOs in one file
export interface OrderCreateRequestDto { ... }
export interface OrderUpdateRequestDto { ... }   // Must be separate files
```

---

### 3.3 `client/` — HTTP Transport Layer

#### Purpose
Provide a generic, reusable HTTP client that wraps the native `fetch()` API. This is the **only place in the entire project** that calls `fetch()` directly. Everything else uses this client.

#### File Rules

| Rule | Standard |
|------|----------|
| **Two files only** | `HttpClient.ts` (transport) + `ApiClientError.ts` (typed error) |
| **Entity-agnostic** | The HTTP client knows nothing about domain entities. It is a generic `<T>` transport layer |
| **Singleton export** | Export both the `class` (for testing/DI) and a pre-configured singleton instance |
| **Error mapping** | Non-OK responses are parsed into `ErrorResponseDto` and thrown as `ApiClientError` |
| **Imports from** | `dto/` (only `ErrorResponseDto`) |
| **Imported by** | `service/` only |

#### File 1: `ApiClientError.ts`

This is a custom `Error` subclass that captures structured error details from the backend. It enables the upper layers to access typed error information (status code, field-level errors, etc.) without parsing raw responses.

```typescript
// src/core/client/ApiClientError.ts

import { ErrorResponseDto } from "@/core/dto/ErrorResponseDto";

export class ApiClientError extends Error {
    public readonly status: number;
    public readonly error: string;
    public readonly path: string;
    public readonly fieldErrors?: Record<string, string>;

    constructor(errorDto: ErrorResponseDto) {
        super(errorDto.message);
        this.status = errorDto.status;
        this.error = errorDto.error;
        this.path = errorDto.path;
        this.fieldErrors = errorDto.fieldErrors;
        this.name = "ApiClientError";
    }

    // Factory method for network-level failures (no response body available)
    public static fromNetwork(message: string): ApiClientError {
        return new ApiClientError({
            timestamp: new Date().toISOString(),
            status: 0,
            error: "Network Error",
            message,
            path: "",
        });
    }
}
```

**Key design decisions:**
- Extends `Error` so it works with standard `try/catch` and `instanceof` checks.
- Properties are `readonly` — errors are immutable facts.
- `fromNetwork()` factory handles cases where the server is unreachable (no JSON body to parse).

#### File 2: `HttpClient.ts`

```typescript
// src/core/client/HttpClient.ts

import { ApiClientError } from "@/core/client/ApiClientError";
import { ErrorResponseDto } from "@/core/dto/ErrorResponseDto";

const DEFAULT_BASE_URL: string = "http://localhost:8080";

export class HttpClient {
    private readonly baseUrl: string;

    constructor(baseUrl: string = DEFAULT_BASE_URL) {
        this.baseUrl = baseUrl;
    }

    public async get<T>(path: string): Promise<T> {
        return this.request<T>(path, "GET");
    }

    public async post<T, B>(path: string, body: B): Promise<T> {
        return this.request<T>(path, "POST", body);
    }

    public async put<T, B>(path: string, body: B): Promise<T> {
        return this.request<T>(path, "PUT", body);
    }

    public async delete<T>(path: string): Promise<T> {
        return this.request<T>(path, "DELETE");
    }

    private async request<T>(path: string, method: string, body?: unknown): Promise<T> {
        const url = `${this.baseUrl}${path}`;

        const response = await fetch(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: body ? JSON.stringify(body) : undefined,
        });

        if (!response.ok) {
            const errorDto = (await response.json()) as ErrorResponseDto;
            throw new ApiClientError(errorDto);
        }

        // Handle 204 No Content (e.g., DELETE responses)
        if (response.status === 204) {
            return undefined as unknown as T;
        }

        return (await response.json()) as T;
    }
}

// Pre-configured singleton for application-wide use
export const httpClient = new HttpClient();
```

**Key design decisions:**
- Generic `<T>` return types — the client doesn't know about domain DTOs.
- `<T, B>` on `post` / `put` — enforces type safety on both the request body and response.
- Export both `HttpClient` class (for unit tests with custom base URLs) **and** `httpClient` singleton (for production use).

#### ❌ Anti-Patterns
```typescript
// ❌ Calling fetch directly in a service or component
const response = await fetch("/api/v1/orders");     // Must go through HttpClient

// ❌ Creating entity-specific HTTP client files
// OrderHttpClient.ts, UserHttpClient.ts             // One generic HttpClient serves all
```

---

### 3.4 `service/` — Domain Service Layer

#### Purpose
Encapsulate all API endpoint logic for a specific domain entity. Each entity gets exactly **two files**: an **interface** (contract) and an **implementation** (concrete class).

#### File Rules

| Rule | Standard |
|------|----------|
| **Two files per entity** | `{Entity}Service.ts` (interface) + `{Entity}ServiceImpl.ts` (class) |
| **Interface defines the contract** | Lists all async methods, their parameters, and return types using DTOs |
| **Implementation wires to HttpClient** | Accepts `HttpClient` via constructor with a default singleton fallback |
| **Singleton export** | `{Entity}ServiceImpl.ts` exports both the class AND a pre-instantiated singleton typed as the interface |
| **Endpoint stored as private field** | API path (e.g., `/api/v1/orders`) is a `private readonly` field, not scattered across methods |
| **Imports from** | `dto/`, `client/` |
| **Imported by** | `controller_hooks/` only |

#### File 1: `{Entity}Service.ts` — Interface

The interface defines **what** can be done, without revealing **how**. Upper layers (hooks) depend on this interface.

```typescript
// src/core/service/{Entity}Service.ts

import { {Entity}ResponseDto } from "@/core/dto/{Entity}ResponseDto";
import { {Entity}CreateRequestDto } from "@/core/dto/{Entity}CreateRequestDto";
import { {Entity}UpdateRequestDto } from "@/core/dto/{Entity}UpdateRequestDto";

export interface {Entity}Service {
    getAll(): Promise<{Entity}ResponseDto[]>;
    getById(id: string): Promise<{Entity}ResponseDto>;
    create(dto: {Entity}CreateRequestDto): Promise<{Entity}ResponseDto>;
    update(id: string, dto: {Entity}UpdateRequestDto): Promise<{Entity}ResponseDto>;
    delete(id: string): Promise<void>;
}
```

**Key rules:**
- Every method returns a `Promise` wrapping DTOs.
- No HTTP details (URLs, methods, headers) leak into the interface.
- Method names are domain verbs: `getAll`, `create`, `update`, `delete` — not `fetchOrders` or `postOrder`.

#### File 2: `{Entity}ServiceImpl.ts` — Implementation

```typescript
// src/core/service/{Entity}ServiceImpl.ts

import { {Entity}Service } from "@/core/service/{Entity}Service";
import { {Entity}ResponseDto } from "@/core/dto/{Entity}ResponseDto";
import { {Entity}CreateRequestDto } from "@/core/dto/{Entity}CreateRequestDto";
import { {Entity}UpdateRequestDto } from "@/core/dto/{Entity}UpdateRequestDto";
import { HttpClient, httpClient } from "@/core/client/HttpClient";

export class {Entity}ServiceImpl implements {Entity}Service {

    private readonly endpoint = "/api/v1/{entities}";   // plural, lowercase
    private readonly client: HttpClient;

    constructor(client: HttpClient = httpClient) {
        this.client = client;
    }

    public async getAll(): Promise<{Entity}ResponseDto[]> {
        return this.client.get<{Entity}ResponseDto[]>(this.endpoint);
    }

    public async getById(id: string): Promise<{Entity}ResponseDto> {
        return this.client.get<{Entity}ResponseDto>(`${this.endpoint}/${id}`);
    }

    public async create(dto: {Entity}CreateRequestDto): Promise<{Entity}ResponseDto> {
        return this.client.post<{Entity}ResponseDto, {Entity}CreateRequestDto>(this.endpoint, dto);
    }

    public async update(id: string, dto: {Entity}UpdateRequestDto): Promise<{Entity}ResponseDto> {
        return this.client.put<{Entity}ResponseDto, {Entity}UpdateRequestDto>(`${this.endpoint}/${id}`, dto);
    }

    public async delete(id: string): Promise<void> {
        return this.client.delete<void>(`${this.endpoint}/${id}`);
    }
}

// Singleton instance — typed as the interface, not the class
export const {entity}Service: {Entity}Service = new {Entity}ServiceImpl();
```

**Key design decisions:**
- Constructor accepts `HttpClient` with default → enables unit testing with mock clients.
- Singleton variable is typed as `{Entity}Service` (interface), not `{Entity}ServiceImpl` (class) → consumers depend on abstraction.
- Endpoint path is centralized in one `private readonly` field.

#### ❌ Anti-Patterns
```typescript
// ❌ No interface — just an implementation
export class OrderService { ... }           // Must have OrderService.ts interface + OrderServiceImpl.ts

// ❌ Hardcoding URLs in each method
async getAll() {
    return this.client.get("/api/v1/orders");        // Use this.endpoint instead
    return this.client.get(`/api/v1/orders/${id}`);
}

// ❌ Singleton typed as the concrete class
export const orderService = new OrderServiceImpl();  // Should be: `: OrderService =`
```

---

### 3.5 `validator/` — Client-Side Validation

#### Purpose
Perform synchronous, client-side validation of DTOs **before** dispatching network requests. Validators are **pure TypeScript** — no React hooks, no JSX, no DOM access.

#### File Rules

| Rule | Standard |
|------|----------|
| **Shared result interface** | `ValidationResult.ts` — one per project, reused by all validators |
| **One validator class per entity** | `{Entity}Validator.ts` containing one class with validation methods per operation |
| **Pure functions / methods** | Synchronous, deterministic, side-effect-free |
| **Error keys match DTO field names** | The `errors` record keys must exactly match the field names in the corresponding DTO |
| **Imports from** | `dto/`, `types/` |
| **Imported by** | `controller_hooks/`, `ui/` (modals/forms) |

#### File 1: `ValidationResult.ts` — Shared Result Contract

```typescript
// src/core/validator/ValidationResult.ts

export interface ValidationResult {
    isValid: boolean;
    errors: Record<string, string>;   // key = field name, value = error message
}
```

One file. One interface. Used by every validator in the project.

#### File 2: `{Entity}Validator.ts` — Domain Validator Class

```typescript
// src/core/validator/{Entity}Validator.ts

import { {Entity}CreateRequestDto } from "@/core/dto/{Entity}CreateRequestDto";
import { {Entity}UpdateRequestDto } from "@/core/dto/{Entity}UpdateRequestDto";
import { ValidationResult } from "@/core/validator/ValidationResult";

export class {Entity}Validator {

    public validateCreate(dto: {Entity}CreateRequestDto): ValidationResult {
        const errors: Record<string, string> = {};

        if (!dto.name || dto.name.trim().length === 0) {
            errors.name = "Name is required";
        }

        if (!dto.email || dto.email.trim().length === 0) {
            errors.email = "Email is required";
        }

        // ... additional field validations ...

        return {
            isValid: Object.keys(errors).length === 0,
            errors,
        };
    }

    public validateUpdate(dto: {Entity}UpdateRequestDto): ValidationResult {
        const errors: Record<string, string> = {};

        if (!dto.name || dto.name.trim().length === 0) {
            errors.name = "Name is required";
        }

        // ... additional field validations ...

        return {
            isValid: Object.keys(errors).length === 0,
            errors,
        };
    }
}
```

**Key design decisions:**
- Methods named `validateCreate`, `validateUpdate` — matching the CRUD operation they guard.
- Returns a `ValidationResult` object — never throws, never returns boolean alone.
- Error keys like `"name"`, `"email"` exactly match DTO field names → enables direct mapping to form field error displays.

#### ❌ Anti-Patterns
```typescript
// ❌ Using React hooks in a validator
export function useValidateOrder() { ... }    // Validators are classes, not hooks

// ❌ Returning just true/false
public validate(dto): boolean { ... }          // Must return ValidationResult with errors map

// ❌ Error keys not matching DTO fields
errors["userName"] = "Required";               // DTO field is "name", not "userName"
```

---

### 3.6 `controller_hooks/` — React State Bridges

#### Purpose
Bridge the pure TypeScript core to React's rendering lifecycle. These are the **only files in `src/core/` that use React**. They are split into two categories:

```
controller_hooks/
├── operations/      ← Atomic, single-action hooks
└── facades/         ← Page-level orchestrators
```

---

#### A. `operations/` — Atomic Operation Hooks

Each file handles the lifecycle (loading state, error state, execution, success callback) for **one** atomic action: fetch all, create, update, or delete.

##### File Rules

| Rule | Standard |
|------|----------|
| **One hook per file** | Each file exports a single `function use{Action}{Entity}()` |
| **Naming** | `use{Action}{Entity}.ts` — e.g., `useCreateOrder.ts`, `useDeleteProduct.ts` |
| **`"use client"` directive** | First line of every file (required for Next.js App Router) |
| **Standard state shape** | Every hook manages: `is{Action}` (boolean), `{action}Error` (string \| null) |
| **`onSuccess` callback** | Mutation hooks accept `onSuccess?: () => void` to notify the caller (e.g., trigger table refresh) |
| **Return boolean from mutations** | `create` / `update` / `delete` actions return `Promise<boolean>` — `true` on success, `false` on failure |
| **Imports from** | `dto/`, `service/` (the singleton instance from `ServiceImpl`) |
| **Imported by** | `facades/` only |

##### Template: Query Hook (Fetch All)

```typescript
// src/core/controller_hooks/operations/use{Entities}.ts

"use client";

import { useState, useEffect, useCallback } from "react";
import { {Entity}ResponseDto } from "@/core/dto/{Entity}ResponseDto";
import { {entity}Service } from "@/core/service/{Entity}ServiceImpl";

export function use{Entities}() {
    const [{entities}, set{Entities}] = useState<{Entity}ResponseDto[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const fetch{Entities} = useCallback(async () => {
        setIsLoading(true);
        setError(null);
        try {
            const data = await {entity}Service.getAll();
            set{Entities}(data);
        } catch (err) {
            setError(err instanceof Error ? err.message : "Failed to load {entities}");
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetch{Entities}();
    }, [fetch{Entities}]);

    return { {entities}, isLoading, error, refresh{Entities}: fetch{Entities} };
}
```

**Key pattern:**
- `useCallback` wraps the fetch function → stable reference for `useEffect` and for passing to child hooks.
- `useEffect` triggers auto-fetch on mount.
- Returns a `refresh` function → allows other hooks/facades to trigger a re-fetch after mutations.

##### Template: Mutation Hook (Create)

```typescript
// src/core/controller_hooks/operations/useCreate{Entity}.ts

"use client";

import { useState } from "react";
import { {Entity}CreateRequestDto } from "@/core/dto/{Entity}CreateRequestDto";
import { {entity}Service } from "@/core/service/{Entity}ServiceImpl";

export function useCreate{Entity}(onSuccess?: () => void) {
    const [isCreating, setIsCreating] = useState(false);
    const [createError, setCreateError] = useState<string | null>(null);

    const create{Entity} = async (dto: {Entity}CreateRequestDto): Promise<boolean> => {
        setIsCreating(true);
        setCreateError(null);
        try {
            await {entity}Service.create(dto);
            if (onSuccess) onSuccess();    // Callback: refresh table / close modal
            return true;
        } catch (err) {
            setCreateError(err instanceof Error ? err.message : "Failed to create {entity}");
            return false;
        } finally {
            setIsCreating(false);
        }
    };

    return { create{Entity}, isCreating, createError };
}
```

##### Template: Mutation Hook (Update)

```typescript
// src/core/controller_hooks/operations/useUpdate{Entity}.ts

"use client";

import { useState } from "react";
import { {Entity}UpdateRequestDto } from "@/core/dto/{Entity}UpdateRequestDto";
import { {entity}Service } from "@/core/service/{Entity}ServiceImpl";

export function useUpdate{Entity}(onSuccess?: () => void) {
    const [isUpdating, setIsUpdating] = useState(false);
    const [updateError, setUpdateError] = useState<string | null>(null);

    const update{Entity} = async (id: string, dto: {Entity}UpdateRequestDto): Promise<boolean> => {
        setIsUpdating(true);
        setUpdateError(null);
        try {
            await {entity}Service.update(id, dto);
            if (onSuccess) onSuccess();
            return true;
        } catch (err) {
            setUpdateError(err instanceof Error ? err.message : "Failed to update {entity}");
            return false;
        } finally {
            setIsUpdating(false);
        }
    };

    return { update{Entity}, isUpdating, updateError };
}
```

##### Template: Mutation Hook (Delete)

```typescript
// src/core/controller_hooks/operations/useDelete{Entity}.ts

"use client";

import { useState } from "react";
import { {entity}Service } from "@/core/service/{Entity}ServiceImpl";

export function useDelete{Entity}(onSuccess?: () => void) {
    const [isDeleting, setIsDeleting] = useState(false);
    const [deleteError, setDeleteError] = useState<string | null>(null);

    const delete{Entity} = async (id: string): Promise<boolean> => {
        setIsDeleting(true);
        setDeleteError(null);
        try {
            await {entity}Service.delete(id);
            if (onSuccess) onSuccess();
            return true;
        } catch (err) {
            setDeleteError(err instanceof Error ? err.message : "Failed to delete {entity}");
            return false;
        } finally {
            setIsDeleting(false);
        }
    };

    return { delete{Entity}, isDeleting, deleteError };
}
```

##### Naming Convention Summary for Operations

| Action | Filename | Hook Name | State Variables Returned |
|--------|----------|-----------|--------------------------|
| Fetch all | `use{Entities}.ts` | `use{Entities}()` | `{entities}`, `isLoading`, `error`, `refresh{Entities}` |
| Create | `useCreate{Entity}.ts` | `useCreate{Entity}(onSuccess?)` | `create{Entity}`, `isCreating`, `createError` |
| Update | `useUpdate{Entity}.ts` | `useUpdate{Entity}(onSuccess?)` | `update{Entity}`, `isUpdating`, `updateError` |
| Delete | `useDelete{Entity}.ts` | `useDelete{Entity}(onSuccess?)` | `delete{Entity}`, `isDeleting`, `deleteError` |

---

#### B. `facades/` — Page-Level Orchestrator Hooks

Facades compose multiple operation hooks and local UI state (modal visibility, selected items) into a single, unified page state object.

##### File Rules

| Rule | Standard |
|------|----------|
| **One facade per feature/page** | `use{Entity}Management.ts` |
| **Orchestrates operations** | Calls operation hooks, wires `onSuccess` callbacks (e.g., `refreshUsers`) between them |
| **Manages UI coordination state** | Modal open/close booleans, "currently selected item" state |
| **Returns structured sub-state objects** | Groups related state into named objects: `table`, `createModal`, `updateModal`, `deleteModal` |
| **Imports from** | `controller_hooks/operations/`, `dto/` |
| **Imported by** | `src/app/` (route pages) only |

##### Template

```typescript
// src/core/controller_hooks/facades/use{Entity}Management.ts

"use client";

import { useState } from "react";
import { {Entity}ResponseDto } from "@/core/dto/{Entity}ResponseDto";
import { use{Entities} } from "@/core/controller_hooks/operations/use{Entities}";
import { useCreate{Entity} } from "@/core/controller_hooks/operations/useCreate{Entity}";
import { useUpdate{Entity} } from "@/core/controller_hooks/operations/useUpdate{Entity}";
import { useDelete{Entity} } from "@/core/controller_hooks/operations/useDelete{Entity}";

export function use{Entity}Management() {

    // ──── Operation hooks ────
    const {entity}Controller = use{Entities}();
    const refresh = {entity}Controller.refresh{Entities};

    const createController = useCreate{Entity}(refresh);
    const updateController = useUpdate{Entity}(refresh);
    const deleteController = useDelete{Entity}(refresh);

    // ──── Local UI coordination state ────
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [{entity}ToEdit, set{Entity}ToEdit] = useState<{Entity}ResponseDto | null>(null);
    const [{entity}ToDelete, set{Entity}ToDelete] = useState<{Entity}ResponseDto | null>(null);

    // ──── Structured page state ────
    return {
        table: {
            {entities}: {entity}Controller.{entities},
            isLoading: {entity}Controller.isLoading,
            error: {entity}Controller.error,
            refresh,
        },
        createModal: {
            isOpen: isCreateModalOpen,
            open: () => setIsCreateModalOpen(true),
            close: () => setIsCreateModalOpen(false),
            submit: createController.create{Entity},
            isSubmitting: createController.isCreating,
            serverError: createController.createError,
        },
        updateModal: {
            isOpen: {entity}ToEdit !== null,
            {entity}: {entity}ToEdit,
            open: (item: {Entity}ResponseDto) => set{Entity}ToEdit(item),
            close: () => set{Entity}ToEdit(null),
            submit: updateController.update{Entity},
            isSubmitting: updateController.isUpdating,
            serverError: updateController.updateError,
        },
        deleteModal: {
            isOpen: {entity}ToDelete !== null,
            {entity}: {entity}ToDelete,
            open: (item: {Entity}ResponseDto) => set{Entity}ToDelete(item),
            close: () => set{Entity}ToDelete(null),
            confirmDelete: deleteController.delete{Entity},
            isDeleting: deleteController.isDeleting,
            serverError: deleteController.deleteError,
        },
    };
}
```

**Key design decisions:**
- All mutation hooks receive `refresh` as `onSuccess` → table auto-refreshes after every successful CUD operation.
- Modal open/close logic is co-located with the data it manages → eliminates prop drilling.
- The view layer receives this entire structured object as a single prop.

---

## 4. Integration: How Core Connects to UI & Route Pages

### View Component Pattern (`src/ui/presentation/`)

View components are **pure presentation**. They receive the facade's return type as a typed prop and render JSX. They never import services, clients, or DTOs directly (except for type references).

```tsx
// src/ui/presentation/{Entity}DashboardView.tsx

import React from "react";
import { use{Entity}Management } from "@/core/controller_hooks/facades/use{Entity}Management";

export interface {Entity}DashboardViewProps {
    state: ReturnType<typeof use{Entity}Management>;
}

export function {Entity}DashboardView({ state }: {Entity}DashboardViewProps) {
    const { table, createModal, updateModal, deleteModal } = state;

    return (
        <main className="app-container">
            {/* Render table, modals, error banners using destructured state */}
        </main>
    );
}
```

### Route Page Pattern (`src/app/`)

The page file has exactly **two responsibilities**: (1) call the facade hook, (2) pass the state to the view.

```tsx
// src/app/{entities}/page.tsx

"use client";

import React from "react";
import { use{Entity}Management } from "@/core/controller_hooks/facades/use{Entity}Management";
import { {Entity}DashboardView } from "@/ui/presentation/{Entity}DashboardView";

export default function {Entity}Page() {
    const pageState = use{Entity}Management();
    return <{Entity}DashboardView state={pageState} />;
}
```

---

## 5. Scaffold Checklist: Adding a New Entity

When adding a new domain entity (e.g., `Product`, `Order`, `Invoice`), create files in this exact order:

| Step | Directory | File(s) to Create | Artifact Type |
|------|-----------|--------------------|---------------|
| **1** | `src/core/types/` | `{EntityStatus}.ts`, `{EntityCategory}.ts`, etc. | `enum` with string values |
| **2** | `src/core/dto/` | `{Entity}CreateRequestDto.ts`, `{Entity}UpdateRequestDto.ts`, `{Entity}ResponseDto.ts` | `interface` mirroring backend JSON |
| **3** | `src/core/validator/` | `{Entity}Validator.ts` | `class` returning `ValidationResult` |
| **4** | `src/core/service/` | `{Entity}Service.ts` | `interface` with async CRUD methods |
| **5** | `src/core/service/` | `{Entity}ServiceImpl.ts` | `class implements {Entity}Service` + singleton export |
| **6** | `src/core/controller_hooks/operations/` | `use{Entities}.ts`, `useCreate{Entity}.ts`, `useUpdate{Entity}.ts`, `useDelete{Entity}.ts` | React hooks with loading/error state |
| **7** | `src/core/controller_hooks/facades/` | `use{Entity}Management.ts` | Orchestrator hook returning structured page state |
| **8** | `src/ui/presentation/` | `{Entity}DashboardView.tsx`, views, modals | Pure presentation React components |
| **9** | `src/app/{entities}/` | `page.tsx` | Wires facade hook to view |

---

## 6. Code Review Checklist

Use this checklist for every PR touching `src/core/`:

### Types
- [ ] Each enum is in its own file, named after the enum
- [ ] All enum values are explicit string literals
- [ ] No magic strings exist elsewhere for values defined in enums

### DTOs
- [ ] Declared as `interface`, not `class`
- [ ] One interface per file
- [ ] File name follows `{Entity}{Action}RequestDto.ts` / `{Entity}ResponseDto.ts` pattern
- [ ] Categorical fields use imported enums, not raw strings
- [ ] Dates are `string` type (ISO 8601)

### Client
- [ ] `fetch()` is called **only** inside `HttpClient.ts` — nowhere else in the project
- [ ] Both class and singleton are exported from `HttpClient.ts`
- [ ] `ApiClientError` extends `Error` and captures `ErrorResponseDto` fields

### Services
- [ ] Interface file (`{Entity}Service.ts`) and implementation file (`{Entity}ServiceImpl.ts`) both exist
- [ ] Implementation `implements` the interface
- [ ] Constructor accepts `HttpClient` with default singleton fallback
- [ ] Singleton export is typed as the **interface**, not the implementation class
- [ ] API endpoint path is a single `private readonly` field

### Validators
- [ ] Pure TypeScript — no React imports, no hooks, no JSX
- [ ] Returns `ValidationResult`, not boolean
- [ ] Error record keys match DTO field names exactly

### Controller Hooks — Operations
- [ ] One hook per file, one action per hook
- [ ] `"use client"` directive is present
- [ ] Mutation hooks accept `onSuccess?: () => void`
- [ ] Mutation hooks return `Promise<boolean>`
- [ ] Standard state naming: `is{Action}`, `{action}Error`

### Controller Hooks — Facades
- [ ] Composes operation hooks, wires `onSuccess` → `refresh`
- [ ] Returns structured sub-state objects (`table`, `createModal`, etc.)
- [ ] Manages modal/selection UI state locally

### Integration
- [ ] View components receive `ReturnType<typeof use{Entity}Management>` — never call services directly
- [ ] Route page files do only two things: call facade hook, render view

---

## 7. Project-Specific Addenda (cloud-native-apps / `web/`)

These rules **add to** (do not replace) sections 1–6 above.

### 7.1 Zero JavaScript / Strict TypeScript

- All files under `src/` must be `.ts` or `.tsx` (no `.js` / `.jsx`).
- Compiler flags: `strict: true`, `noImplicitAny: true`, `strictNullChecks: true`.
- The `any` type is completely banned. Use generics, discriminated unions, or `unknown` with narrowing type guards.

### 7.2 1:1 Type Parity with Java Backend Records

- Every Java Request/Response `record` in backend microservices MUST have an exact TypeScript `interface` counterpart in `src/core/dto/`.
- Enums in backend domain MUST be mirrored as TypeScript enums (explicit string values) in `src/core/types/`.
- Field names and enum string values must match the JSON API contract exactly.

### 7.3 BFF Cookie Auth & OAuth2 (no tokens in the browser)

- The SPA is a BFF client of `gateway-service`. Use `credentials: "include"` on all `HttpClient` requests so the gateway `APP_SESSION` cookie is sent.
- Never store access tokens or refresh tokens in `localStorage`, `sessionStorage`, or SPA memory as the browser auth mechanism.
- **Password login HTML is hosted by `oauth2-authorization-server`**, not by the SPA.
- SPA Sign in = full-page navigation to  
  `{GATEWAY_BASE}/oauth2/authorization/gateway-client`.
- SPA may provide: Register, Home/profile (`/api/v1/auth/me`), Logout (`/api/v1/auth/logout`), and a Sign-in button/redirect.
- Optional `HttpClient` behavior: on `401`, redirect to the OAuth start URL (except for intentional unauthenticated pages such as Home before Sign in).

Extend the generic `HttpClient` template (section 3.3) for this project as follows:

```typescript
const response = await fetch(url, {
    method,
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    body: body ? JSON.stringify(body) : undefined,
});
```

### 7.4 Simple HTML & CSS UI

Learning/demo UI. Prefer clarity over polish. These constraints apply on top of `src/ui/*` presentation rules.

**Allowed**
- Semantic HTML via JSX (`form`, `input`, `button`, `label`, `nav`, `main`, `h1`, `p`, `ul`).
- Plain CSS: one global stylesheet and/or simple CSS Modules (`.module.css`).
- Minimal layout: margin/padding, simple flex column, basic borders.
- Native browser controls.

**Forbidden**
- UI libraries (Material UI, Chakra, Ant Design, shadcn, Tailwind UI kits, etc.).
- Tailwind / CSS-in-JS / styled-components / theme systems / design tokens.
- Animations, gradients, glassmorphism, dark-mode themes, icon packs.
- Complex cards/modals/drawers/dashboards; hero marketing layouts.
- Over-styled product landing pages.

### 7.5 Legacy flat `web/` layout → target layout

If existing code still uses a flat tree, migrate toward the structure in section 1:

| Legacy path | Target path |
|-------------|-------------|
| `src/controller/` | `src/core/controller_hooks/facades/` (+ `operations/` when split) |
| `src/service/` | `src/core/service/` |
| `src/client/` | `src/core/client/` |
| `src/dto/` | `src/core/dto/` |
| `src/types/` | `src/core/types/` |
| `src/validator/` | `src/core/validator/` |
| `src/presentation/` | `src/ui/presentation/` |
| `src/components/` | `src/ui/components/` |
| `src/app/` | `src/app/` (unchanged) |
| `src/context/` | Prefer DI via facade constructor args / React context wrapping service interfaces under `src/core` or `src/ui` providers — do not put business logic in context |

### 7.6 Extra review checks (this repo)

- [ ] Strict TS; no `any`
- [ ] DTOs/enums match Java backend 1:1
- [ ] `credentials: "include"` on API calls; no tokens in browser storage
- [ ] Sign in is OAuth redirect; AS hosts password form
- [ ] Simple HTML/CSS only — no UI kits / Tailwind / animations
