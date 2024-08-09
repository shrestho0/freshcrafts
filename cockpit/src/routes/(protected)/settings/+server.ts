import { BackendEndpoints } from '@/backend-endpoints';
import { EngineConnection } from '@/server/EngineConnection';
import { AuthProviderType } from '@/types/enums';
import { json, type RequestHandler } from '@sveltejs/kit';

export const PATCH: RequestHandler = async ({ request }) => {
	const {
		update_type = 'oauth_connect',
		provider,
		githubId,
		googleEmail
	}: {
		update_type: 'oauth_connect' | 'oauth_disconnect' | any;
		provider: AuthProviderType;
		githubId?: string;
		googleEmail?: string;
	} = await request.json();

	let res = {
		success: false,
		message: 'Invalid Request. Check update_type param'
	};

	switch (update_type) {
		case 'oauth_connect':
			if (githubId) {
				res = await EngineConnection.getInstance().updateSystemConfigPartial({
					systemUserOauthGithubEnabled: true,
					systemUserOAuthGithubId: githubId
				} as any);
			} else if (googleEmail) {
				res = await EngineConnection.getInstance().updateSystemConfigPartial({
					systemUserOauthGoogleEnabled: true,
					systemUserOAuthGoogleEmail: googleEmail
				});
			} else {
				res = {
					success: false,
					message: 'Invalid `provider` type'
				};
			}
			break;
		case 'oauth_disconnect':
			console.log(
				'trying to disconnect: ',
				provider,
				provider === AuthProviderType.OAUTH_GITHUB,
				provider === AuthProviderType.OAUTH_GOOGLE
			);
			if (provider === AuthProviderType.OAUTH_GITHUB) {
				res = await EngineConnection.getInstance().updateSystemConfigPartial({
					systemUserOauthGithubEnabled: false,
					systemUserOAuthGithubId: null
				} as any);
			} else if (provider === AuthProviderType.OAUTH_GOOGLE) {
				res = await EngineConnection.getInstance().updateSystemConfigPartial({
					systemUserOauthGoogleEnabled: false,
					systemUserOAuthGoogleEmail: null
				});
			} else {
				res = {
					success: false,
					message: 'Invalid `provider` type'
				};
			}

			// update from engine

			break;
		default:
			res = {
				success: false,
				message: 'Invalid Request'
			};
			break;
	}

	return json(res);
};
