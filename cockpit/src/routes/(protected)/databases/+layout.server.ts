import { EngineConnection } from "@/server/EngineConnection";
import type { LayoutServerLoad } from "./$types";

export const load: LayoutServerLoad = async ({ locals }) => {
    const res = await EngineConnection.getInstance().getServiceStatus()


    if (res.payload && res.payload?.length == 2) {
        // ase and thikthak
        return {
            dependenciesStatus: res.payload[0],
            systemServiceStatus: res.payload[1]
        }
    }

};