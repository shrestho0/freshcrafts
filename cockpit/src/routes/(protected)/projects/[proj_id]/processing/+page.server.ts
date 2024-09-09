import { ProjectStatus } from "@/types/enums";
import type { PageServerLoad } from "./$types";
import { redirect } from "@sveltejs/kit";


export const load: PageServerLoad = async ({ parent, params, url }) => {
    const d = await parent()


    if (d.project?.status?.toString().startsWith("PROCESSING")) {
        console.log('Processing')
    } else {
        console.log('Not Processing, redirecting...')
        redirect(307, url.pathname.replace('/processing', ''))
    }

};