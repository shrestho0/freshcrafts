import { EngineConnection } from '@/server/EngineConnection';
import type { CommonPagination } from '@/types/dtos';
import { json, type RequestHandler } from '@sveltejs/kit';

export const POST: RequestHandler = async ({ request }) => {
	const paginationORquery = await request.json();
	const pagination: CommonPagination = {
		page: paginationORquery.page,
		pageSize: paginationORquery.pageSize,
		orderBy: paginationORquery.orderBy,
		sort: paginationORquery.sort
	};

	console.log();

	const query = paginationORquery.query;
	if (query) {
		return json(await EngineConnection.getInstance().searchPostgresDBs(query));
	}

	return json(await EngineConnection.getInstance().getPostgresDBs(pagination));
};
