// import {
// 	AUTH_COOKIE_EXPIRES_IN,
// 	AUTH_COOKIE_NAME,
// 	GITHUB_CLIENT_ID,
// 	GITHUB_CLIENT_SECRET,
// 	OAUTH_STATE_COOKIE_NAME
// } from '$env/static/private';
// import type { OAuthCallbackInternalResponse, OAuthGithubDataRequestDto } from '@/types/internal';
// import type { PageServerLoad } from './$types';
// import { BackendEndpoints } from '@/backend-endpoints';
// import { AuthProviderType } from '@/types/enums';
// import { redirect } from '@sveltejs/kit';

import { EngineConnection } from "@/server/EngineConnection";
import type { PageServerLoad } from "./$types";
import { AUTH_COOKIE_EXPIRES_IN, AUTH_COOKIE_NAME, GITHUB_CLIENT_ID, GITHUB_CLIENT_SECRET, GITHUB_WEBHOOK_PEM_KEY } from "$env/static/private";
import { BackendEndpoints } from "@/backend-endpoints";
import { AuthProviderType } from "@/types/enums";
import type { EngineSystemConfigResponseDto } from "@/types/dtos";

// export const load: PageServerLoad = async ({ locals, url, cookies, params }) => {
// 	const dataToSendToEngine: OAuthGithubDataRequestDto = {
// 		token: undefined,
// 		userInfo: undefined,
// 		dataJson: undefined,
// 		emails: [],
// 		oAuthGithubId: ''
// 	};
// 	const responseObject: OAuthCallbackInternalResponse = {
// 		success: false,
// 		provider: 'github',
// 		closeWindow: false,
// 		message: '',
// 		data: dataToSendToEngine,
// 		redirect: ''
// 	};

// 	// popup for this page only rest will be redirected
// 	let fromPage: 'setup' | 'link' | undefined = cookies.get('fromPage') as any;
// 	// http://localhost:10001/_/oauth-callback/github?code=e6f602e67ede9bfccbbd&installation_id=53847602&setup_action=install
// 	const savedState = cookies.get(OAUTH_STATE_COOKIE_NAME);

// 	// Data from github callback
// 	const oData = {
// 		code: url.searchParams.get('code'),
// 		state: url.searchParams.get('state')
// 	};

// 	if (!oData.code) {
// 		responseObject.message = 'Code not found';
// 		return responseObject;
// 	}

// 	const isSetupAction = url.searchParams.get('setup_action') == 'install';
// 	console.log('isSetupAction', isSetupAction);


// 	// if (savedState != oData.state) {
// 	// 	responseObject.message = 'State mismatch';
// 	// 	return responseObject;
// 	// }

// 	// verify from gh

// 	const gh_url = new URL('https://github.com/login/oauth/access_token');
// 	gh_url.searchParams.set('client_id', GITHUB_CLIENT_ID);
// 	gh_url.searchParams.set('client_secret', GITHUB_CLIENT_SECRET);
// 	gh_url.searchParams.set('code', oData.code as string);

// 	const gh_res = await fetch(gh_url, {
// 		method: 'POST',
// 		headers: { Accept: 'application/json', 'Content-Type': 'application/json' }
// 	})
// 		.then((res) => res.json())
// 		.catch((err) => {
// 			console.log('gh_err', err);
// 			return null;
// 		});

// 	// console.log("gh_res", gh_res)
// 	if (!gh_res || !gh_res.access_token) {
// 		responseObject.message = 'Failed to get access token';
// 		return responseObject;
// 	}

// 	// get users info and save it
// 	const gh_user = await fetch('https://api.github.com/user', {
// 		headers: {
// 			Accept: 'application/json',
// 			Authorization: `Bearer ${gh_res.access_token}`
// 		}
// 	})
// 		.then((res) => res.json())
// 		.catch((err) => {
// 			console.log('gh_user_err', err);
// 			return null;
// 		});

// 	if (!gh_user) {
// 		responseObject.message = 'Failed to get user info';
// 		return responseObject;
// 	}

// 	// const gh_user_emails = await fetch('https://api.github.com/user/emails', {
// 	// 	headers: {
// 	// 		Accept: 'application/json',
// 	// 		Authorization: `Bearer ${gh_res.access_token}`
// 	// 	}
// 	// })
// 	// 	.then((res) => res.json())
// 	// 	.catch((err) => {
// 	// 		console.log('gh_user_emails_err', err);
// 	// 		return null;
// 	// 	}); 

// 	// TODO: Ekhane page er hishab hobe,
// 	// Link or Setup page theke ashle windowClose korbe, data save korbe
// 	// Other jekono page theke ashle
// 	// Try generating token from engine /tokens/generate
// 	// On success, set tokens, on failure show error messages [maybe, account not authorized or something like that]
// 	// finally save tokens to cookie
// 	// and redirect
// 	// TODO: Google er jonno same hobe

// 	// TODO: Temporary data, we'll serialize later
// 	dataToSendToEngine.token = gh_res.access_token;
// 	dataToSendToEngine.userInfo = gh_user;
// 	dataToSendToEngine.dataJson = gh_res;

// 	console.log(dataToSendToEngine)

// 	// console.log('gh_user_emails', gh_user_emails);
// 	// dataToSendToEngine.emails = gh_user_emails.map((e: any) => e.email)

// 	// thakbe
// 	dataToSendToEngine.oAuthGithubId = gh_user.id;

// 	if (fromPage != 'setup' && fromPage != 'link') {
// 		// try generating token from engine
// 		// we'll check with provider and consiting email
// 		const res = await fetch(BackendEndpoints.GENERATE_TOKEN, {
// 			method: 'POST',
// 			headers: {
// 				'Content-Type': 'application/json'
// 			},
// 			body: JSON.stringify({
// 				provider: AuthProviderType.OAUTH_GITHUB,
// 				// check all email with all emails, bad approach, nedda fix on refactor, proceeding for now
// 				// email: dataToSendToEngine.emails,
// 				// oAuthEmails: dataToSendToEngine.emails
// 				githubId: gh_user.id
// 			})
// 		})
// 			.then((res) => res.json())
// 			.catch(() => {
// 				return {
// 					success: false,
// 					message: 'Failed to communicate with `engine`'
// 				};
// 			});

// 		console.log('TOKEN GENERATION RESPONSE', res);

// 		if (res.success == true) {
// 			// set tokens in cookie
// 			// cookies.set('_freshCraftsTokens', JSON.stringify(res.data));
// 			cookies.set(AUTH_COOKIE_NAME, JSON.stringify(res.tokens), {
// 				path: '/',
// 				secure: false,
// 				maxAge: parseInt(AUTH_COOKIE_EXPIRES_IN)
// 			});

// 			// redirecting to dashboard, seems all fine
// 			// return redirect(307, '/dashboard')
// 			// redirect from frontend
// 			responseObject.success = true;
// 			responseObject.redirect = '/dashboard';
// 			responseObject.data = dataToSendToEngine;
// 			return responseObject;
// 		}
// 	}

// 	responseObject.success = true;
// 	responseObject.message = 'Success';
// 	responseObject.closeWindow = true;
// 	responseObject.data = dataToSendToEngine;
// 	return responseObject;
// };


export const load: PageServerLoad = async ({ locals, url, cookies, params, fetch }) => {

	// const code = url.searchParams.get('code');
	// const isSetupAction = url.searchParams.get('setup_action') == 'install';

	// const closeWindow = from_page == 'setup' || from_page == 'link'

	// if (!code) {
	// 	return {
	// 		success: false,
	// 		message: 'Code not found'
	// 	}
	// }



	const from_page = cookies.get('fromPage') as 'setup' | 'link' | undefined;
	const code = url.searchParams.get('code');

	const closeWindow = from_page == 'setup' || from_page == 'link'

	if (!code) {
		return {
			success: false,
			message: 'Code not found'
		}
	}
	try {


		const gh_url = new URL('https://github.com/login/oauth/access_token');
		gh_url.searchParams.set('client_id', GITHUB_CLIENT_ID);
		gh_url.searchParams.set('client_secret', GITHUB_CLIENT_SECRET);
		gh_url.searchParams.set('code', code as string);

		const gh_res = await getAccessToken(gh_url.toString(), code, fetch);
		console.log('gh_res', gh_res);
		if (!gh_res || !gh_res.access_token) return {
			success: false,
			message: 'Failed to get access token. Code is expired or invalid',

		};

		// get users info and save it
		const gh_user = await getUserInfo(gh_res.access_token, fetch);
		console.log('gh_user', gh_user);
		if (!gh_user) return {
			success: false,
			message: 'Failed to get user info'
		}


		const sysconfToUpdate: Partial<EngineSystemConfigResponseDto> = {
			systemUserOauthGithubEnabled: true,
			systemUserOAuthGithubId: gh_user?.id,
			systemUserOauthGithubData: {
				user_access_token: gh_res.access_token,
			}
		}

		const actualSysConf = await EngineConnection.getInstance().getSystemConfig();

		if (actualSysConf?.systemUserOauthGithubEnabled && actualSysConf?.systemUserOAuthGithubId) {
			if (sysconfToUpdate.systemUserOAuthGithubId != actualSysConf.systemUserOAuthGithubId) {
				throw new Error('Github account mismatch')
			}
		}


		const up = await EngineConnection.getInstance().updateSystemConfigPartial(sysconfToUpdate)


		if (!up.success) {
			return up
		}



		if (from_page != 'setup' && from_page != 'link') {
			// generateToken
			console.log('\n\nGenerating token\n\n')
			const tokens = await generateEngineTokens(sysconfToUpdate.systemUserOAuthGithubId, fetch);
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
			message: 'Github oauth success',
			closeWindow
		}
	} catch (e: any) {
		return {
			success: false,
			message: e?.message ?? 'Failed to authorize Github OAuth'
		}
	}

	// const gh_url = new URL('https://github.com/login/oauth/access_token');
	// gh_url.searchParams.set('client_id', GITHUB_CLIENT_ID);
	// gh_url.searchParams.set('client_secret', GITHUB_CLIENT_SECRET);
	// gh_url.searchParams.set('code', code as string);

	// const gh_res = await getAccessToken(gh_url.toString(), code, fetch);
	// console.log('gh_res', gh_res);
	// if (!gh_res || !gh_res.access_token) return {
	// 	success: false,
	// 	message: 'Failed to get access token. Code is expired or invalid',

	// };

	// // get users info and save it
	// const gh_user = await getUserInfo(gh_res.access_token, fetch);
	// console.log('gh_user', gh_user);
	// if (!gh_user) return {
	// 	success: false,
	// 	message: 'Failed to get user info'
	// }


	// // jodi ager theke github thake, validate
	// // else new one is the only one
	// const sysConf = await EngineConnection.getInstance().getSystemConfig();
	// let partial_sysconf = {};
	// if (sysConf?.systemUserOauthGithubEnabled && sysConf?.systemUserOAuthGithubId) {
	// 	// github enabled
	// 	// validate
	// 	console.log('github enabled')
	// 	if (sysConf.systemUserOAuthGithubId != gh_user?.id) {
	// 		return {
	// 			success: false,
	// 			message: 'Github account mismatch'
	// 		}
	// 	}

	// 	// update access token
	// 	partial_sysconf = {
	// 		systemUserOauthGithubData: {
	// 			user_access_token: gh_res.access_token
	// 		}
	// 	}

	// 	if (from_page != 'setup' && from_page != 'link') {
	// 		// generateToken
	// 		console.log('\n\nGenerating token\n\n')
	// 		const tokens = await generateEngineTokens(gh_user.id, fetch);
	// 		console.log('TOKEN GENERATION RESPONSE', tokens);
	// 		if (!tokens.success) {
	// 			return {
	// 				success: false,
	// 				message: 'Failed to generate token'
	// 			}
	// 		}

	// 		// set tokens in cookie if not logged in 
	// 		if (!locals.user) {
	// 			cookies.set(AUTH_COOKIE_NAME, JSON.stringify(tokens?.tokens), {
	// 				path: '/',
	// 				secure: false,
	// 				maxAge: parseInt(AUTH_COOKIE_EXPIRES_IN)
	// 			});
	// 		}
	// 	}



	// } else {
	// 	// valiate code
	// 	partial_sysconf = {
	// 		systemUserOauthGithubEnabled: true,
	// 		systemUserOAuthGithubId: gh_user?.id,
	// 		systemUserOauthGithubData: {
	// 			user_access_token: gh_res.access_token,
	// 		}
	// 	}
	// }

	// // if any key is present in partial_sysconf update
	// if (Object.keys(partial_sysconf).length > 0) {
	// 	// save to engine
	// 	const up = await EngineConnection.getInstance().updateSystemConfigPartial(partial_sysconf)
	// 	if (up.success) {
	// 		return {
	// 			success: true,
	// 			message: 'Github oauth success',
	// 			closeWindow: closeWindow,
	// 		}
	// 	}

	// 	return up;
	// } else {
	// 	return {
	// 		success: false,
	// 		message: 'Invalid request'
	// 	}
	// }
}

async function generateEngineTokens(gh_user_id: any, fetch: any) {
	const res = await fetch(BackendEndpoints.GENERATE_TOKEN, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({
			provider: AuthProviderType.OAUTH_GITHUB,
			// check all email with all emails, bad approach, nedda fix on refactor, proceeding for now
			githubId: gh_user_id
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

async function getAccessToken(gh_url: string, code: string, fetch: any) {
	return fetch(gh_url, {
		method: 'POST',
		headers: { Accept: 'application/json', 'Content-Type': 'application/json' }
	})
		.then((res: { json: () => any; }) => res.json())
		.catch((err: any) => {
			console.log('gh_err', err);
			return null;
		});
}

async function getUserInfo(access_token: string, fetch: any) {
	return fetch('https://api.github.com/user', {
		headers: {
			Accept: 'application/json',
			Authorization: `Bearer ${access_token}`
		}
	})
		.then((res: { json: () => any; }) => res.json())
		.catch((err: any) => {
			console.log('gh_user_err', err);
			return null;
		});
}