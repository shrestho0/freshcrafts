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

export const load: PageServerLoad = async ({ locals, url, cookies, params, fetch }) => {




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
		console.log('sysconfToUpdate', sysconfToUpdate)
		const actualSysConf = await EngineConnection.getInstance().getSystemConfig();

		if (actualSysConf?.systemUserOauthGithubEnabled && actualSysConf?.systemUserOAuthGithubId) {
			if (sysconfToUpdate.systemUserOAuthGithubId != actualSysConf.systemUserOAuthGithubId) {
				throw new Error('Github account mismatch')
			}
		}


		const up = await EngineConnection.getInstance().updateSystemConfigPartial(sysconfToUpdate)

		// console.log('up', up)

		if (!up.success) { return up }

		// console.log('from_page', from_page)

		if (from_page != 'setup' && from_page != 'link') {
			// generateToken
			// console.log('\n\nGenerating token\n\n')
			const tokens = await generateEngineTokens(sysconfToUpdate.systemUserOAuthGithubId, fetch);
			// console.log('TOKEN GENERATION RESPONSE', tokens);


			if (!tokens.success) return { success: false, message: 'Failed to generate token' }



			// set tokens in cookie if not logged in 
			if (!locals.user) {
				cookies.set(AUTH_COOKIE_NAME, JSON.stringify(tokens?.tokens), {
					path: '/',
					secure: false,
					maxAge: parseInt(AUTH_COOKIE_EXPIRES_IN)
				});
			}
		}



		return { success: true, message: 'Github oauth success', closeWindow }
	} catch (e: any) {
		return { success: false, message: e?.message ?? 'Failed to authorize Github OAuth' }
	}

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