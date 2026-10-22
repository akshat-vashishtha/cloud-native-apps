"use client";

import { useState } from "react";
import { RegisterRequestDto } from "@/core/dto/RegisterRequestDto";
import { RegisterResponseDto } from "@/core/dto/RegisterResponseDto";
import { authService } from "@/core/service/AuthServiceImpl";

export function useRegisterUser(onSuccess?: (response: RegisterResponseDto) => void) {
    const [isRegistering, setIsRegistering] = useState(false);
    const [registerError, setRegisterError] = useState<string | null>(null);
    const [registerResult, setRegisterResult] = useState<RegisterResponseDto | null>(null);

    const registerUser = async (dto: RegisterRequestDto): Promise<boolean> => {
        setIsRegistering(true);
        setRegisterError(null);
        try {
            const response = await authService.register(dto);
            setRegisterResult(response);
            if (onSuccess) {
                onSuccess(response);
            }
            return true;
        } catch (err: unknown) {
            setRegisterError(err instanceof Error ? err.message : "Failed to register user");
            return false;
        } finally {
            setIsRegistering(false);
        }
    };

    return { registerUser, isRegistering, registerError, registerResult };
}
