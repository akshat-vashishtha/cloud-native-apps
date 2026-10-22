import { HttpClient, httpClient } from "@/core/client/HttpClient";
import { RegisterRequestDto } from "@/core/dto/RegisterRequestDto";
import { RegisterResponseDto } from "@/core/dto/RegisterResponseDto";
import { UserProfileDto } from "@/core/dto/UserProfileDto";
import { AuthService } from "@/core/service/AuthService";

export class AuthServiceImpl implements AuthService {
    private readonly endpoint = "/api/v1/auth";
    private readonly client: HttpClient;

    constructor(client: HttpClient = httpClient) {
        this.client = client;
    }

    public async register(dto: RegisterRequestDto): Promise<RegisterResponseDto> {
        return this.client.post<RegisterResponseDto, RegisterRequestDto>(
            `${this.endpoint}/register`,
            dto,
            false,
        );
    }

    public async getCurrentUser(): Promise<UserProfileDto> {
        return this.client.get<UserProfileDto>(`${this.endpoint}/me`, false);
    }

    public async logout(): Promise<void> {
        return this.client.post<void, Record<string, never>>(
            `${this.endpoint}/logout`,
            {},
            false,
        );
    }

    public getSignInUrl(): string {
        const baseUrl = process.env.NEXT_PUBLIC_GATEWAY_BASE_URL ?? "http://localhost:8080";
        return `${baseUrl.replace(/\/$/, "")}/oauth2/authorization/gateway-client`;
    }
}

export const authService: AuthService = new AuthServiceImpl();
