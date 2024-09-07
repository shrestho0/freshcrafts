import { EngineConnection } from "@/server/EngineConnection";
import type { PageServerLoad } from "./$types";
import { error, redirect } from "@sveltejs/kit";
import { ProjectStatus } from "@/types/enums";
import type { EngineCommonResponseDto } from "@/types/dtos";
import type { Project, ProjectDeployment } from "@/types/entities";

export const load: PageServerLoad = async ({ locals, params }) => {
    const { proj_id } = params;

    const proj = await EngineConnection.getInstance().getProject<EngineCommonResponseDto<Project, null, ProjectDeployment | null, ProjectDeployment | null>>(proj_id)

    console.log('proj', Object.keys(proj))

    if (proj.success == false) {
        return error(404, 'Project not found')
    }

    if (proj.payload.status === ProjectStatus.AWAIT_INITIAL_SETUP) {
        // ok
        return redirect(307, `/projects/${proj_id}/initial-setup`)
    } else {
        // redirect to the project page
        return { ...proj, }
    }



};