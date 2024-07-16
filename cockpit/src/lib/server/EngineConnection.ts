import { BackendEndpoints } from "@/backend-endpoints";
import type { EngineSystemConfigResponseDto } from "@/types/dtos";
import { AuthProviderType } from "@/types/enums";
import messages from "@/utils/messages";

export class EngineConnection {
    private static _instance: EngineConnection;
    static getInstance(): EngineConnection {
        if (!this._instance) {
            this._instance = new EngineConnection();
        }
        return this._instance
    }

    private constructor() { }

    public async getProviders(): Promise<{ success: boolean, providers: AuthProviderType[], message: string }> {
        const res = await fetch(BackendEndpoints.PROVIDERS, {
            headers: {
                'Content-Type': 'application/json',
            }
        }).then(res => res.json()).catch(() => {
            return {
                success: false,
                message: messages.RESPONSE_ERROR,
                providers: [],
            };
        });

        if (res.data) {
            res.providers = res.data as {
                success: boolean,
                providers: AuthProviderType[],
                message: string
            }
            delete res.data;
        }
        return res
    }

    public async getSystemConfig(): Promise<EngineSystemConfigResponseDto> {
        return await fetch(BackendEndpoints.SETUP_SYSCONFIG, {
            method: "GET",
            headers: {
                'Content-Type': 'application/json',
            },
        }).then(res => res.json()).catch(err => {
            return null
        })
    }

    public async updateSystemConfigPartial(new_sysconf: Partial<EngineSystemConfigResponseDto>): Promise<{ success: boolean, message: string, data: EngineSystemConfigResponseDto }> {
        return await fetch(BackendEndpoints.SETUP_SYSCONFIG, {
            method: "PATCH",
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(new_sysconf)
        }).then(res => res.json()).catch((err) => { return { success: false, message: messages.RESPONSE_ERROR } }) as unknown as { success: boolean, message: string, data: EngineSystemConfigResponseDto }
    }

    public async generateToken(provider: AuthProviderType, data: {
        email?: string,
        password?: string,
        googleEmail?: string,
        githubId?: string,
    }) {

        switch (provider) {
            case AuthProviderType.EMAIL_PASSWORD:
                return await fetch(BackendEndpoints.GENERATE_TOKEN, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        provider,
                        email: data.email,
                        password: data.password

                    })
                }).then(res => res.json()).catch(() => {
                    return {
                        success: false,
                        message: messages.COMMUNICATION_FAILURE
                    };
                });
                break;
            default:
                console.log("Requested Generate Token", provider, data)
                break
        }

    }

    public async refreshToken(refreshToken: string, provider: AuthProviderType) {
        return await fetch(BackendEndpoints.REFRESH_TOKEN, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                refreshToken,
                provider
            })
        }).then(res => res.json()).catch(() => { return { success: false } })
    }

    public async changePassword({ oldPassword,
        newPassword }: {
            oldPassword: string,
            newPassword: string
        }) {
        return await fetch(BackendEndpoints.CHANGE_PASSWORD, {
            method: "POST",
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                oldPassword,
                newPassword
            })
        }).then(res => res.json()).catch(e => {
            return {
                success: false,
                message: messages.COMMUNICATION_FAILURE
            }
        })
    }

}