import type { AuthProviderType } from "./enums";

export type SystemUser = {
    name: string;
    email: string;
    provider: AuthProviderType;
}