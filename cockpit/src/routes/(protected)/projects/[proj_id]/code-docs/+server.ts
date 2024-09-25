import { AIHelper } from "@/server/AIHelper";
import { EngineConnection } from "@/server/EngineConnection";
import { FilesHelper } from "@/server/FilesHelper";
import { json, type RequestHandler } from "@sveltejs/kit";


function delay(ms: number) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

export const PATCH: RequestHandler = async ({ request }) => {
    try {

        const d = await request.json()
        // console.warn("d---->", d.activeDeployment)

        // TODO create a code doc entity from engine
        // thus, status and data can be viewed from there

        // generate context
        const srcDir = d?.activeDeployment?.src?.filesDirAbsPath
        if (!(srcDir)) {
            throw new Error("srcDirLoc not found")
        }

        const promptContext = await AIHelper.getInstance().generateContextForCodeDoc(srcDir)
        const tokenCount = AIHelper.getInstance().getTokenCount(promptContext)

        console.warn("tokenCount", tokenCount)

        // increase token limit in azure
        // send prompt to azure
        // get response
        // save response to db 

        const keys = await (await EngineConnection.getInstance().getChatApiKeys()).payload
        AIHelper.getInstance().setApiKey(keys.azureChatApiKey)
        AIHelper.getInstance().setApiEndpoint(keys.azureChatApiEndpoint)

        const xx = await EngineConnection.getInstance().createCodeDoc({
            projectId: d.activeDeployment.projectId,
            content: "",
            status: "REQUESTED",
        })

        try {
            const x = await AIHelper.getInstance().generateText(promptContext, false)
            console.log("x", x)
            if (x?.choices[0]?.message?.content) {
                await EngineConnection.getInstance().updateCodeDoc(xx.payload.id, {
                    projectId: d.activeDeployment.projectId,
                    prompt: promptContext,
                    content: x.choices[0].message.content,
                    status: "CREATED",
                })

            } else {
                throw new Error(x?.message || "Failed to generate code doc from ai api")
            }

            console.log("x", x)

            return json({
                success: true,
                message: "Code doc created successfully"
            })
        } catch (e: any) {

            console.error(e)

            await EngineConnection.getInstance().updateCodeDoc(xx.payload.id, {
                content: "",
                status: "FAILED",
                message: e?.message || "Failed to create code doc",

            })

            return json({
                success: false,
                message: e?.message || "Failed to create code doc"
            })
        }

    } catch (e: any) {
        return json({ success: false, message: e?.message })
    }

};
