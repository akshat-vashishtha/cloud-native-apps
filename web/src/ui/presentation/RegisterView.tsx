import Link from "next/link";
import { useRegisterManagement } from "@/core/controller_hooks/facades/useRegisterManagement";
import { Button } from "@/ui/components/Button";
import { TextInput } from "@/ui/components/TextInput";

export interface RegisterViewProps {
    state: ReturnType<typeof useRegisterManagement>;
}

export function RegisterView({ state }: RegisterViewProps) {
    const { form, session } = state;

    return (
        <main>
            <nav>
                <Link href="/">Home</Link>
                <Link href="/register">Register</Link>
            </nav>

            <h1>Register</h1>

            <form
                onSubmit={(event) => {
                    event.preventDefault();
                    void form.submit();
                }}
            >
                <TextInput
                    label="Full name"
                    name="fullName"
                    value={form.fullName}
                    onChange={(event) => form.setFullName(event.target.value)}
                    error={form.fieldErrors.fullName}
                    autoComplete="name"
                    required
                />
                <TextInput
                    label="Email"
                    name="email"
                    type="email"
                    value={form.email}
                    onChange={(event) => form.setEmail(event.target.value)}
                    error={form.fieldErrors.email}
                    autoComplete="email"
                    required
                />
                <TextInput
                    label="Password"
                    name="password"
                    type="password"
                    value={form.password}
                    onChange={(event) => form.setPassword(event.target.value)}
                    error={form.fieldErrors.password}
                    autoComplete="new-password"
                    required
                />

                {form.serverError ? <p className="error">{form.serverError}</p> : null}
                {form.success ? (
                    <p className="success">
                        Registered {form.success.email}. You can sign in now.
                    </p>
                ) : null}

                <Button type="submit" disabled={form.isSubmitting}>
                    {form.isSubmitting ? "Registering…" : "Register"}
                </Button>
            </form>

            <p>
                Already have an account?{" "}
                <Button onClick={session.signIn}>Sign in</Button>
            </p>
        </main>
    );
}
