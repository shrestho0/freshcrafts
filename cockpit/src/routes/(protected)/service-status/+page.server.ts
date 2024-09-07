import { EngineConnection } from "@/server/EngineConnection";
import type { PageServerLoad } from "./$types";

export const load: PageServerLoad = async ({ locals }) => {
    const res = await EngineConnection.getInstance().getServiceStatus()
    console.log(res)
    return res
};