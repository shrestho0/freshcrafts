import { EngineConnection } from "@/server/EngineConnection";
import type { Actions, PageServerLoad } from "./$types";
import { ProjectStatus } from "@/types/enums";
import { error, redirect } from "@sveltejs/kit";
import type { EngineCommonResponseDto } from "@/types/dtos";
import type { Project, ProjectDeployment } from "@/types/entities";


export const load: PageServerLoad = async ({ locals, parent, params, }) => {
    // await parent(); // previous stuff should be loaded first


    const { proj_id } = params;
    const proj = await EngineConnection.getInstance().getProject<EngineCommonResponseDto<Project, null, ProjectDeployment>>(proj_id)

    // console.warn('proj', proj)

    // if (proj?.success == false) {
    //     return error(404, 'Project not found')
    // }


    return proj;



    // if (proj?.payload?.status === ProjectStatus.PROCESSING_SETUP) {
    // ok
    // return { project: proj?.payload, deployment: proj?.payload3 }
    // }
    //  else {
    //     // redirect to the project page
    //     return redirect(307, `/projects/${proj_id}`)
    // }

};


export const actions: Actions = {
    deleteIncompleteDeployment: async ({ request }) => {
        const { id } = Object.fromEntries(await request.formData())
        // delete project directory
        console.log("deleteIncompleteDeployment", id)
        ///incomplete/{id}
    },

    requestDeployment: async ({ request }) => {
        const req_data = Object.fromEntries(await request.formData())
        console.log("requestDeployment", req_data)

    }
};