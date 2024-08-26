import {
	AUTH_COOKIE_EXPIRES_IN,
	AUTH_COOKIE_NAME,
	JWT_ACCESS_SECRET,
	JWT_ISSUER,
	JWT_REFRESH_SECRET,
	COCKPIT_SSE_TOKEN
} from '$env/static/private';
import { BackendEndpoints } from '@/backend-endpoints';
import { EngineConnection } from '@/server/EngineConnection';
import type { SystemUser } from '@/types/entities';
import type { AuthProviderType } from '@/types/enums';
import type { CustomJwtPayload } from '@/types/internal';
import { error, json, type Handle } from '@sveltejs/kit';

import jwt from 'jsonwebtoken';

type User = {
	name: string;
	email: string;
};

export const handle: Handle = async ({ event, resolve }) => {
	console.log('[DEBUG]: hook.server.ts from', {
		origin: event.url.origin,
		host: event.url.host,
	});

	// Cokies and System wide data setup
	const authCookie = event.cookies.get(AUTH_COOKIE_NAME);

	if (
		event.url.pathname?.startsWith('/sse') &&
		event.request.method?.trim().toUpperCase() === 'PATCH'
	) {
		// console.log("check check")
		try {
			const authorizationToken = event.request.headers.get('authorization')?.trim();
			if (authorizationToken != `Bearer ${COCKPIT_SSE_TOKEN}`) {
				throw new Error('Invalid token');
			}
		} catch (e: any) {
			return error(401, { message: e?.message });
		}
	}

	let { unverifiedAccessToken, unverifiedRefreshToken } = {
		unverifiedAccessToken: undefined,
		unverifiedRefreshToken: undefined
	};

	if (authCookie) {
		try {
			const parsedCookie = JSON.parse(authCookie); // throws on error
			const { access, refresh } = parsedCookie;
			unverifiedAccessToken = access;
			unverifiedRefreshToken = refresh;

			if (!unverifiedAccessToken || !unverifiedRefreshToken) {
				throw new Error('Invalid cookie data');
			}

			try {
				// Verify unverifiedAccessToken
				// throws error if not verified and if time expired
				const verifiedAccessToken = jwt.verify(access, JWT_ACCESS_SECRET, {
					issuer: JWT_ISSUER,
					subject: 'ACCESS_TOKEN'
				}) as CustomJwtPayload;

				// console.log("Verified access token payload", verifiedAccessToken)
				// console.log('Verified access token payload');

				event.locals.user = {
					name: verifiedAccessToken.systemUserName,
					email: verifiedAccessToken.systemUserEmail,
					provider: verifiedAccessToken.provider as AuthProviderType
				} as SystemUser;

				// console.log("Verified access cookie: ", verifiedAccessToken)
			} catch (e: Error | any) {
				if (e?.message?.includes('jwt expired')) {
					console.log('Access token expired. trying to refresh token');
					const verifiedRefreshToken = jwt.verify(refresh, JWT_REFRESH_SECRET, {
						issuer: JWT_ISSUER,
						subject: 'REFRESH_TOKEN'
					}) as CustomJwtPayload;

					const res = await EngineConnection.getInstance().refreshToken(
						// unverifiedAccessToken,
						unverifiedRefreshToken,
						verifiedRefreshToken.provider
					);
					// console.log('[DEBUG]: Refresh token response: ', res);

					if (res.success == true) {
						event.cookies.set(AUTH_COOKIE_NAME, JSON.stringify(res.tokens), {
							path: '/',
							secure: false,
							maxAge: parseInt(AUTH_COOKIE_EXPIRES_IN)
						});
						// set user
						event.locals.user = {
							name: verifiedRefreshToken.systemUserName,
							email: verifiedRefreshToken.systemUserEmail,
							provider: verifiedRefreshToken.provider as AuthProviderType
						} as SystemUser;

						// console.log("[DEBUG]: Refreshed token")
						return await resolve(event); // we'll validate in the next request
					}
					// console.log("verified refresh token: ", verifiedRefreshToken)
					throw new Error('Failed to refresh token');
				}
				throw e;

				// console.log("Error verifying access token: ", e?.message || e,)
				// console.log("The E: ", e,)
			}
		} catch (e: Error | any) {
			console.log('Error: ', e?.message || e);
			// delete cookie
			event.cookies.delete(AUTH_COOKIE_NAME, { path: '/', secure: false });
		}

		// const user: User = JSON.parse(authCookie);
		// event.locals.user = user;
	}

	// test cookie

	const response = await resolve(event);
	// set cookies
	return response;
};
