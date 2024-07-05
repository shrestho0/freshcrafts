import { redirect } from "@sveltejs/kit";
import type { LayoutServerLoad } from "./$types";



export const load: LayoutServerLoad = async ({ locals, url }) => {
    let path = url.pathname?.split("/")?.slice(1).join("/");

    console.log("path", path);
    if (!path) {
        path = "dashboard";
    }

    if (!locals.user) {
        return redirect(307, "/signin" + (path === "/" ? "" : `?redirect=${encodeURIComponent(path)}`));
    }
};