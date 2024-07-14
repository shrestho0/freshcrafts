


/**
 * Response Object DTOs
 */
export type EngineSystemConfigResponseDto = {
    systemUserSetupComplete: boolean;
    systemUserName: string;
    systemUserEmail: string;
    systemUserPasswordHash: string;
    systemUserOauthGoogleEnabled: boolean;
    systemUserOauthGithubEnabled: boolean;
    systemUserOauthGoogleData: Object | null;
    systemUserOauthGithubData: Object | null;
    systemUserOAuthGithubId?: string;
    systemUserOAuthGoogleEmail?: string;
    created: null;
    updated: Date;
}

export type GoogleOAuthLoginCallbackDto = {
    access_token: string;
    refresh_token: string;
    scope: string;
    token_type: string;
    id_token: string;
    expiry_date: number;
}

