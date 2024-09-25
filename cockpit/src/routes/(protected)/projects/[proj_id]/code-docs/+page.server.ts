import { EngineConnection } from "@/server/EngineConnection";
import type { Actions, PageServerLoad } from "./$types";
import type { AICodeDoc } from "@/types/entities";

export const load: PageServerLoad = async ({ locals, parent, params }) => {
    const d = await parent()

    const { proj_id } = params

    const codeDocs = await EngineConnection.getInstance().getCodeDocForProject(proj_id)

    // console.warn("codeDocs", codeDocs)

    return {
        codeDocs: codeDocs
    } as unknown as {
        codeDocs: AICodeDoc[]
    }

};

export const actions: Actions = {
    deleteDoc: async ({ request }) => {
        const { id } = Object.fromEntries(await request.formData()) as { id: string }
        if (!id) throw new Error("Invalid id")
        const x = await EngineConnection.getInstance().deleteCodeDoc(id)
        return x

    }
};