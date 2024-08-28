import type { LayoutServerLoad } from "./$types";

export const load: LayoutServerLoad = async ({ locals, cookies, url }) => {
    if (!url.pathname.startsWith('/_') || !url.pathname.startsWith('/settings')) {
        cookies.delete('fromPage', { path: '/' });
    }
};