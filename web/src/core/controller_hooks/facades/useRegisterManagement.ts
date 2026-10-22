"use client";

import { useState } from "react";
import { useRegisterUser } from "@/core/controller_hooks/operations/useRegisterUser";
import { RegisterRequestDto } from "@/core/dto/RegisterRequestDto";
import { authService } from "@/core/service/AuthServiceImpl";
import { RegisterValidator } from "@/core/validator/RegisterValidator";

const registerValidator = new RegisterValidator();

export function useRegisterManagement() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [fullName, setFullName] = useState("");
    const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

    const registerController = useRegisterUser(() => {
        setEmail("");
        setPassword("");
        setFullName("");
        setFieldErrors({});
    });

    const submit = async (): Promise<boolean> => {
        const dto: RegisterRequestDto = { email, password, fullName };
        const validation = registerValidator.validateCreate(dto);
        setFieldErrors(validation.errors);
        if (!validation.isValid) {
            return false;
        }
        return registerController.registerUser(dto);
    };

    return {
        form: {
            email,
            password,
            fullName,
            fieldErrors,
            setEmail,
            setPassword,
            setFullName,
            submit,
            isSubmitting: registerController.isRegistering,
            serverError: registerController.registerError,
            success: registerController.registerResult,
        },
        session: {
            signIn: () => {
                window.location.assign(authService.getSignInUrl());
            },
        },
    };
}
