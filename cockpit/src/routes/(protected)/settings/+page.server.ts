// import { EngineConnection } from '@/server/EngineConnection';
// import type { Actions, PageServerLoad } from './$types';
// import {
// 	AUTH_COOKIE_EXPIRES_IN,
// 	AUTH_COOKIE_NAME,
// 	OAUTH_STATE_COOKIE_NAME
// } from '$env/static/private';
// import { ulid } from 'ulid';
// import { getGithubLoginUrl, getGooleLoginhUrl } from '@/server/helpers/OAuthHelper';
// import { fail } from '@sveltejs/kit';

// export const load: PageServerLoad = async ({ locals, cookies, url }) => {
// 	const engine = EngineConnection.getInstance();

// 	let oAuthState = cookies.get(OAUTH_STATE_COOKIE_NAME);

// 	if (!oAuthState) {
// 		oAuthState = ulid();
// 		cookies.set(OAUTH_STATE_COOKIE_NAME, oAuthState, { path: '/', maxAge: 60 * 60 * 24 * 3, secure: false });
// 	}

// 	// if (browser) {
// 	// 	document.cookie = 'fromPage=link; path=/; max-age=600'; //
// 	// }

// 	// TODO: set fromPage cookie
// 	return {
// 		githubLoginUrl: getGithubLoginUrl(url.origin, oAuthState),
// 		googleLoginUrl: getGooleLoginhUrl(url.origin, oAuthState),
// 		providers: await engine.getProviders(),
// 		systemConfig: await engine.getSystemConfig(),
// 	};
// };

// export const actions: Actions = {
// 	changeInfo: async ({ request, cookies, locals }) => {
// 		const { name, email } = Object.fromEntries(await request.formData()) as {
// 			name: string;
// 			email: string;
// 		};
// 		if (!name || !email) {
// 			// TODO: Check using regex
// 			return fail(403, { message: 'Enter valid data' });
// 		}

// 		let res = await EngineConnection.getInstance().updateSystemConfigPartial({
// 			systemUserName: name,
// 			systemUserEmail: email
// 		});
// 		if (res.success) {
// 			const { refresh, access } = JSON.parse(cookies.get(AUTH_COOKIE_NAME) ?? '{}');

// 			const t = await EngineConnection.getInstance().refreshToken(refresh, locals.user.provider);
// 			// console.log("Refreshing token from settings page")
// 			if (t.success == true) {
// 				console.log('refresh successfully');
// 				cookies.set(AUTH_COOKIE_NAME, JSON.stringify(t.tokens), {
// 					path: '/',
// 					maxAge: parseInt(AUTH_COOKIE_EXPIRES_IN),
// 					secure: false,
// 				});
// 			}
// 		}

// 		return res;
// 	},
// 	changePassword: async ({ request, cookies, locals }) => {
// 		const { oldPassword, newPassword } = Object.fromEntries(await request.formData()) as {
// 			oldPassword: string;
// 			newPassword: string;
// 		};
// 		// console.log("oldpass", oldPassword, "newpass", newPassword)

// 		if (!oldPassword || !newPassword) {
// 			return fail(403, { message: 'All fields are required' });
// 		}

// 		const res = await EngineConnection.getInstance().changePassword({ oldPassword, newPassword });

// 		console.log('change password response', res);
// 		if (res.success) {
// 			return res;
// 		} else {
// 			return fail(403, res);
// 		}
// 	}
// };

import { getGithubAppInstallationUrl, getGithubLoginUrl, getGooleLoginhUrl } from '@/server/helpers/OAuthHelper';
import { ulid } from 'ulid';
import { OAUTH_STATE_COOKIE_NAME } from '$env/static/private';
import { PUBLIC_GITHUB_APP_INSTALLATION_URL, PUBLIC_GITHUB_OAUTH_CALLBACK_URL } from '$env/static/public';
import type { Actions, PageServerLoad } from './$types';
import { EngineConnection } from '@/server/EngineConnection';
import { fail } from '@sveltejs/kit';

export const load: PageServerLoad = async ({ locals, url, cookies }) => {
	let oAuthState = cookies.get(OAUTH_STATE_COOKIE_NAME);

	// cookies.set(
	// 	'fromPage',
	// 	'link',
	// 	{
	// 		path: '/',
	// 	}
	// )

	if (!oAuthState) {
		oAuthState = ulid();
		cookies.set(OAUTH_STATE_COOKIE_NAME, oAuthState, { path: '/', maxAge: 60 * 60 * 24 * 3, secure: false });
	}

	const sysConf = await EngineConnection.getInstance().getSystemConfig();

	// const oUrls = {
	// 	githubLoginUrl: getGithubLoginUrl(url.origin, oAuthState),
	// 	googleLoginUrl: getGooleLoginhUrl(url.origin, oAuthState),
	// 	githubAppInstallUrl: PUBLIC_GITHUB_APP_INSTALLATION_URL +
	// 		'?redirect_uri=' + url.origin +
	// 		PUBLIC_GITHUB_OAUTH_CALLBACK_URL
	// };

	const oUrls = {
		githubLoginUrl: getGithubLoginUrl(url.origin, oAuthState),
		googleLoginUrl: getGooleLoginhUrl(url.origin, oAuthState),
		githubAppInstallUrl: getGithubAppInstallationUrl(url.origin)
	};

	console.warn(`oUrls`, JSON.stringify(oUrls, null, 2));

	return {
		...oUrls,
		sysConf,
	}


};


// FIXME: Refactor, same code in setup page
export const actions: Actions = {
	removeGithub: async ({ locals, cookies }) => {
		const sysConf = await EngineConnection.getInstance().getSystemConfig();
		let allowed = (sysConf != null && Object.keys(sysConf).includes("systemUserSetupComplete") && sysConf.systemUserSetupComplete === false) || locals.user

		if (allowed) {
			// remove github
			const res = await EngineConnection.getInstance().updateSystemConfigPartial({
				systemUserOauthGithubEnabled: false,
				systemUserOauthGithubData: null,
				systemUserOAuthGithubId: null
			})

			return {
				...res
			}
		}
	},
	removeGoogle: async ({ locals, cookies }) => {
		const sysConf = await EngineConnection.getInstance().getSystemConfig();
		let allowed = (sysConf != null && Object.keys(sysConf).includes("systemUserSetupComplete") && sysConf.systemUserSetupComplete === false) || locals.user
		if (allowed) {
			// remove github
			const res = await EngineConnection.getInstance().updateSystemConfigPartial({
				systemUserOauthGoogleEnabled: false,
				systemUserOauthGoogleData: null,
				systemUserOAuthGoogleEmail: null
			})

			return {
				...res
			}
		}
	},
	saveUserInfo: async ({ locals, cookies, request }) => {
		const {
			systemUserEmail,
			systemUserName,

		} = Object.fromEntries(await request.formData()) as {
			systemUserEmail: string;
			systemUserName: string;
		}
		const errors = {
			systemUserEmail: '',
			systemUserName: '',
		}

		if (!systemUserEmail && !systemUserName) {
			return fail(403, { message: 'Atleast one field is required' })
		}

		const res = await EngineConnection.getInstance().updateSystemConfigPartial({
			systemUserEmail,
			systemUserName
		})

		if (res.success) {
			return res
		} else {
			return fail(403, {
				message: res?.message ?? 'Failed to update user info',
				res,
			})
		}
	},
	updatePassword: async ({ request, cookies, locals }) => {
		const { oldPassword, newPassword } = Object.fromEntries(await request.formData()) as {
			oldPassword: string;
			newPassword: string;
		};
		// console.log("oldpass", oldPassword, "newpass", newPassword)

		if (!oldPassword || !newPassword) {
			return fail(403, { message: 'All fields are required' });
		}

		if (oldPassword === newPassword) {
			return fail(403, { message: 'Old password and new password cannot be same' });
		}

		const res = await EngineConnection.getInstance().changePassword({ oldPassword, newPassword });

		console.log('change password response', res);
		if (res.success) {
			return res;
		} else {
			return fail(403, { message: res?.message ?? 'Failed to change password' });
		}
	}

};