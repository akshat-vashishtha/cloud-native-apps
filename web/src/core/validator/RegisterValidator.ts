import { RegisterRequestDto } from "@/core/dto/RegisterRequestDto";
import { ValidationResult } from "@/core/validator/ValidationResult";

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export class RegisterValidator {
    public validateCreate(dto: RegisterRequestDto): ValidationResult {
        const errors: Record<string, string> = {};

        const email = dto.email.trim();
        if (!email) {
            errors.email = "Email is required";
        } else if (!EMAIL_PATTERN.test(email)) {
            errors.email = "Invalid email format";
        }

        const fullName = dto.fullName.trim();
        if (!fullName) {
            errors.fullName = "Full name is required";
        } else if (fullName.length < 2 || fullName.length > 50) {
            errors.fullName = "Full name must be between 2 and 50 characters";
        }

        if (!dto.password) {
            errors.password = "Password is required";
        } else if (dto.password.length < 8) {
            errors.password = "Password must be at least 8 characters";
        }

        return {
            isValid: Object.keys(errors).length === 0,
            errors,
        };
    }
}
