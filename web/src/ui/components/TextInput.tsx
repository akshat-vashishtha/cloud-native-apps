import type { InputHTMLAttributes } from "react";

export interface TextInputProps extends InputHTMLAttributes<HTMLInputElement> {
    label: string;
    error?: string;
}

export function TextInput({ label, error, id, ...rest }: TextInputProps) {
    const inputId = id ?? rest.name;

    return (
        <label htmlFor={inputId}>
            {label}
            <input id={inputId} {...rest} />
            {error ? <span className="field-error">{error}</span> : null}
        </label>
    );
}
