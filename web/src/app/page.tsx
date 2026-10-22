"use client";

import { useHomeManagement } from "@/core/controller_hooks/facades/useHomeManagement";
import { HomeView } from "@/ui/presentation/HomeView";

export default function HomePage() {
    const pageState = useHomeManagement();
    return <HomeView state={pageState} />;
}
