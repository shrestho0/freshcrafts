import type { Handle } from '@sveltejs/kit';


type User = {
    name: string;
    email: string;
}

export const handle: Handle = async ({ event, resolve }) => {

    console.log("Entered hook.server.ts")
    const response = await resolve(event);
    // set cookies
    return response;
};