import { json } from "@sveltejs/kit"
import { writable } from "svelte/store"
import { produce } from "sveltekit-sse"


const DBSSEStore = writable({
    message: "Hello from the server",
    time: new Date().getTime(),
})


export function POST() {
    return produce(async function start({ emit }) {
        DBSSEStore.subscribe(async (data) => {
            const { error } = emit('message', `the time is ${JSON.stringify(data)}`)
        })
    })
}

export async function PATCH({ request }) {
    const data = await request.json()
    console.log(data)
    if (data?.message) {
        console.log("updating some sse store from +server.ts Post")
        // SomeSSEStore.set(data)
        DBSSEStore.set({
            message: data?.message,
            time: new Date().getTime(),
        })
    }

    return json({
        whatever: "something"
    })
}