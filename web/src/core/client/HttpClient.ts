import { ApiClientError } from "@/core/client/ApiClientError";
import { ErrorResponseDto } from "@/core/dto/ErrorResponseDto";

const DEFAULT_BASE_URL: string = "http://localhost:8080";

export class HttpClient {
    private readonly baseUrl: string;

    constructor(baseUrl: string = HttpClient.resolveBaseUrl()) {
        this.baseUrl = baseUrl.replace(/\/$/, "");
    }

    private static resolveBaseUrl(): string {
        const configured = process.env.NEXT_PUBLIC_GATEWAY_BASE_URL;
        if (configured && configured.trim().length > 0) {
            return configured;
        }
        return DEFAULT_BASE_URL;
    }

    public async get<T>(path: string, redirectOnUnauthorized: boolean = true): Promise<T> {
        return this.request<T>(path, "GET", undefined, redirectOnUnauthorized);
    }

    public async post<T, B>(
        path: string,
        body: B,
        redirectOnUnauthorized: boolean = true,
    ): Promise<T> {
        return this.request<T>(path, "POST", body, redirectOnUnauthorized);
    }

    public async put<T, B>(
        path: string,
        body: B,
        redirectOnUnauthorized: boolean = true,
    ): Promise<T> {
        return this.request<T>(path, "PUT", body, redirectOnUnauthorized);
    }

    public async delete<T>(path: string, redirectOnUnauthorized: boolean = true): Promise<T> {
        return this.request<T>(path, "DELETE", undefined, redirectOnUnauthorized);
    }

    private async request<T>(
        path: string,
        method: string,
        body?: unknown,
        redirectOnUnauthorized: boolean = true,
    ): Promise<T> {
        const url = `${this.baseUrl}${path}`;

        let response: Response;
        try {
            response = await fetch(url, {
                method,
                credentials: "include",
                headers: { "Content-Type": "application/json" },
                body: body !== undefined ? JSON.stringify(body) : undefined,
            });
        } catch {
            throw ApiClientError.fromNetwork("Network request failed");
        }

        if (response.status === 401 && redirectOnUnauthorized) {
            if (typeof window !== "undefined") {
                window.location.assign(
                    `${this.baseUrl}/oauth2/authorization/gateway-client`,
                );
            }
            throw ApiClientError.fromHttp(401, "Unauthorized", path);
        }

        if (!response.ok) {
            throw await this.toApiClientError(response, path);
        }

        if (response.status === 204) {
            return undefined as unknown as T;
        }

        const contentType = response.headers.get("content-type");
        if (contentType?.includes("application/json")) {
            return (await response.json()) as T;
        }

        return undefined as unknown as T;
    }

    private async toApiClientError(response: Response, path: string): Promise<ApiClientError> {
        try {
            const errorDto = (await response.json()) as ErrorResponseDto;
            if (errorDto && typeof errorDto.message === "string") {
                return new ApiClientError({
                    timestamp: errorDto.timestamp ?? new Date().toISOString(),
                    status: errorDto.status ?? response.status,
                    error: errorDto.error ?? "HTTP Error",
                    message: errorDto.message,
                    path: errorDto.path ?? path,
                    fieldErrors: errorDto.fieldErrors,
                });
            }
        } catch {
            // fall through to text/body fallback
        }

        const text = await response.text().catch(() => response.statusText);
        return ApiClientError.fromHttp(response.status, text || response.statusText, path);
    }
}

export const httpClient = new HttpClient();
