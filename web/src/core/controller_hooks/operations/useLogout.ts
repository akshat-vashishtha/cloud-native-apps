"use client";

import { useState } from "react";
import { authService } from "@/core/service/AuthServiceImpl";

export function useLogout(onSuccess?: () => void) {
    const [isLoggingOut, setIsLoggingOut] = useState(false);
    const [logoutError, setLogoutError] = useState<string | null>(null);

    const logout = async (): Promise<boolean> => {
        setIsLoggingOut(true);
        setLogoutError(null);
        try {
            await authService.logout();
            if (onSuccess) {
                onSuccess();
            }
            return true;
        } catch (err: unknown) {
            setLogoutError(err instanceof Error ? err.message : "Failed to logout");
            return false;
        } finally {
            setIsLoggingOut(false);
        }
    };

    return { logout, isLoggingOut, logoutError };
}
