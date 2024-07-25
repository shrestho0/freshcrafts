import { EngineConnection } from "@/server/EngineConnection";
import type { PageServerLoad } from "./$types";

export const load: PageServerLoad = async ({ locals, params }) => {
    // find db with param

    const { db_id } = params;

    // TODO: validate before sending

    const res = await EngineConnection.getInstance().getMysqlDB(db_id);
    console.log("[DEBUG]: databases/mysql/[db_id] res", res);

    return res;
};