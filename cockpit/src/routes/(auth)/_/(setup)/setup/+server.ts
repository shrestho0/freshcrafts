import { BackendEndpoints } from '@/backend-endpoints';
import type { EngineSystemConfigResponseDto } from '@/types/dtos';
import type { SetupPageAccountData, SetupPageOauthData } from '@/types/internal';
import { json, type RequestHandler } from '@sveltejs/kit';

export const POST: RequestHandler = async ({ locals, request }) => {
	try {
		const data = (await request.json()) as {
			account: SetupPageAccountData;
			oauth: SetupPageOauthData;
		};
		console.log(data);

		const sanitizeSystemConfig: EngineSystemConfigResponseDto = {
			systemUserSetupComplete: true,
			systemUserEmail: data.account.email,
			systemUserName: data.account.name,
			systemUserPasswordHash: data.account.password,
			systemUserOauthGithubEnabled: data.oauth.githubStatus === 'connected',
			systemUserOauthGithubData:
				data.oauth.githubStatus === 'connected' ? data.oauth.githubOAuthJson : null,

			systemUserOAuthGithubId: data.oauth.oAuthGithubId,
			systemUserOAuthGoogleEmail: data.oauth.oAuthGoogleEmail,

			systemUserOauthGoogleEnabled: data.oauth.googleStatus === 'connected',
			systemUserOauthGoogleData:
				data.oauth.googleStatus === 'connected' ? data.oauth.googleOAuthJson : null
		};

		// sanitize data

		// Send to engine
		const res = await fetch(BackendEndpoints.SETUP_SYSCONFIG, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(sanitizeSystemConfig)
		})
			.then((res) => res.json())
			.catch((err) => null);

		// console.log(res)
		// throw new Error('Not implemented');
		// return json(res);
		return json(res);
	} catch (err) {
		return json({
			success: false,
			message: 'Failed to setup system config due to invalid data'
		});
	}
};
