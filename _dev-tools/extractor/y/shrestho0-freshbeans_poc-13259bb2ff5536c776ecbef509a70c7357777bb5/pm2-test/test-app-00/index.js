import express from 'express';
import idempotency from 'express-idempotency';

const app = express();
app.use('*', idempotency.idempotency());

const host = process.env.HOST || "0.0.0.0"
const port = process.env.PORT || 6969;
const origin = process.env.ORIGIN || "";

console.warn(`[INFO]: HOST: ${host}, PORT: ${port}, ORIGIN: ${origin}`);

let req_count = 0;
app.get('/', (req, res) => {
    req_count++;
    let x = '';
    // all values of process.env
    for (let key in process.env) {
        if (!process.env.hasOwnProperty(key)) continue;
        if (key.includes("XXX")) {
            x += `${key}: ${process.env[key]}<br>`;
        }
    }
    const time_start = new Date();
    let y = 0;
    for (let i = 0; i < 1_000_000_00; i++) {
        y += i;
    }
    const time_taken = new Date() - time_start;
    const time_taken_ms = time_taken % 1000;
    console.log("[DEBUG]: Received a request",);
    console.info(`[INFO]: Y: ${y}, Time taken: ${time_taken_ms}ms, Request Count: ${req_count}, Now: ${new Date()} ${x.replace(/<br>/g, ' ')}`);

    res.send(`Y: ${y}<br> Time taken: ${time_taken_ms}ms <br> Request Count: ${req_count} <br>Now: ${new Date()} <br> ${x}`);
})

app.listen(port, host, () => {
    console.log(`Server is running on http://${host}:${port}`);
})
