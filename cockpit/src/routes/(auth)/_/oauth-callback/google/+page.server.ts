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
import { google } from "googleapis";
import type { PageServerLoad } from "./$types";
import type { OAuthGoogleIdTokenData } from "@/types/internal";
import { jwtDecode } from "jwt-decode";
import type { EngineSystemConfigResponseDto } from "@/types/dtos";
import { EngineConnection } from "@/server/EngineConnection";
import { BackendEndpoints } from "@/backend-endpoints";
import { AuthProviderType } from "@/types/enums";

// import { jwtDecode } from 'jwt-decode';
// import { BackendEndpoints } from '@/backend-endpoints';
// import { AuthProviderType } from '@/types/enums';
// import { redirect } from '@sveltejs/kit';
// import { PUBLIC_GOOGLE_OAUTH_CALLBACK_URL } from '$env/static/public';

// export const load: PageServerLoad = async ({ locals, url, cookies, params }) => {
// 	const oauth2Client = new google.auth.OAuth2(
// 		GOOGLE_CLIENT_ID,
// 		GOOGLE_CLIENT_SECRET,
// 		url.origin + PUBLIC_GOOGLE_OAUTH_CALLBACK_URL
// 	);

// 	const dataToSendToEngine: OAuthGithubDataRequestDto = {
// 		token: '',
// 		userInfo: undefined,
// 		dataJson: undefined,
// 		emails: []
// 	};
// 	const responseObject: OAuthCallbackInternalResponse = {
// 		success: false,
// 		provider: 'google',
// 		closeWindow: false,
// 		message: '',
// 		data: dataToSendToEngine,
// 		redirect: ''
// 	};

// 	// popup for this page only rest will be redirected
// 	let fromPage: 'setup' | 'link' | undefined = cookies.get('fromPage') as any;

// 	// // Data from github callback
// 	const oData = {
// 		code: url.searchParams.get('code'),
// 		state: url.searchParams.get('state')
// 	};

// 	// console.log(" GOOGLE OAUTH CALLBACK DATA", oData)

// 	if (!oData.code) {
// 		responseObject.message = 'Code not found';
// 		return responseObject;
// 	}

// 	// console.log(oData)

// 	try {
// 		let { tokens } = await oauth2Client.getToken(oData.code);
// 		let decodedUserInfo: OAuthGoogleIdTokenData | null = null;
// 		if (tokens?.id_token) decodedUserInfo = jwtDecode(tokens?.id_token);
// 		dataToSendToEngine.token = tokens?.access_token ?? '';
// 		dataToSendToEngine.userInfo = decodedUserInfo;
// 		dataToSendToEngine.dataJson = tokens;

// 		dataToSendToEngine.emails = [decodedUserInfo?.email ?? ''];

// 		// Thakbe
// 		dataToSendToEngine.oAuthGoogleEmail = decodedUserInfo?.email ?? '';

// 		if (fromPage == 'setup' || fromPage == 'link') {
// 			// do stuff

// 			responseObject.success = true;
// 			responseObject.message = 'Success';
// 			responseObject.closeWindow = true;
// 			responseObject.data = dataToSendToEngine;

// 			return responseObject;
// 		} else {
// 			const res = await fetch(BackendEndpoints.GENERATE_TOKEN, {
// 				method: 'POST',
// 				headers: {
// 					'Content-Type': 'application/json'
// 				},
// 				body: JSON.stringify({
// 					provider: AuthProviderType.OAUTH_GOOGLE,
// 					// check all email with all emails, bad approach, nedda fix on refactor, proceeding for now
// 					// email: dataToSendToEngine.emails,
// 					// oAuthEmails: dataToSendToEngine.emails
// 					// oAuthGithubId: dataToSendToEngine.oAuthGithubId,
// 					googleEmail: dataToSendToEngine.oAuthGoogleEmail
// 				})
// 			})
// 				.then((res) => res.json())
// 				.catch(() => {
// 					return {
// 						success: false,
// 						message: 'Failed to communicate with `engine`'
// 					};
// 				});
// 			console.log('ENGINE TOKENS', res);
// 			if (res.success == true) {
// 				// set tokens in cookie
// 				// cookies.set('_freshCraftsTokens', JSON.stringify(res.data));
// 				cookies.set(AUTH_COOKIE_NAME, JSON.stringify(res.tokens), {
// 					path: '/',
// 					maxAge: parseInt(AUTH_COOKIE_EXPIRES_IN)
// 				});
// 			} else {
// 				console.error('Failed to generate token from engine', res);
// 			}

// 			responseObject.success = true;
// 			responseObject.message = 'Success';
// 			// responseObject.closeWindow = true
// 			responseObject.redirect = '/dashboard';
// 			responseObject.data = dataToSendToEngine;

// 			return responseObject;
// 		}
// 	} catch (e) { }

// 	// // decoding userInfo
// 	// // try {
// 	// let { tokens } = await oauth2Client.getToken(oData.code);

// 	// console.log("GOOGLE TOKENS", tokens)

// 	// let decodedUserInfo: OAuthGoogleIdTokenData | null = null;
// 	// try {
// 	//     if (tokens?.id_token) decodedUserInfo = jwtDecode(tokens?.id_token)

// 	//     if (tokens) {
// 	//         dataToSendToEngine.token = tokens?.access_token ?? '';
// 	//         dataToSendToEngine.userInfo = decodedUserInfo;
// 	//         dataToSendToEngine.dataJson = tokens

// 	//         dataToSendToEngine.emails = [decodedUserInfo?.email ?? '']

// 	//         // Thakbe
// 	//         dataToSendToEngine.oAuthGoogleEmail = decodedUserInfo?.email ?? ''

// 	//         if (fromPage != 'setup' && fromPage != 'link') {
// 	//             // try generating token from engine
// 	//             // we'll check with provider and consiting email
// 	//             const res = await fetch(BackendEndpoints.GENERATE_TOKEN, {
// 	//                 method: 'POST',
// 	//                 headers: {
// 	//                     'Content-Type': 'application/json',
// 	//                 },
// 	//                 body: JSON.stringify({
// 	//                     provider: AuthProviderType.OAUTH_GOOGLE,
// 	//                     // check all email with all emails, bad approach, nedda fix on refactor, proceeding for now
// 	//                     // email: dataToSendToEngine.emails,
// 	//                     // oAuthEmails: dataToSendToEngine.emails
// 	//                     // oAuthGithubId: dataToSendToEngine.oAuthGithubId,
// 	//                     googleEmail: dataToSendToEngine.oAuthGoogleEmail,
// 	//                 })
// 	//             }).then(res => res.json()).catch(() => {
// 	//                 return {
// 	//                     success: false,
// 	//                     message: 'Failed to communicate with `engine`'
// 	//                 };
// 	//             });
// 	//             console.log("ENGINE TOKENS", res)
// 	//             if (res.success == true) {
// 	//                 // set tokens in cookie
// 	//                 // cookies.set('_freshCraftsTokens', JSON.stringify(res.data));
// 	//                 cookies.set(AUTH_COOKIE_NAME, JSON.stringify(res.tokens), {
// 	//                     path: '/',
// 	//                     maxAge: parseInt(AUTH_COOKIE_EXPIRES_IN)
// 	//                 });

// 	//                 // redirecting to dashboard, seems all fine
// 	//                 return redirect(307, '/dashboard')
// 	//             }

// 	//         }

// 	//         responseObject.success = true
// 	//         responseObject.closeWindow = true
// 	//         responseObject.provider = 'google'
// 	//         responseObject.data = dataToSendToEngine

// 	//         return responseObject

// 	//     }
// 	// } catch (e) {

// 	// }

// 	responseObject.message = 'Invalid token';
// 	return responseObject;
// };







export const load: PageServerLoad = async ({ locals, url, cookies, params, fetch }) => {
	const oauth2Client = new google.auth.OAuth2(
		GOOGLE_CLIENT_ID,
		GOOGLE_CLIENT_SECRET,
		url.origin + PUBLIC_GOOGLE_OAUTH_CALLBACK_URL
	);


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

	try {

		let { tokens } = await oauth2Client.getToken(code);
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
			const tokens = await generateEngineTokens(sysconfToUpdate.systemUserOAuthGoogleEmail, fetch);
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


async function generateEngineTokens(email: any, fetch: any) {
	const res = await fetch(BackendEndpoints.GENERATE_TOKEN, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({
			provider: AuthProviderType.OAUTH_GOOGLE,
			googleEmail: email
		})
	})
		.then((res: { json: () => any; }) => res.json())
		.catch(() => {
			return {
				success: false,
				message: 'Failed to communicate with `engine`'
			};
		});

	return res;
}