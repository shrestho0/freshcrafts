import { AUTH_COOKIE_EXPIRES_IN, AUTH_COOKIE_NAME, GITHUB_CLIENT_SECRET, GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, GOOGLE_OAUTH_CALLBACK_URL } from "$env/static/private";
import type { OAuthCallbackInternalResponse, OAuthGithubDataRequestDto, OAuthGoogleIdTokenData } from "@/types/internal";
import type { PageServerLoad } from "./$types";
import { google } from "googleapis";

import { jwtDecode } from "jwt-decode";
import { BackendEndpoints } from "@/backend-endpoints";
import { AuthProviderType } from "@/types/enums";
import { redirect } from "@sveltejs/kit";


export const load: PageServerLoad = async ({ locals, url, cookies, params, }) => {


    const oauth2Client = new google.auth.OAuth2(
        GOOGLE_CLIENT_ID,
        GOOGLE_CLIENT_SECRET,
        url.origin + GOOGLE_OAUTH_CALLBACK_URL
    );


    const dataToSendToEngine: OAuthGithubDataRequestDto = { token: "", userInfo: undefined, dataJson: undefined, emails: [] };
    const responseObject: OAuthCallbackInternalResponse = { success: false, provider: "google", closeWindow: false, message: "", data: dataToSendToEngine }

    // popup for this page only rest will be redirected
    let fromPage: 'setup' | 'link' | undefined = cookies.get('fromPage') as any


    // Data from github callback
    const oData = {
        code: url.searchParams.get("code"),
        state: url.searchParams.get("state")
    }

    console.log(" GOOGLE OAUTH CALLBACK DATA", oData)

    if (!oData.code) {
        responseObject.message = "Code not found"
        return responseObject
    }

    console.log(oData)



    // decoding userInfo
    // try {
    let { tokens } = await oauth2Client.getToken(oData.code);

    console.log("GOOGLE TOKENS", tokens)

    let decodedUserInfo: OAuthGoogleIdTokenData | null = null;
    if (tokens?.id_token) decodedUserInfo = jwtDecode(tokens?.id_token)

    if (tokens) {
        dataToSendToEngine.token = tokens?.access_token ?? '';
        dataToSendToEngine.userInfo = decodedUserInfo;
        dataToSendToEngine.dataJson = tokens

        dataToSendToEngine.emails = [decodedUserInfo?.email ?? '']

        // Thakbe
        dataToSendToEngine.oAuthGoogleEmail = decodedUserInfo?.email ?? ''


        if (fromPage != 'setup' && fromPage != 'link') {
            // try generating token from engine
            // we'll check with provider and consiting email
            const res = await fetch(BackendEndpoints.GENERATE_TOKEN, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    provider: AuthProviderType.OAUTH_GOOGLE,
                    // check all email with all emails, bad approach, nedda fix on refactor, proceeding for now
                    // email: dataToSendToEngine.emails,
                    // oAuthEmails: dataToSendToEngine.emails
                    // oAuthGithubId: dataToSendToEngine.oAuthGithubId,
                    googleEmail: dataToSendToEngine.oAuthGoogleEmail,
                })
            }).then(res => res.json()).catch(() => {
                return {
                    success: false,
                    message: 'Failed to communicate with `engine`'
                };
            });
            console.log("ENGINE TOKENS", res)
            if (res.success == true) {
                // set tokens in cookie
                // cookies.set('_freshCraftsTokens', JSON.stringify(res.data));
                cookies.set(AUTH_COOKIE_NAME, JSON.stringify(res.tokens), {
                    path: '/',
                    maxAge: parseInt(AUTH_COOKIE_EXPIRES_IN)
                });

                // redirecting to dashboard, seems all fine
                return redirect(307, '/dashboard')
            }


        }


        responseObject.success = true
        responseObject.closeWindow = true
        responseObject.provider = 'google'
        responseObject.data = dataToSendToEngine
        return responseObject

    }
    // } catch (err) {
    //     console.error(err)
    // }
    // oauth2Client.setCredentials(tokens);


    responseObject.message = "Invalid token"
    return responseObject
};