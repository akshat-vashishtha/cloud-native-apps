import { RegisterRequestDto } from "@/core/dto/RegisterRequestDto";
import { RegisterResponseDto } from "@/core/dto/RegisterResponseDto";
import { UserProfileDto } from "@/core/dto/UserProfileDto";

export interface AuthService {
    register(dto: RegisterRequestDto): Promise<RegisterResponseDto>;
    getCurrentUser(): Promise<UserProfileDto>;
    logout(): Promise<void>;
    getSignInUrl(): string;
}
