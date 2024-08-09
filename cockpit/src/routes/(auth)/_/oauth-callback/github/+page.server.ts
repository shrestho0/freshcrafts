import {
	AUTH_COOKIE_EXPIRES_IN,
	AUTH_COOKIE_NAME,
	GITHUB_CLIENT_ID,
	GITHUB_CLIENT_SECRET,
	OAUTH_STATE_COOKIE_NAME
} from '$env/static/private';
import type { OAuthCallbackInternalResponse, OAuthGithubDataRequestDto } from '@/types/internal';
import type { PageServerLoad } from './$types';
import { BackendEndpoints } from '@/backend-endpoints';
import { AuthProviderType } from '@/types/enums';
import { redirect } from '@sveltejs/kit';

export const load: PageServerLoad = async ({ locals, url, cookies, params }) => {
	const dataToSendToEngine: OAuthGithubDataRequestDto = {
		token: undefined,
		userInfo: undefined,
		dataJson: undefined,
		emails: [],
		oAuthGithubId: ''
	};
	const responseObject: OAuthCallbackInternalResponse = {
		success: false,
		provider: 'github',
		closeWindow: false,
		message: '',
		data: dataToSendToEngine,
		redirect: ''
	};

	// popup for this page only rest will be redirected
	let fromPage: 'setup' | 'link' | undefined = cookies.get('fromPage') as any;

	const savedState = cookies.get(OAUTH_STATE_COOKIE_NAME);

	// Data from github callback
	const oData = {
		code: url.searchParams.get('code'),
		state: url.searchParams.get('state')
	};

	if (!oData.code) {
		responseObject.message = 'Code not found';
		return responseObject;
	}

	if (savedState != oData.state) {
		responseObject.message = 'State mismatch';
		return responseObject;
	}

	// verify from gh

	const gh_url = new URL('https://github.com/login/oauth/access_token');
	gh_url.searchParams.set('client_id', GITHUB_CLIENT_ID);
	gh_url.searchParams.set('client_secret', GITHUB_CLIENT_SECRET);
	gh_url.searchParams.set('code', oData.code as string);

	const gh_res = await fetch(gh_url, {
		method: 'POST',
		headers: { Accept: 'application/json', 'Content-Type': 'application/json' }
	})
		.then((res) => res.json())
		.catch((err) => {
			console.log('gh_err', err);
			return null;
		});

	// console.log("gh_res", gh_res)
	if (!gh_res || !gh_res.access_token) {
		responseObject.message = 'Failed to get access token';
		return responseObject;
	}

	// get users info and save it
	const gh_user = await fetch('https://api.github.com/user', {
		headers: {
			Accept: 'application/json',
			Authorization: `Bearer ${gh_res.access_token}`
		}
	})
		.then((res) => res.json())
		.catch((err) => {
			console.log('gh_user_err', err);
			return null;
		});

	if (!gh_user) {
		responseObject.message = 'Failed to get user info';
		return responseObject;
	}

	const gh_user_emails = await fetch('https://api.github.com/user/emails', {
		headers: {
			Accept: 'application/json',
			Authorization: `Bearer ${gh_res.access_token}`
		}
	})
		.then((res) => res.json())
		.catch((err) => {
			console.log('gh_user_emails_err', err);
			return null;
		});

	// TODO: Ekhane page er hishab hobe,
	// Link or Setup page theke ashle windowClose korbe, data save korbe
	// Other jekono page theke ashle
	// Try generating token from engine /tokens/generate
	// On success, set tokens, on failure show error messages [maybe, account not authorized or something like that]
	// finally save tokens to cookie
	// and redirect
	// TODO: Google er jonno same hobe

	// TODO: Temporary data, we'll serialize later
	dataToSendToEngine.token = gh_res.access_token;
	dataToSendToEngine.userInfo = gh_user;
	dataToSendToEngine.dataJson = gh_res;
	// dataToSendToEngine.emails = gh_user_emails.map((e: any) => e.email)

	// thakbe
	dataToSendToEngine.oAuthGithubId = gh_user.id;

	if (fromPage != 'setup' && fromPage != 'link') {
		// try generating token from engine
		// we'll check with provider and consiting email
		const res = await fetch(BackendEndpoints.GENERATE_TOKEN, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({
				provider: AuthProviderType.OAUTH_GITHUB,
				// check all email with all emails, bad approach, nedda fix on refactor, proceeding for now
				// email: dataToSendToEngine.emails,
				// oAuthEmails: dataToSendToEngine.emails
				githubId: gh_user.id
			})
		})
			.then((res) => res.json())
			.catch(() => {
				return {
					success: false,
					message: 'Failed to communicate with `engine`'
				};
			});

		console.log('TOKEN GENERATION RESPONSE', res);

		if (res.success == true) {
			// set tokens in cookie
			// cookies.set('_freshCraftsTokens', JSON.stringify(res.data));
			cookies.set(AUTH_COOKIE_NAME, JSON.stringify(res.tokens), {
				path: '/',
				maxAge: parseInt(AUTH_COOKIE_EXPIRES_IN)
			});

			// redirecting to dashboard, seems all fine
			// return redirect(307, '/dashboard')
			// redirect from frontend
			responseObject.success = true;
			responseObject.redirect = '/dashboard';
			return responseObject;
		}
	}

	responseObject.success = true;
	responseObject.message = 'Success';
	responseObject.closeWindow = true;
	responseObject.data = dataToSendToEngine;
	return responseObject;
};
