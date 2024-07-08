import type { LoginTypeEnum } from "./enums";

export type SystemUser = {
    name: string;
    email: string;
    provider: LoginTypeEnum;
}