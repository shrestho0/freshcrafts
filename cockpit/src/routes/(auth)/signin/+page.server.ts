import { fail, redirect, type Actions } from "@sveltejs/kit";
import type { PageServerLoad } from "./$types";
import { EngineConnection } from "@/server/EngineConnection";
import { AUTH_COOKIE_NAME, AUTH_COOKIE_EXPIRES_IN } from "$env/static/private";
import { AuthProviderType } from "@/types/enums";
export const load: PageServerLoad = async ({ locals }) => {
    // request server authorized oauth providers and emails associated
    // check here for that


    if (locals.user) {
        return redirect(308, '/dashboard');
    }

    // get providers 

    const contextData = await EngineConnection.getInstance().getProviders();

    // console.log("[DEBUG]: From Server Login ", contextData);

    return contextData;

};

export const actions: Actions = {
    default: async ({ request, cookies, url }) => {

        const data = Object.fromEntries(await request.formData());

        // generate token from engine
        const res = await EngineConnection.getInstance().generateToken(
            AuthProviderType.EMAIL_PASSWORD,
            {
                email: data['x-email'].toString(),
                password: data['x-password'].toString()
            }
        )

        if (res.success == true) {
            // set tokens in cookie
            cookies.set(AUTH_COOKIE_NAME, JSON.stringify(res.tokens), {
                path: '/',
                maxAge: parseInt(AUTH_COOKIE_EXPIRES_IN)
            });

            // goto
            let redirect_url = url.searchParams.get("redirect");
            if (!redirect_url) {
                redirect_url = "dashboard"
            }

            return redirect(307, `/${redirect_url}`);
        } else {
            return fail(res.message);
        }


        // return res;
    }
};