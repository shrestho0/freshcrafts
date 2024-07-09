import { fail, redirect, type Actions } from "@sveltejs/kit";
import type { PageServerLoad } from "./$types";
import { BackendEndpoints } from "@/backend-endpoints";
import { AUTH_COOKIE_EXPIRES_IN, AUTH_COOKIE_NAME } from "$env/static/private";
import type { AuthProviderType } from "@/types/enums";
import messages from "@/utils/messages";

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
            message: messages.RESPONSE_ERROR
        };
    });
    if (serverProviders.data) {
        serverProviders.providers = serverProviders.data as {
            providers: AuthProviderType[],
            message: string
        }
        delete serverProviders.data;
    }

    console.log("From Server Login ", serverProviders);

    return serverProviders;

};

export const actions: Actions = {
    default: async ({ request, cookies }) => {
        const data = Object.fromEntries(await request.formData());

        // generate token from engine
        const res = await fetch(BackendEndpoints.GENERATE_TOKEN, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                provider: 'EMAIL_PASSWORD',
                email: data['x-email'],
                password: data['x-password']
            })
        }).then(res => res.json()).catch(() => {
            return {
                success: false,
                message: 'Failed to communicate with `engine`'
            };
        });

        if (res.success == true) {
            // set tokens in cookie
            // cookies.set('_freshCraftsTokens', JSON.stringify(res.data));
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