import { EngineConnection } from "@/server/EngineConnection";
import type { LayoutServerLoad } from "./$types";
import { error } from "@sveltejs/kit";

export const load: LayoutServerLoad = async ({ locals, cookies, url }) => {
    if (!url.pathname.startsWith('/_') || !url.pathname.startsWith('/settings')) {
        cookies.delete('fromPage', { path: '/' });
    }



    // cookies.delete('error_fallback', { path: '/' });// we are already handling it manually

    console.log('root layout.server.ts',)
};