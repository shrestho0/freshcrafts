import { fail, redirect, type Actions } from "@sveltejs/kit";
import type { PageServerLoad } from "./$types";
import { BackendEndpoints } from "@/backend-endpoints";
import { AUTH_COOKIE_EXPIRES_IN, AUTH_COOKIE_NAME } from "$env/static/private";
import { EngineConnection } from "@/server/EngineConnection";
import { AuthProviderType } from "@/types/enums";

export const load: PageServerLoad = async ({ locals }) => {
    // request server authorized oauth providers and emails associated
    // check here for that


    if (locals.user) {
        return redirect(308, '/dashboard');
    }

    // get providers 
    // const serverProviders = await fetch(BackendEndpoints.PROVIDERS, {
    //     headers: {
    //         'Content-Type': 'application/json',
    //     }
    // }).then(res => res.json()).catch(() => {
    //     return {
    //         success: false,
    //         message: messages.RESPONSE_ERROR
    //     };
    // });

    // if (serverProviders.data) {
    //     serverProviders.providers = serverProviders.data as {
    //         providers: AuthProviderType[],
    //         message: string
    //     }
    //     delete serverProviders.data;
    // }

    const engine = EngineConnection.getInstance()
    const contextData = await engine.getProviders()

    // console.log("From Server Login ", contextData);

    return contextData;

};

export const actions: Actions = {
    default: async ({ request, cookies }) => {
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

        } else {
            return fail(res.message);
        }


        return res;
    }
};