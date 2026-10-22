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

    public static fromNetwork(message: string): ApiClientError {
        return new ApiClientError({
            timestamp: new Date().toISOString(),
            status: 0,
            error: "Network Error",
            message,
            path: "",
        });
    }

    public static fromHttp(status: number, message: string, path: string = ""): ApiClientError {
        return new ApiClientError({
            timestamp: new Date().toISOString(),
            status,
            error: "HTTP Error",
            message,
            path,
        });
    }
}
