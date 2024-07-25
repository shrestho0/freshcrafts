import type { SystemwideNotification } from '@/types/entities';
import { json, type RequestHandler } from '@sveltejs/kit';
import { writable } from 'svelte/store';
import { produce } from 'sveltekit-sse'

// /**
//  * @param {number} milliseconds
//  * @returns
//  */
// function delay(milliseconds: number | undefined) {
//     return new Promise(function run(resolve) {
//         setTimeout(resolve, milliseconds)
//     })
// }

const NotificationStore = writable<string>();


export function POST() {

    return produce(async function start({ emit }) {
        const unsubscribe = NotificationStore.subscribe(async (data) => {
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
        message: "Some message"
    });
}

export const PATCH: RequestHandler = async ({ request }) => {
    const data = await request.json()

    if (!data.message) return json({ success: false, message: "param `message` is required" })

    try {
        NotificationStore.set(data)
        // NotificationStore.getStore().set(JSON.parse(message))
        return json({ success: true, })
    } catch (err: any) {
        return json({
            success: false,
            message: err?.message ?? "Some error occured"
        })
    }
}

export const DELETE: RequestHandler = async ({ request }) => {
    NotificationStore.set("")
    return json({
        success: true,
    })
}