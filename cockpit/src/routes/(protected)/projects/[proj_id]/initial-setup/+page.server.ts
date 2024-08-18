import { EngineConnection } from "@/server/EngineConnection";
import type { Actions, PageServerLoad } from "./$types";
import { ProjectStatus } from "@/types/enums";
import { redirect } from "@sveltejs/kit";


export const load: PageServerLoad = async ({ locals, parent, params }) => {
    await parent(); // previous stuff should be loaded first


    const { proj_id } = params;
    const proj = await EngineConnection.getInstance().getProject(proj_id)

    if (proj.payload.status === ProjectStatus.PROSESSING_SETUP) {
        // ok
        return proj
    } else {
        // redirect to the project page
        return redirect(307, `/projects/${proj_id}`)
    }

};
