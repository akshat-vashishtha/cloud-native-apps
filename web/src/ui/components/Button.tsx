import type { ButtonHTMLAttributes, ReactNode } from "react";

export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    children: ReactNode;
}

export function Button({ children, type = "button", ...rest }: ButtonProps) {
    return (
        <button type={type} {...rest}>
            {children}
        </button>
    );
}
