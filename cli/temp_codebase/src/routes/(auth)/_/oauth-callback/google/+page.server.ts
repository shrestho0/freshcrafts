// import {
// 	AUTH_COOKIE_EXPIRES_IN,
// 	AUTH_COOKIE_NAME,
// 	GOOGLE_CLIENT_ID,
// 	GOOGLE_CLIENT_SECRET,
// } from '$env/static/private';
// import type {
// 	OAuthCallbackInternalResponse,
// 	OAuthGithubDataRequestDto,
// 	OAuthGoogleIdTokenData
// } from '@/types/internal';
// import type { PageServerLoad } from './$types';
// import { google } from 'googleapis';

import { AUTH_COOKIE_EXPIRES_IN, AUTH_COOKIE_NAME, GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET } from "$env/static/private";
import { PUBLIC_GOOGLE_OAUTH_CALLBACK_URL } from "$env/static/public";
// import { google } from "googleapis";
import type { PageServerLoad } from "./$types";
import type { OAuthGoogleIdTokenData } from "@/types/internal";
import { jwtDecode } from "jwt-decode";
import type { EngineSystemConfigResponseDto } from "@/types/dtos";
import { EngineConnection } from "@/server/EngineConnection";
import { BackendEndpoints } from "@/backend-endpoints";
import { AuthProviderType } from "@/types/enums";




/// FIXME: Commented to remove googleapis package
export const load: PageServerLoad = async ({ locals, url, cookies, params, fetch }) => {

    // const oauth2Client = new google.auth.OAuth2(
    //     GOOGLE_CLIENT_ID,
    //     GOOGLE_CLIENT_SECRET,
    //     url.origin + PUBLIC_GOOGLE_OAUTH_CALLBACK_URL
    // );


    const from_page = cookies.get('fromPage') as 'setup' | 'link' | undefined;
    const code = url.searchParams.get('code');
    const isSetupAction = url.searchParams.get('setup_action') == 'install';

    const closeWindow = from_page == 'setup' || from_page == 'link'

    if (!code) {
        return {
            success: false,
            message: 'Code not found'
        }
    }

    const redirectUri = url.origin + PUBLIC_GOOGLE_OAUTH_CALLBACK_URL;

    const tokenEndpoint = 'https://oauth2.googleapis.com/token';
    // Build the POST request body
    const body = new URLSearchParams({
        client_id: GOOGLE_CLIENT_ID,
        client_secret: GOOGLE_CLIENT_SECRET,
        code: code,
        grant_type: 'authorization_code',
        redirect_uri: redirectUri
    });



    try {

        // let { tokens } = await oauth2Client.getToken(code);

        const response = await fetch(tokenEndpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: body.toString()
        });

        const tokens = await response.json();
        console.log('=================>>TOKENS<==================', tokens);

        let decodedUserInfo: OAuthGoogleIdTokenData | null = null;
        if (tokens?.id_token) decodedUserInfo = jwtDecode(tokens?.id_token);

        if (!decodedUserInfo) throw new Error('Failed to decode Google OAuth ID Token')
        console.log(tokens, decodedUserInfo)

        const sysconfToUpdate: Partial<EngineSystemConfigResponseDto> = {
            systemUserOauthGoogleEnabled: true,
            systemUserOAuthGoogleEmail: decodedUserInfo.email,
            systemUserOauthGoogleData: { ...tokens, decodedUserInfo_email: decodedUserInfo.email, decodedUserInfo_picture: decodedUserInfo.picture }
        }

        const actualSysConf = await EngineConnection.getInstance().getSystemConfig();

        if (actualSysConf?.systemUserOauthGoogleEnabled && actualSysConf?.systemUserOAuthGoogleEmail) {
            if (sysconfToUpdate.systemUserOAuthGoogleEmail != actualSysConf.systemUserOAuthGoogleEmail) {
                throw new Error('Google account mismatch')
            }
        }

        const up = await EngineConnection.getInstance().updateSystemConfigPartial(sysconfToUpdate)
        // if (up.success) {
        // 	return {
        // 		success: true,
        // 		message: 'Google oauth success',
        // 		closeWindow
        // 	}
        // }

        if (!up.success) {
            return up
        }


        if (from_page != 'setup' && from_page != 'link') {
            // generateToken
            console.log('\n\nGenerating token\n\n')
            const tokens = await EngineConnection.getInstance().generateEngineTokens(sysconfToUpdate.systemUserOAuthGoogleEmail, fetch);
            console.log('TOKEN GENERATION RESPONSE', tokens);
            if (!tokens.success) {
                return {
                    success: false,
                    message: 'Failed to generate token'
                }
            }

            // set tokens in cookie if not logged in 
            if (!locals.user) {
                cookies.set(AUTH_COOKIE_NAME, JSON.stringify(tokens?.tokens), {
                    path: '/',
                    secure: false,
                    maxAge: parseInt(AUTH_COOKIE_EXPIRES_IN)
                });
            }
        }

        return {
            success: true,
            message: 'Google oauth success',
            closeWindow
        }

    } catch (e: any) {
        return {
            success: false,
            message: e?.message ?? 'Failed to authorize Google OAuth'
        }
    }







}


