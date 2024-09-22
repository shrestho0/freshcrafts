import type { Project, ProjectDeployment } from "@/types/entities";
import type { PageServerLoad } from "./$types";
import { error } from "@sveltejs/kit";
import { ProjectStatus } from "@/types/enums";

export const load: PageServerLoad = async ({ parent }) => {
    const { project, currentDeployment } = await parent() as {
        project: Project,
        currentDeployment: ProjectDeployment
    }



    // not an error that should be handled gracefully
    if (!project || !currentDeployment) return error(403, "project and currentDeployment must be defined ")

};