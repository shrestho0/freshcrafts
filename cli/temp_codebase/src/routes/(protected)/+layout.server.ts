import { error, redirect } from '@sveltejs/kit';
import type { LayoutServerLoad } from './$types';
import { EngineConnection } from '@/server/EngineConnection';

export const load: LayoutServerLoad = async ({ locals, url, cookies, parent }) => {
	await parent();
	let path = url.pathname?.split('/')?.slice(1).join('/');

	// REFACTOR remove fromPage cookie
	// ping engine, if not okay, boom
	// an extra request, doesn't matter much
	// as, it keeps stuff safe when engine is not running
	const engineOk = await EngineConnection.getInstance().ping()
	if (!engineOk.success) {
		// not ok
		return error(503, "Engine connection is not available")
	}

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
