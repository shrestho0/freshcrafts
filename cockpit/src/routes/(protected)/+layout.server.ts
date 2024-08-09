import { redirect } from '@sveltejs/kit';
import type { LayoutServerLoad } from './$types';

export const load: LayoutServerLoad = async ({ locals, url, cookies }) => {
	let path = url.pathname?.split('/')?.slice(1).join('/');

	// remove fromPage cookie
	cookies.set('fromPage', '', { path: '/', maxAge: 0 });

	console.log('path', path);
	if (!path) {
		path = 'dashboard';
	}

	if (!locals.user) {
		return redirect(307, '/signin' + (path === '/' ? '' : `?redirect=${encodeURIComponent(path)}`));
	} else {
		return {
			user: locals.user
		};
	}
};
