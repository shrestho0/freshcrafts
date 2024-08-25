import { json, type RequestHandler } from '@sveltejs/kit';
import { EngineConnection } from "@/server/EngineConnection";
import { ProjectSetupCommand } from '@/types/enums';

export const PATCH: RequestHandler = async ({ request }) => {

    const { command, data } = await request.json() as {
        command: ProjectSetupCommand,
        data: any
    }
    switch (command) {
        case ProjectSetupCommand.CHECK_UNIQUE_NAME:

            const result = await EngineConnection.getInstance().getProjectByUniqueName(data);
            result.success = !result.success
            return json(result, { status: result.statusCode ?? 400 });
        case ProjectSetupCommand.DECOMPRESS_SOURCE_FILE:
        // const 
        default:
            break;
    }
    return new Response();
};
