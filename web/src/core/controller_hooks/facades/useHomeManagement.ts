"use client";

import { useCurrentUser } from "@/core/controller_hooks/operations/useCurrentUser";
import { useLogout } from "@/core/controller_hooks/operations/useLogout";
import { authService } from "@/core/service/AuthServiceImpl";

export function useHomeManagement() {
    const currentUserController = useCurrentUser();
    const logoutController = useLogout(() => {
        currentUserController.clearProfile();
    });

    return {
        profile: {
            user: currentUserController.profile,
            isLoading: currentUserController.isLoading,
            error: currentUserController.error,
            refresh: currentUserController.refreshCurrentUser,
        },
        session: {
            signIn: () => {
                window.location.assign(authService.getSignInUrl());
            },
            logout: logoutController.logout,
            isLoggingOut: logoutController.isLoggingOut,
            logoutError: logoutController.logoutError,
        },
    };
}
