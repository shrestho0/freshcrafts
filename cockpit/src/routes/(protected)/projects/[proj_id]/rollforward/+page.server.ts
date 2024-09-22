import { EngineConnection } from "@/server/EngineConnection";
import type { PageServerLoad } from "./$types";
import { FilesHelper } from "@/server/FilesHelper";


export const load: PageServerLoad = async ({ parent }) => {
    const pd = await parent()

    // if (pd?.project?.rollforwardDeploymentId) {
    //     // const currentDeployment = await EngineConnection.getInstance().get
    //     return {

    //     }
    // }

    const envFileLoc = pd?.activeDeployment?.envFile?.absPath;
    if (envFileLoc) {
        const envFileContent = await FilesHelper.getInstance().readFile(envFileLoc);
        return {
            envFileContent,
        }

    }

};