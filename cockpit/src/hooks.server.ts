import {
	AUTH_COOKIE_EXPIRES_IN,
	AUTH_COOKIE_NAME,
	JWT_ACCESS_SECRET,
	JWT_ISSUER,
	JWT_REFRESH_SECRET,
	COCKPIT_SSE_TOKEN,
	ENV
} from '$env/static/private';
import { EngineConnection } from '@/server/EngineConnection';
import type { SystemUser } from '@/types/entities';
import type { AuthProviderType } from '@/types/enums';
import type { CustomJwtPayload } from '@/types/internal';
import { error, type Handle, type RequestEvent } from '@sveltejs/kit';
import jwt from 'jsonwebtoken';

// verify access token util
function verifyAccessToken(token: string): CustomJwtPayload {
	return jwt.verify(token, JWT_ACCESS_SECRET, { issuer: JWT_ISSUER, subject: 'ACCESS_TOKEN' }) as CustomJwtPayload;
}

// verify refresh token util
function verifyRefreshToken(token: string): CustomJwtPayload {
	return jwt.verify(token, JWT_REFRESH_SECRET, { issuer: JWT_ISSUER, subject: 'REFRESH_TOKEN' }) as CustomJwtPayload;
}

// refresh token util
async function refreshToken(event: RequestEvent<Partial<Record<string, string>>, string | null>, refreshToken: string, provider: AuthProviderType) {
	const res = await EngineConnection.getInstance().refreshToken(refreshToken, provider);
	if (res.success) {
		event.cookies.set(AUTH_COOKIE_NAME, JSON.stringify(res?.tokens), {
			path: '/',
			secure: ENV === "prod", httpOnly: true,
			sameSite: 'strict',
			maxAge: parseInt(AUTH_COOKIE_EXPIRES_IN)
		});
		return res?.tokens;
	}
	throw new Error('Failed to refresh token');
}

// Centralized error handling
function handleError(e: any, event) {
	console.log('Error: ', e?.message || e);
	event.cookies.delete(AUTH_COOKIE_NAME, { path: '/', secure: ENV === "prod", httpOnly: true, });
	event.cookies.set('flash_message', 'Refresh token expired. Please login again', {
		path: '/',
		secure: false,
		httpOnly: true,
	});
}

export const handle: Handle = async ({ event, resolve }) => {
	console.log('[DEBUG]: hook.server.ts from', {
		origin: event.url.origin,
		host: event.url.host,
	});

	// Handle SSE endpoint authentication
	if (
		event.url.pathname?.startsWith('/sse') &&
		event.request.method?.trim().toUpperCase() === 'PATCH'
	) {
		try {
			const authorizationToken = event.request.headers.get('authorization')?.trim();
			if (authorizationToken !== `Bearer ${COCKPIT_SSE_TOKEN}`) {
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

	// Handle cookies and token validation
	const authCookie = event.cookies.get(AUTH_COOKIE_NAME);
	if (authCookie) {
		try {
			const parsedCookie = JSON.parse(authCookie);
			const { access, refresh } = parsedCookie;
			unverifiedAccessToken = access;
			unverifiedRefreshToken = refresh;

			if (!unverifiedAccessToken || !unverifiedRefreshToken) {
				throw new Error('Invalid cookie data');
			}

			try {
				// Verify access token
				const verifiedAccessToken = verifyAccessToken(access);

				// Set user in event.locals
				event.locals.user = {
					name: verifiedAccessToken.systemUserName,
					email: verifiedAccessToken.systemUserEmail,
					provider: verifiedAccessToken.provider as AuthProviderType
				} as SystemUser;

			} catch (e: Error | any) {
				if (e?.message?.includes('jwt expired')) {
					console.log('Access token expired. Trying to refresh token');
					const verifiedRefreshToken = verifyRefreshToken(refresh);

					const tokens = await refreshToken(event, unverifiedRefreshToken, verifiedRefreshToken.provider);

					// Set user in event.locals
					event.locals.user = {
						name: verifiedRefreshToken.systemUserName,
						email: verifiedRefreshToken.systemUserEmail,
						provider: verifiedRefreshToken.provider as AuthProviderType
					} as SystemUser;

					// Refreshed token, resolve the request
					return await resolve(event);
				}
				throw e;
			}
		} catch (e: Error | any) {
			handleError(e, event);
		}
	}

	// Proceed with the request
	const response = await resolve(event);
	return response;
};




// import {
// 	AUTH_COOKIE_EXPIRES_IN,
// 	AUTH_COOKIE_NAME,
// 	JWT_ACCESS_SECRET,
// 	JWT_ISSUER,
// 	JWT_REFRESH_SECRET,
// 	COCKPIT_SSE_TOKEN,
// 	ENV
// } from '$env/static/private';
// import { EngineConnection } from '@/server/EngineConnection';
// import type { SystemUser } from '@/types/entities';
// import type { AuthProviderType } from '@/types/enums';
// import type { CustomJwtPayload } from '@/types/internal';
// import { error, json, type Handle } from '@sveltejs/kit';

// import jwt from 'jsonwebtoken';

// type User = {
// 	name: string;
// 	email: string;
// };

// export const handle: Handle = async ({ event, resolve }) => {
// 	console.log('[DEBUG]: hook.server.ts from', {
// 		origin: event.url.origin,
// 		host: event.url.host,
// 	});



// 	if (
// 		event.url.pathname?.startsWith('/sse') &&
// 		event.request.method?.trim().toUpperCase() === 'PATCH'
// 	) {
// 		// console.log("check check")
// 		try {
// 			const authorizationToken = event.request.headers.get('authorization')?.trim();
// 			if (authorizationToken != `Bearer ${COCKPIT_SSE_TOKEN}`) {
// 				throw new Error('Invalid token');
// 			}
// 		} catch (e: any) {
// 			return error(401, { message: e?.message });
// 		}
// 	}

// 	let { unverifiedAccessToken, unverifiedRefreshToken } = {
// 		unverifiedAccessToken: undefined,
// 		unverifiedRefreshToken: undefined
// 	};


// 	// FIXME: Refactor this shit

// 	// Cookies and System wide data setup
// 	const authCookie = event.cookies.get(AUTH_COOKIE_NAME);
// 	if (authCookie) {
// 		try {
// 			const parsedCookie = JSON.parse(authCookie); // throws on error
// 			const { access, refresh } = parsedCookie;
// 			unverifiedAccessToken = access;
// 			unverifiedRefreshToken = refresh;

// 			if (!unverifiedAccessToken || !unverifiedRefreshToken) {
// 				throw new Error('Invalid cookie data');
// 			}

// 			try {
// 				// Verify unverifiedAccessToken
// 				// throws error if not verified and if time expired
// 				const verifiedAccessToken = jwt.verify(access, JWT_ACCESS_SECRET, {
// 					issuer: JWT_ISSUER,
// 					subject: 'ACCESS_TOKEN'
// 				}) as CustomJwtPayload;

// 				// console.log("Verified access token payload", verifiedAccessToken)
// 				// console.log('Verified access token payload');

// 				event.locals.user = {
// 					name: verifiedAccessToken.systemUserName,
// 					email: verifiedAccessToken.systemUserEmail,
// 					provider: verifiedAccessToken.provider as AuthProviderType
// 				} as SystemUser;

// 				// console.log("Verified access cookie: ", verifiedAccessToken)
// 			} catch (e: Error | any) {
// 				if (e?.message?.includes('jwt expired')) {
// 					console.log('Access token expired. trying to refresh token');
// 					const verifiedRefreshToken = jwt.verify(refresh, JWT_REFRESH_SECRET, {
// 						issuer: JWT_ISSUER,
// 						subject: 'REFRESH_TOKEN'
// 					}) as CustomJwtPayload;

// 					const res = await EngineConnection.getInstance().refreshToken(
// 						// unverifiedAccessToken,
// 						unverifiedRefreshToken,
// 						verifiedRefreshToken.provider
// 					);
// 					// console.log('[DEBUG]: Refresh token response: ', res);

// 					if (res.success == true) {
// 						event.cookies.set(AUTH_COOKIE_NAME, JSON.stringify(res?.tokens), {
// 							path: '/',
// 							secure: ENV == "prod", httpOnly: true,
// 							maxAge: parseInt(AUTH_COOKIE_EXPIRES_IN)
// 						});
// 						// set user
// 						event.locals.user = {
// 							name: verifiedRefreshToken.systemUserName,
// 							email: verifiedRefreshToken.systemUserEmail,
// 							provider: verifiedRefreshToken.provider as AuthProviderType
// 						} as SystemUser;

// 						// console.log("[DEBUG]: Refreshed token")
// 						return await resolve(event); // we'll validate in the next request
// 					}
// 					// console.log("verified refresh token: ", verifiedRefreshToken)
// 					throw new Error('Failed to refresh token');
// 				}
// 				throw e;

// 			}
// 		} catch (e: Error | any) {
// 			console.log('Error: ', e?.message || e);
// 			// delete cookie
// 			event.cookies.delete(AUTH_COOKIE_NAME, { path: '/', secure: ENV == "prod", httpOnly: true });
// 			event.cookies.set('flash_message', 'Refresh token expired. Please login again', {
// 				path: '/',
// 				secure: false,
// 				httpOnly: true,

// 			})
// 		}

// 		// const user: User = JSON.parse(authCookie);
// 		// event.locals.user = user;
// 	}

// 	// test cookie

// 	const response = await resolve(event);
// 	// set cookies
// 	return response;
// };
