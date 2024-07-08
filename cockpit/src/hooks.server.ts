import { AUTH_COOKIE_EXPIRES_IN, AUTH_COOKIE_NAME, JWT_ACCESS_SECRET, JWT_ISSUER, JWT_REFRESH_SECRET } from '$env/static/private';
import { BackendEndpoints } from '@/backend-endpoints';
import type { SystemUser } from '@/types/entities';
import type { LoginTypeEnum } from '@/types/enums';
import type { Handle } from '@sveltejs/kit';

import jwt from "jsonwebtoken"

type User = {
    name: string;
    email: string;
}

export const handle: Handle = async ({ event, resolve }) => {

    console.log("Entered hook.server.ts")

    // Cokies and System wide data setup
    const authCookie = event.cookies.get(AUTH_COOKIE_NAME)

    let { unverifiedAccessToken, unverifiedRefreshToken } = { unverifiedAccessToken: undefined, unverifiedRefreshToken: undefined };

    if (authCookie) {
        try {
            const parsedCookie = JSON.parse(authCookie); // throws on error
            const { access, refresh } = parsedCookie;
            unverifiedAccessToken = access;
            unverifiedRefreshToken = refresh;



            if (!unverifiedAccessToken || !unverifiedRefreshToken) {
                throw new Error("Invalid cookie data")
            }

            try {

                // Verify unverifiedAccessToken 
                // throws error if not verified and if time expired
                const verifiedAccessToken = jwt.verify(access, JWT_ACCESS_SECRET, {
                    issuer: JWT_ISSUER,
                    subject: "ACCESS_TOKEN"
                }) as {
                    iss: string,
                    systemUserName: string,
                    systemUserEmail: string,
                    provider: LoginTypeEnum,
                    exp: number,
                    sub: 'ACCESS_TOKEN' | 'REFRESH_TOKEN'
                }

                event.locals.user = {
                    name: verifiedAccessToken.systemUserName,
                    email: verifiedAccessToken.systemUserEmail,
                    provider: verifiedAccessToken.provider as LoginTypeEnum
                } as SystemUser;


                console.log("Verified access cookie: ", verifiedAccessToken)
            } catch (e: Error | any) {
                if (e?.message?.includes("jwt expired")) {
                    console.log("Access token expired. trying to refresh token")
                    const verifiedRefreshToken = jwt.verify(refresh, JWT_REFRESH_SECRET, {
                        issuer: JWT_ISSUER,
                        subject: "REFRESH_TOKEN"
                    }) as {
                        iss: string,
                        systemUserName: string,
                        systemUserEmail: string,
                        provider: LoginTypeEnum,
                        exp: number,
                        sub: 'ACCESS_TOKEN' | 'REFRESH_TOKEN'
                    }
                    const res = await fetch(BackendEndpoints.REFRESH_TOKEN, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                        },
                        body: JSON.stringify({
                            refreshToken: unverifiedRefreshToken,
                            provider: verifiedRefreshToken.provider
                        })
                    }).then(res => res.json()).catch(() => { return { success: false } })

                    if (res.success == true) {
                        event.cookies.set(AUTH_COOKIE_NAME, JSON.stringify(res.tokens), { path: '/', maxAge: parseInt(AUTH_COOKIE_EXPIRES_IN) });
                        // set user
                        event.locals.user = {
                            name: verifiedRefreshToken.systemUserName,
                            email: verifiedRefreshToken.systemUserEmail,
                            provider: verifiedRefreshToken.provider as LoginTypeEnum
                        } as SystemUser;

                        // console.log("Refreshed token: ", res.tokens)
                        return await resolve(event); // we'll validate in the next request
                    }

                    // console.log("verified refresh token: ", verifiedRefreshToken)

                }
                throw e;

                // console.log("Error verifying access token: ", e?.message || e,)
                // console.log("The E: ", e,)
            }


        } catch (e: Error | any) {
            console.log("Error: ", e?.message || e)
            // delete cookie
            event.cookies.delete(AUTH_COOKIE_NAME, { path: '/' })
        }

        // const user: User = JSON.parse(authCookie);
        // event.locals.user = user;
    }

    const response = await resolve(event);
    // set cookies
    return response;
};