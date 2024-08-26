import { BackendEndpoints } from '@/backend-endpoints';
import type { PageServerLoad } from './$types';
import { error, fail, redirect, type Actions } from '@sveltejs/kit';
import { EngineConnection } from '@/server/EngineConnection';

export const load: PageServerLoad = async ({ locals, cookies }) => {
	// check if already setup
	// if not, okay,
	// if yes, add setup complete to cookie
	// // and redirect to dashboard

	cookies.set(
		'fromPage',
		'setup',
		{
			path: '/',
		}
	)


	const sysConf = await EngineConnection.getInstance().getSystemConfig();
	console.log("sysconf", sysConf);
	// console.log(sysConf);
	let allowed = sysConf != null && Object.keys(sysConf).includes("systemUserSetupComplete") && sysConf.systemUserSetupComplete === false

	if (!allowed) {
		return redirect(307, '/dashboard');
	}

	return {
		sysConf
	}


};

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
				systemUserOAuthGoogleEmail: null,
				systemUserOauthGoogleData: null,

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
			systemUserPasswordHash
		} = Object.fromEntries(await request.formData()) as {
			systemUserEmail: string;
			systemUserName: string;
			systemUserPasswordHash: string;
		}
		const errors = {
			systemUserEmail: '',
			systemUserName: '',
			systemUserPasswordHash: '',
		}

		if (!systemUserEmail) {
			errors.systemUserEmail = "Invalid email"
		}

		if (!systemUserPasswordHash) {
			errors.systemUserPasswordHash = "Invalid password"
		}
		if (errors.systemUserEmail || errors.systemUserPasswordHash) {
			return fail(403, errors);
		}
		console.log("systemUserEmail", systemUserEmail);

		const up = await EngineConnection.getInstance().updateSystemConfigPartial({
			systemUserSetupComplete: true,
			systemUserEmail,
			systemUserName,
			systemUserPasswordHash
		})
		if (up.success) {
			return up;
		} else {
			return fail(403, up);
		}
	}

};