import type { EngineMySQLGetOnePayload } from "@/types/dtos";
import { json, type RequestHandler } from "@sveltejs/kit";
import { writable } from "svelte/store";
import { produce } from "sveltekit-sse";

const MySQLStore = writable<EngineMySQLGetOnePayload | undefined>();


export function POST() {

    return produce(async function start({ emit }) {
        const unsubscribe = MySQLStore.subscribe(async (data) => {
            if (data) {
                const { error } = emit('message', `${JSON.stringify(data)}`)

                if (error) {
                    return unsubscribe();
                }
            }
            // await delay(1000)
        })
    })
}

export const PUT: RequestHandler = async ({ request }) => {
    const data = await request.json();
    console.log("[DEBUG]: PUT Request on Notification SSE\nData: ", data, "\n");

    return json({
        success: true,
        message: "NOT IMPLEMENTED"
    });
}

export const PATCH: RequestHandler = async ({ request }) => {
    const data = await request.json() as EngineMySQLGetOnePayload

    // Validate Stuff    

    try {
        MySQLStore.set(data)
        // MySQLStore.getStore().set(JSON.parse(message))
        return json({ success: true, })
    } catch (err: any) {
        return json({
            success: false,
            message: err?.message ?? "Some error occured"
        })
    }
}

export const DELETE: RequestHandler = async ({ request }) => {
    MySQLStore.set(undefined)
    return json({
        success: true,
    })
}