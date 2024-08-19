import {
	GITHUB_CLIENT_ID,
	GOOGLE_CLIENT_ID,
	GOOGLE_CLIENT_SECRET,
} from '$env/static/private';

import { PUBLIC_GITHUB_OAUTH_CALLBACK_URL, PUBLIC_GOOGLE_OAUTH_CALLBACK_URL } from '$env/static/public';

import { google } from 'googleapis';

/*
ALL THESE SHOULD BE UNDER server/utils */

export const getGithubLoginUrl = (baseUrl: string, state: string) => {
	// if (!browser) return;

	let loginUrl = 'https://github.com/login/oauth/authorize';
	let github_login_url = new URL(loginUrl);
	github_login_url.searchParams.append('client_id', GITHUB_CLIENT_ID);
	github_login_url.searchParams.append('redirect_uri', baseUrl + PUBLIC_GITHUB_OAUTH_CALLBACK_URL);

	// Setup scope
	github_login_url.searchParams.append('scope', 'user:email repo workflow');

	// Setup state and check from where necessary
	github_login_url.searchParams.append('state', state);

	// return github_login_url;
	return github_login_url.toString();
};

export function getGooleLoginhUrl(baseUrl: string, state: string) {
	// // console.log("GOogle Auth Url", googleAuthUrl.href)
	const oauth2Client = new google.auth.OAuth2(
		GOOGLE_CLIENT_ID,
		GOOGLE_CLIENT_SECRET,
		baseUrl + PUBLIC_GOOGLE_OAUTH_CALLBACK_URL
	);

	const scopes = [
		'https://www.googleapis.com/auth/userinfo.email',
		'https://www.googleapis.com/auth/userinfo.profile'
	];
	const authorizationUrl = oauth2Client.generateAuthUrl({
		// 'online' (default) or 'offline' (gets refresh_token)
		access_type: 'offline',
		prompt: 'consent',
		scope: scopes,
		include_granted_scopes: true,
		state: state
	});
	return authorizationUrl.toString();
}
