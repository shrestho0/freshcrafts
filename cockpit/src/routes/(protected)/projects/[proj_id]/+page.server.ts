import type { Project, ProjectDeployment } from "@/types/entities";
import type { PageServerLoad } from "./$types";
import { error, redirect } from "@sveltejs/kit";
import { ProjectStatus } from "@/types/enums";

export const load: PageServerLoad = async ({ parent }) => {
    const { project, currentDeployment, activeDeployment } = await parent() as {
        project: Project,
        currentDeployment: ProjectDeployment,
        activeDeployment: ProjectDeployment,
    }


    // console.log("pp", await parent())



};