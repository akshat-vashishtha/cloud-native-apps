import Link from "next/link";
import { useHomeManagement } from "@/core/controller_hooks/facades/useHomeManagement";
import { Button } from "@/ui/components/Button";

export interface HomeViewProps {
    state: ReturnType<typeof useHomeManagement>;
}

export function HomeView({ state }: HomeViewProps) {
    const { profile, session } = state;

    return (
        <main>
            <nav>
                <Link href="/">Home</Link>
                <Link href="/register">Register</Link>
            </nav>

            <h1>Home</h1>

            {profile.isLoading ? <p>Loading…</p> : null}
            {profile.error ? <p className="error">{profile.error}</p> : null}
            {session.logoutError ? <p className="error">{session.logoutError}</p> : null}

            {!profile.isLoading && !profile.user ? (
                <section>
                    <p>You are not signed in.</p>
                    <Button onClick={session.signIn}>Sign in</Button>
                </section>
            ) : null}

            {profile.user ? (
                <section>
                    <p>Signed in as {profile.user.fullName}</p>
                    <p>Email: {profile.user.email}</p>
                    <p>Status: {profile.user.status}</p>
                    <p>Roles: {profile.user.roles.join(", ") || "none"}</p>
                    <p>Permissions: {profile.user.permissions.join(", ") || "none"}</p>
                    <Button
                        disabled={session.isLoggingOut}
                        onClick={() => {
                            void session.logout();
                        }}
                    >
                        {session.isLoggingOut ? "Logging out…" : "Logout"}
                    </Button>
                </section>
            ) : null}
        </main>
    );
}
