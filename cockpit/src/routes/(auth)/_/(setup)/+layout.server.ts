import { BackendEndpoints } from "@/backend-endpoints";
import type { LayoutServerLoad } from "./$types";
import { redirect } from "@sveltejs/kit";
export const load: LayoutServerLoad = async ({ locals, cookies }) => {
    // check if already setup
    // if not, okay,
    // if yes, add setup complete to cookie
    // // and redirect to dashboard 

    const res = await fetch(BackendEndpoints.SETUP_SYSCONFIG, {
        headers: {
            'Content-Type': 'application/json',
        },
    }).then(res => res.json()).catch(err => {
        return null
    });

    console.log(res)


    if (res && res.systemUserSetupComplete === false) {
        // okay but not setup, allow to setup

    } else {
        return redirect(307, "/dashboard")
    }



    // if (res && res.systemSetupComplete) {
    //     console.log("setup complete")
    //     cookies.set("setup_message", "true", {
    //         path: "/",
    //         maxAge: 60 * 60 * 1, // 1 hour
    //     })
    // } else if (res && !res.systemSetupComplete) {
    //     // okay
    // }
};