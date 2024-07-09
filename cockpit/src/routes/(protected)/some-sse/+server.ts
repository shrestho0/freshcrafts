import SomeSSEStore from '@/stores/SomeSSEStore.server'
import { json } from '@sveltejs/kit'
import { produce } from 'sveltekit-sse'

/**
 * @param {number} milliseconds
 * @returns
 */
function delay(milliseconds: number) {
    return new Promise(function run(resolve) {
        setTimeout(resolve, milliseconds)
    })
}

export async function POST() {
    return produce(async function start({ emit }) {
        while (1) {

            try {
                SomeSSEStore.subscribe((value) => {
                    const { error } = emit('message', JSON.stringify(value))
                    if (error) {
                        throw error
                    }
                })
            } catch (e) {
                console.log("Error in SSE", e)
                console.log("Connection should be closed by now")
                return
            }
        }
        // await delay(1000)
        // while (true) {
        // const { error } = emit('message', JSON.stringify(`the time is ${Date.now()}`))
        // if (error) {
        //     return
        // }
        // }
    })
}

export async function PATCH({ request }) {
    try {
        const data = await request.json()
        SomeSSEStore.set(data)
    } catch (e) {
        console.log("Error in SSE PATCH", e)
    }

    return json({ success: true })
}