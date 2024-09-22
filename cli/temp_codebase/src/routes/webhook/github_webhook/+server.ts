
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
        let parsed_payload = JSON.parse(payload);
        const d = {
            timestamp: Date.now(),
            headers: {
                "x-hub-signature-256": signature,
                "content-type": request.headers.get("content-type"),
                "user-agent": request.headers.get("user-agent"),
                "X-GitHub-Hook-ID": request.headers.get("X-GitHub-Hook-ID"),
                "X-GitHub-Delivery": request.headers.get("X-GitHub-Delivery"),
                "X-GitHub-Event": request.headers.get("X-GitHub-Event"),
                "X-GitHub-Hook-Installation-Target-Type": request.headers.get("X-GitHub-Hook-Installation-Target-Type"),
                "X-GitHub-Hook-Installation-Target-ID": request.headers.get("X-GitHub-Hook-Installation-Target-ID"),
            },
            clientAddress: getClientAddress(),
            payload: parsed_payload
        }
        temp_data_arr.push(d);
    } catch (e) {
        console.error(e);
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

