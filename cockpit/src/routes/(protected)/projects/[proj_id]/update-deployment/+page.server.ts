import type { Project, ProjectDeployment } from "@/types/entities";
import type { PageServerLoad } from "./$types";
import { error } from "@sveltejs/kit";
import { ProjectStatus } from "@/types/enums";
import { FilesHelper } from "@/server/FilesHelper";

export const load: PageServerLoad = async ({ parent, cookies }) => {
    const { project, activeDeployment } = await parent() as {
        project: Project,
        activeDeployment: ProjectDeployment
    }

    // if (project.status != ProjectStatus.ACTIVE) {
    //     // cookies.set
    //     return error(403, "Project must be inactive to re-de deploy")
    // }

    // send envFileContent along
    const envFileLoc = activeDeployment?.envFile?.absPath;
    if (envFileLoc) {
        const envFileContent = await FilesHelper.getInstance().readFile(envFileLoc);
        return {
            envFileContent,
        }

    }

    // not an error that should be handled gracefully
    if (!project) return error(403, "project  must be defined ")

};