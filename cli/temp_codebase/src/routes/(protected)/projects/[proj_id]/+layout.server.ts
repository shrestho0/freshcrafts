import { EngineConnection } from "@/server/EngineConnection";
import { error } from "@sveltejs/kit";
import type { LayoutServerLoad } from "./$types";

export const load: LayoutServerLoad = async ({ locals, params, cookies }) => {
    const { proj_id } = params;

    const proj = await EngineConnection.getInstance().getProject(proj_id)

    console.log('proj', Object.keys(proj))

    if (proj.success == false) {
        // back to all projects cookie
        cookies.set('error_fallback', JSON.stringify({
            href: '/projects/all',
            title: 'Back to projects'
        } as any), { path: '/', maxAge: 60 * 60 * 1, secure: false, httpOnly: false })

        return error(proj?.statusCode ?? 404, proj?.message ?? 'Project not found')
    }

    return proj


};