
import { GITHUB_WEBHOOK_SECRET } from "$env/static/private";
import { Webhooks } from "@octokit/webhooks";
import { error, json, type RequestHandler } from "@sveltejs/kit";

const webhooks = new Webhooks({
    // CHECKED: it only works if secret is correct 
    secret: GITHUB_WEBHOOK_SECRET,
});

let temp_data_arr: any[] = []

export const POST: RequestHandler = async ({ request, getClientAddress }) => {
    // const signature = req.headers["x-hub-signature-256"];
    const signature = request.headers.get("x-hub-signature-256") ?? "";
    const payload = await request.text();
    try {

        if (!(await webhooks.verify(payload, signature))) {
            return json({ message: "Unauthorized due to invalid signature" }, { status: 401 });
        }
    } catch (e) {
        return json({ message: "Unauthorized due to invalid signature" }, { status: 401 });
    }

    try {

        const headers = structuredClone(request.headers);
        console.log(headers);
        const d = {
            timestamp: Date.now(),
            headers,
            clientAddress: getClientAddress(),
            payload: JSON.parse(payload)
        }
        temp_data_arr.push(d);
    } catch (e) {
        return json({ message: "Error parsing payload" }, { status: 400 });
    }


    return json({ message: "bam!" });
};

export const GET: RequestHandler = async ({ request }) => {
    return json(temp_data_arr);
}

export const DELETE: RequestHandler = async ({ request }) => {
    temp_data_arr = [];
    return json({ message: "deleted" });
}

