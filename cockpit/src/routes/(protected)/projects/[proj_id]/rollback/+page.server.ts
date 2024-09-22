import { EngineConnection } from "@/server/EngineConnection";
import type { PageServerLoad } from "./$types";

export const load: PageServerLoad = async ({ parent }) => {
    const d = await parent()



    // rollbackList

    const rollbackList = await EngineConnection.getInstance().getAllDeployments(d.project?.id!)
    return {
        rollbackInfo: {
            total: rollbackList?.payload?.total ?? 0,
            rollbacks: rollbackList?.payload2?.reverse() ?? []
        }
    }



};