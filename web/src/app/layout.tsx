import type { Metadata } from "next";
import type { ReactNode } from "react";
import "@/ui/styles/globals.css";

export const metadata: Metadata = {
    title: "Cloud Native Apps",
    description: "BFF client SPA",
};

export default function RootLayout({ children }: { children: ReactNode }) {
    return (
        <html lang="en">
            <body>{children}</body>
        </html>
    );
}
