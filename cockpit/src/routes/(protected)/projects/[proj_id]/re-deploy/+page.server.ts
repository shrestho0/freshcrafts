import { EngineConnection } from "@/server/EngineConnection";
import type { Actions, PageServerLoad } from "./$types";
import { ProjectStatus } from "@/types/enums";
import { error, redirect } from "@sveltejs/kit";
import type { EngineCommonResponseDto } from "@/types/dtos";
import type { Project, ProjectDeployment } from "@/types/entities";
import { FilesHelper } from "@/server/FilesHelper";


export const load: PageServerLoad = async ({ locals, parent, params }) => {
    await parent(); // previous stuff should be loaded first


    const { proj_id } = params;
    const proj = await EngineConnection.getInstance().getProject<EngineCommonResponseDto<Project, null, ProjectDeployment>>(proj_id)

    console.warn('proj', proj)

    // check if env file content available
    console.log('projXXXXXXXXXXXXXXXXXXXXx', proj.payload3?.envFile?.absPath)




    if (proj.success == false) {
        return error(404, 'Project not found')
    }

    if (proj.payload.status === ProjectStatus.INACTIVE || proj.payload.status === ProjectStatus.ACTIVE) {
        const envFile = proj.payload3?.envFile?.absPath
        let envFileContent = '';
        try {
            envFileContent = await FilesHelper.getInstance().readFile(envFile);
        } catch (e: any) { }

        // ok
        return { project: proj.payload, deployment: proj.payload3, envFileContent }
    } else {
        // redirect to the project page
        return redirect(307, `/projects/${proj_id}`)
    }

};

