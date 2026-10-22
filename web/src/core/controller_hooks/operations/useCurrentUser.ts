"use client";

import { useCallback, useEffect, useState } from "react";
import { ApiClientError } from "@/core/client/ApiClientError";
import { UserProfileDto } from "@/core/dto/UserProfileDto";
import { authService } from "@/core/service/AuthServiceImpl";

export function useCurrentUser() {
    const [profile, setProfile] = useState<UserProfileDto | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const fetchCurrentUser = useCallback(async () => {
        setIsLoading(true);
        setError(null);
        try {
            const data = await authService.getCurrentUser();
            setProfile(data);
        } catch (err: unknown) {
            setProfile(null);
            if (err instanceof ApiClientError && err.status === 401) {
                setError(null);
            } else {
                setError(err instanceof Error ? err.message : "Failed to load profile");
            }
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        void fetchCurrentUser();
    }, [fetchCurrentUser]);

    return {
        profile,
        isLoading,
        error,
        refreshCurrentUser: fetchCurrentUser,
        clearProfile: () => setProfile(null),
    };
}
