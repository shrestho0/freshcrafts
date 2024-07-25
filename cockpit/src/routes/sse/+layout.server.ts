import { SSE_AUTHORIZATION_TOKEN } from "$env/static/private";
import { error, fail } from "@sveltejs/kit";
import type { LayoutServerLoad } from "./$types";

export const load: LayoutServerLoad = async ({ request, locals }) => {
    console.log("Entered SSE layout.server.ts", request.method)
    if (locals.user) { return }

    // FIXME: This is redundent
    try {
        const bearer = request.headers.get("authorization")?.split(" ")[1]
        if (bearer && bearer === SSE_AUTHORIZATION_TOKEN) {
            return
        }
    } catch (_) { }


    return error(401, "Unauthorized")

}

// check if /sse
// if (event.url.pathname.startsWith("/sse")) {
//     // check if request is form localhost:10001
//     // get bearer token

//     try {

//         const bearerToken = event.request.headers.get("authorization")?.split(" ")[1]
//         if (bearerToken && bearerToken === SSE_AUTHORIZATION_TOKEN) {
//             return await resolve(event);
//         } else {
//             throw new Error("Unauthorized")

//         }
//     } catch (_) { }

//     return json({ message: "Unauthorized" }, { status: 401 })
// }