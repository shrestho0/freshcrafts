import { AIHelper } from "@/server/AIHelper";
import { FilesHelper } from "@/server/FilesHelper";
import { json, type RequestHandler } from "@sveltejs/kit";


function delay(ms: number) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

export const PATCH: RequestHandler = async ({ request }) => {
    try {

        const d = await request.json()
        console.warn("d---->", d.activeDeployment)
        await delay(2000) // simulating data coming from ai server

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



        throw new Error("not implimented")

    } catch (e: any) {
        return json({ success: false, message: e?.message })
    }

};

async function generateContext(srcDirLoc: string) {



}