import { EngineConnection } from "@/server/EngineConnection";
import type { EnginePaginatedDto } from "@/types/dtos";
import type { AIChatHistory, Project } from "@/types/entities";
import { json, type RequestHandler } from "@sveltejs/kit";

export const GET: RequestHandler = async ({ url, }) => {

    const page = parseInt(url.searchParams.get('page')!);
    const pageSize = parseInt(url.searchParams.get('pageSize')!);
    const sort = (url.searchParams.get('sort') ?? 'DESC') as 'ASC' | 'DESC';

    const d: EnginePaginatedDto<Project> = await EngineConnection.getInstance().getProjectsPaginated({
        page, pageSize, sort,
    })

    console.log(d);
    return json(d)
};

export const DELETE = async ({ request }) => {
    const { id } = await request.json() as { id: string };
    const d = await EngineConnection.getInstance().deleteAIChatHistory(id);

    return json(d)
}