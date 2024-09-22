import { AUTH_COOKIE_NAME } from '$env/static/private';
import { BackendEndpoints } from '@/backend-endpoints';
import { json, type RequestHandler } from '@sveltejs/kit';

export const POST: RequestHandler = async ({ locals, cookies }) => {
	const token = cookies.get(AUTH_COOKIE_NAME);

	try {
		// get token
		const { refresh } = JSON.parse(cookies.get(AUTH_COOKIE_NAME) || '{}');
		// invalidate token
		if (refresh) {
			const res = await fetch(BackendEndpoints.INVALIDATE_TOKEN, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({ token: refresh })
			})
				.then((res) => res.json())
				.catch(() => { });
			console.log('Invalidating token', res);
		}

		// For all cases, we'll remove the cookie
		cookies.set(AUTH_COOKIE_NAME, '', { path: '/', maxAge: 0, secure: false });
		return json({ success: true });
	} catch (e) {
		console.error(e);
		return json({ success: false });
	}
};
