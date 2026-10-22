"use client";

import { useRegisterManagement } from "@/core/controller_hooks/facades/useRegisterManagement";
import { RegisterView } from "@/ui/presentation/RegisterView";

export default function RegisterPage() {
    const pageState = useRegisterManagement();
    return <RegisterView state={pageState} />;
}
