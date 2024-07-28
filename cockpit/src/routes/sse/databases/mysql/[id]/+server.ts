import type { DBMysql, SystemwideNotification } from "@/types/entities";
import { json, type RequestHandler } from "@sveltejs/kit";
import { writable } from "svelte/store";
import { produce } from "sveltekit-sse";

const MySQLStore = writable<string>();


export function POST() {

    return produce(async function start({ emit }) {
        const unsubscribe = MySQLStore.subscribe(async (data) => {
            if (data) {
                const { error } = emit('message', `${data}`)

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
    try {
        const data = await request.json() as SystemwideNotification;

        console.log("[DEBUG]: PATCH Request on databases/mysql/[id] SSE\nData: ", data, "\n");


        return json({
            success: true,
            message: "Event sent"
        })
    } catch (e: any) {
        console.warn("[ERROR]: PATCH Request on databases/mysql/[id] SSE");
        return json({
            success: false,
            message: e?.message ?? "An error occurred"
        });
    }
}

export const DELETE: RequestHandler = async ({ request }) => {
    MySQLStore.set("")
    return json({
        success: true,
    })
}