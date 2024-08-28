import { EngineConnection } from "@/server/EngineConnection";
import type { LayoutServerLoad } from "../$types";

export const load: LayoutServerLoad = async ({ locals, parent }) => {
    await parent(); // waits till parent load functions are executed
    const sysConf = await EngineConnection.getInstance().getSystemConfig();
    return {
        sysConf,
    }
};