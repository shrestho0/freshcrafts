import { EngineConnection } from "@/server/EngineConnection";
import type { Actions, PageServerLoad } from "./$types";
import { error, redirect } from "@sveltejs/kit";
import type { EngineCommonResponseDto } from "@/types/dtos";
import type { DepPostInstallCommands, Project, ProjectDeployment } from "@/types/entities";
import { InternalDeploymentActions, ProjectStatus } from "@/types/enums";
import { FilesHelper } from "@/server/FilesHelper";


// const allowedActions = ['initial-setup', 're-deploy', 'modify-env', 'modify-domain']

export const load: PageServerLoad = async ({ locals, parent, params, cookies }) => {
    // await parent(); // previous stuff should be loaded first

    const { proj_id, action } = params;

    const proj = await EngineConnection.getInstance().getProject<EngineCommonResponseDto<Project, null, ProjectDeployment, ProjectDeployment>>(proj_id)

    // console.warn('proj', proj)

    if (!Object.values<string>(InternalDeploymentActions).includes(action)) {
        // this is not acceptable
        cookies.set('error_fallback', JSON.stringify({
            href: `/projects/${proj_id}`,
            title: 'Back to project'

        }), { path: '/', maxAge: 60 * 60 * 1, secure: false, httpOnly: false })
        return error(406, 'Invalid deployment option not found')
    }

    if (proj?.success == false) {
        return error(404, proj?.message ?? "Project not found")
    }

    console.log('proj', proj)
    // let envFileContent = ''

    // // FIXME: Refactor Required, this should be done according to user's choice
    // if (proj?.payload3) {
    //     envFileContent = await FilesHelper.getInstance().readFile(proj?.payload2?.envFile?.absPath)
    // } else if (proj?.payload2) {
    //     envFileContent = await FilesHelper.getInstance().readFile(proj?.payload3?.envFile?.absPath)
    // }


    // payload 1: project, payload 2: active proj dep, payload 3: current proj dep
    let envPath = undefined;
    let envFileContent = ''

    if (proj?.payload.status === ProjectStatus.ACTIVE) {
        // envFileContent = await FilesHelper.getInstance().readFile(proj?.payload2?.envFile?.absPath)
        envPath = proj?.payload2?.envFile?.absPath
    } else if (proj?.payload.status === ProjectStatus.INACTIVE) {
        envPath = proj?.payload3?.envFile?.absPath
    }

    if (envPath) {
        // envFileContent = await FilesHelper.getInstance().readFile(envPath)
        const envFileExists = await FilesHelper.getInstance().exists(envPath)
        if (envFileExists) {
            envFileContent = await FilesHelper.getInstance().readFile(envPath)
        }
        // console.log("\n")
        // console.log(envPath, await FilesHelper.getInstance().exists(envPath))
        // console.log("\n")
    }

    return { ...proj, action, envFileContent };



    // if (proj?.payload?.status === ProjectStatus.PROCESSING_SETUP) {
    // ok
    // return { project: proj?.payload, deployment: proj?.payload3 }
    // }
    //  else {
    //     // redirect to the project page
    //     return redirect(307, `/projects/${proj_id}`)
    // }

};


