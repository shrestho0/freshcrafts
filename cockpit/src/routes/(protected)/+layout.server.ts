import { redirect } from '@sveltejs/kit';
import type { LayoutServerLoad } from './$types';

export const load: LayoutServerLoad = async ({ locals, url, cookies, parent }) => {
	await parent();
	let path = url.pathname?.split('/')?.slice(1).join('/');

	// REFACTOR remove fromPage cookie


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
