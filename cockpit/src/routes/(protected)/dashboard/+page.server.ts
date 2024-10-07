import { delay } from "@/utils/utils";
import type { PageServerLoad } from "./$types";
import { EngineConnection } from "@/server/EngineConnection";

export const load: PageServerLoad = async ({ locals }) => {

    const dbData = await EngineConnection.getInstance().getDashboardDBsData();
    const depData = await EngineConnection.getInstance().getDashboardDeploymentsData();

    return {
        dbData: dbData,
        depData: depData,
    }

};