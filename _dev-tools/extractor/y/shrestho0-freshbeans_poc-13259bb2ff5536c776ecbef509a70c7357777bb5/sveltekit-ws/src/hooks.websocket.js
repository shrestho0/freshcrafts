
/** @type {import('@ubermanu/sveltekit-websocket').Handle} */
export function handle({ socket }) {
    socket.on('message', (/** @type {string} */ msg) => {
        socket.send("Client Sent `" + msg + "`")
    })
}