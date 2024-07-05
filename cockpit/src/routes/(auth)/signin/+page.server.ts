import { redirect } from "@sveltejs/kit";
import type { PageServerLoad } from "./$types";
import { BackendEndpoints } from "@/backend-endpoints";

export const load: PageServerLoad = async ({ locals }) => {
    // request server authorized oauth providers and emails associated
    // check here for that


    if (locals.user) {
        return redirect(308, '/dashboard');
    }

    // get providers 
    const serverProviders = await fetch(BackendEndpoints.PROVIDERS, {
        headers: {
            'Content-Type': 'application/json',
        }
    }).then(res => res.json()).catch(() => {
        return {
            success: false,
        };
    });
    console.log(serverProviders);
    if (serverProviders.success) {
        return {
            oProviders: serverProviders.providers
        };
    }
};