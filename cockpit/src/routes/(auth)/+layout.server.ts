import { getGithubAppInstallationUrl, getGithubLoginUrl, getGooleLoginhUrl } from '@/server/helpers/OAuthHelper';
import type { LayoutServerLoad } from './$types';
import { ulid } from 'ulid';
import { OAUTH_STATE_COOKIE_NAME } from '$env/static/private';
import { PUBLIC_GITHUB_APP_INSTALLATION_URL, PUBLIC_GITHUB_OAUTH_CALLBACK_URL } from '$env/static/public';

export const load: LayoutServerLoad = async ({ locals, url, cookies, parent }) => {
	await parent();
	let oAuthState = cookies.get(OAUTH_STATE_COOKIE_NAME);

	if (!oAuthState) {
		oAuthState = ulid();
		cookies.set(OAUTH_STATE_COOKIE_NAME, oAuthState, { path: '/', maxAge: 60 * 60 * 24 * 3, secure: false });
	}

	const oUrls = {
		githubLoginUrl: getGithubLoginUrl(url.origin, oAuthState),
		googleLoginUrl: getGooleLoginhUrl(url.origin, oAuthState),
		githubAppInstallUrl: getGithubAppInstallationUrl(url.origin)
	};
	// console.warn(`oUrls`, JSON.stringify(oUrls, null, 2));
	return oUrls;
};
