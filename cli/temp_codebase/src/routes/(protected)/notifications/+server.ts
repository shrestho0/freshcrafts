

import { EngineConnection } from '@/server/EngineConnection';
import type { CommonPagination, EnginePaginatedDto } from '@/types/dtos';
import type { SystemwideNotification } from '@/types/entities.js';
import { json, type RequestHandler } from '@sveltejs/kit';

export const GET: RequestHandler = async ({ url, }) => {

	const page = parseInt(url.searchParams.get('page')!);
	const pageSize = parseInt(url.searchParams.get('pageSize')!);
	const sort = (url.searchParams.get('sort') ?? 'DESC') as 'ASC' | 'DESC';

	const d: EnginePaginatedDto<SystemwideNotification> = await EngineConnection.getInstance().getPaginatedNotifications({
		page, pageSize, sort, orderBy: 'id'
	})
	console.log(d);
	return json(d)
};

export const PATCH: RequestHandler = async ({ request }) => {
	// partial update
	return json({ message: 'PATCH' });
};

export const DELETE = async ({ request }) => {
	const { id } = await request.json() as { id: string };
	const d = await EngineConnection.getInstance().deleteNofitication(id);

	return json(d)
}