import { EngineConnection } from '@/server/EngineConnection';
import type { Actions, PageServerLoad } from './$types';
import { fail } from '@sveltejs/kit';

export const load: PageServerLoad = async ({ locals, params }) => {
	// find db with param

	const { db_id } = params;

	// TODO: validate before sending

	const res = await EngineConnection.getInstance().getMongoDB(db_id);
	console.log('[DEBUG]: databases/mongodb/[db_id] res', res);

	return res;
};

export const actions: Actions = {
	delete: async ({ params }) => {
		const { db_id } = params;
		const res = await EngineConnection.getInstance().deleteMongoDB(db_id);
		console.warn('[DEBUG]: databases/mongodb/[db_id] delete res', res);
		return res?.success ? res : fail(506, { res });
	},

	update: async ({ params, request }) => {
		const { db_id } = params;

		const { newDBName, newDBUser, newUserPassword } = Object.fromEntries(
			await request.formData()
		) as {
			newDBName: string;
			newDBUser: string;
			newUserPassword: string;
		};

		console.warn(
			'[DEBUG]: databases/mongodb/[db_id] update',
			JSON.stringify({ db_id, newDBName, newDBUser, newUserPassword }, null, 2)
		);

		const res = await EngineConnection.getInstance().updateMongoDB(
			db_id,
			newDBName?.trim(),
			newDBUser?.trim(),
			newUserPassword?.trim()
		);
		return res?.success ? res : fail(506, { ...res });
	},

	revert: async ({ params }) => {
		console.warn('[DEBUG]: databases/mongodb/[db_id] revert', params);
		const { db_id } = params;
		const res = await EngineConnection.getInstance().revertChangesMongo(db_id);
		console.warn('[DEBUG]: databases/mongodb/[db_id] revert res', res);
		return res?.success ? res : fail(506, { res });
	}
};
