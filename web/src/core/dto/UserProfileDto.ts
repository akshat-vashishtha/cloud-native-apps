import { UserStatus } from "@/core/types/UserStatus";

export interface UserProfileDto {
    id: string;
    email: string;
    fullName: string;
    roles: string[];
    permissions: string[];
    status: UserStatus;
}
