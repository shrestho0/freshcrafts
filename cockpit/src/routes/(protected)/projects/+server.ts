import { PRIMARY_DOMAIN } from "$env/static/private";
import { EngineConnection } from "@/server/EngineConnection";
import { FilesHelper } from "@/server/FilesHelper";
import type { EngineCommonResponseDto } from "@/types/dtos";
import type { Project, ProjectDeployment, ProjectDeploymentSource } from "@/types/entities";
import { ProjectDeploymentStatus, ProjectSetupCommand } from "@/types/enums";
import messages from "@/utils/messages";
import { error, json, type RequestHandler } from "@sveltejs/kit";

export const GET: RequestHandler = async ({ request, cookies }) => {
    return error(404, 'Not found');
}

export const PATCH: RequestHandler = async ({ request, cookies }) => {
    try {
        const command = request.headers.get('x-freshcrafts-project-setup-command') as unknown as ProjectSetupCommand
        const data = await request.json();
        const filesHelper = FilesHelper.getInstance();

        switch (command) {
            case ProjectSetupCommand.CHECK_UNIQUE_NAME:
                const projName = data.projName;
                //   only allowed laters, numbers, hyphens of length 5-32
                const regex = /^[a-z0-9-]{3,32}$/;
                if (!regex.test(projName)) {
                    return json({ message: messages.PROJECT_NAME_INVALID, success: false, });
                }
                console.log('data', data)

                const result = await EngineConnection.getInstance().getProjectByUniqueName(projName);

                result.success = !result.success;

                let message = result.success ? "Project name is valid" : "Project name is already taken"

                return json({ success: result.success, message, }, { status: result.statusCode });

            case ProjectSetupCommand.LIST_SOURCE_FILES:
                const srcX: ProjectDeploymentSource = data.src;
                console.log('src', srcX)
                // filesDirAbsPath
                const fileTreeDetailed = await filesHelper.listFilesRecursive(srcX.filesDirAbsPath);
                // console.log('fileList', fileTreeDetailed)
                return json({ success: true, files: fileTreeDetailed.structure, total_levels: fileTreeDetailed.total_levels, iota: fileTreeDetailed.iota });

            case ProjectSetupCommand.DEPLOY_PROJECT:
                const d: {
                    projectName: string,
                    buildCommand: string,
                    installCommand: string,
                    outputDir: string,
                    selectedFileRelativeUrl: string,
                    envFileContent: string,
                    deployment: ProjectDeployment,
                    projectId: string,
                    postInstall: string,
                } = data;

                console.log("dddddd", d)

                // TODO: send only the abs path
                // create env file from env content
                // const envFile = await filesHelper.writeProjectEnvFile(d.projectId, d.envFileContent);

                // Env file should be projectRoot/.env

                // whole source should be checked and saved
                const src = d.deployment.src;
                // src.rootDirPath should be relative to the filesDirPath
                src.rootDirPath = filesHelper.joinPath(d.selectedFileRelativeUrl)
                src.rootDirAbsPath = filesHelper.joinPath(src.filesDirAbsPath, d.selectedFileRelativeUrl)

                const envFile = await filesHelper.writeProjectEnvFile(src.filesDirPath, src.rootDirPath, d.envFileContent);

                // src.buildDirPath should be relative to the rootDirPath
                src.buildDirPath = filesHelper.joinPath(d.outputDir)
                src.buildDirAbsPath = filesHelper.joinPath(src.rootDirAbsPath, d.outputDir)

                const depCommands = {
                    build: d.buildCommand,
                    install: d.installCommand,
                    postInstall: d.postInstall,
                }

                const domain = `${d.projectName}.${PRIMARY_DOMAIN}`;
                const prodFiles = await filesHelper.writeProjectEcoSystemFile(
                    d.projectId,
                    1, // as initial deployment
                    d.projectName,
                    domain,
                    src.buildDirAbsPath,
                    envFile.absPath,);

                const depUpdate = await EngineConnection.getInstance().updatePartialProjectDeployment(d.deployment.id, {
                    src,
                    envFile,
                    depCommands,
                    prodFiles,
                    status: ProjectDeploymentStatus.READY_FOR_DEPLOYMENT
                } as Partial<ProjectDeployment>);

                // console.log('depUpdate', depUpdate)

                // now create project

                // console.log('d', d)

                const firstDeploy = await EngineConnection.getInstance().deployProject(d.projectId, {
                    uniqueName: d.projectName,
                    domain: domain,
                } as Partial<Project>
                ) as EngineCommonResponseDto


                console.log('firstDeploy', firstDeploy)


                // console.warn(JSON.stringify({
                //     src,
                //     envFile,
                //     depCommands,
                // }, null, 2))
                // // console.warn('newSource', src)

                // // partial update project deployment
                // // upon result, set 
                // console.log('depUpdate', depUpdate)
                // return json(depUpdate);

                return json(firstDeploy);
            // case ProjectSetupCommand.RE_DEPLOY_PROJECT:
            //     console.log("Re-deploy project with data", data)
            //     break
            case ProjectSetupCommand.DELETE_PROJECT:
                console.log("Delete project with data", data)
                const res = await EngineConnection.getInstance().deleteProject(data.projectId);
                // if(res.success){
                //     // set will 
                // }
                return json(res);
            default:
                throw new Error("Invalid command provided for `x-freshcrafts-project-setup-command`: " + command.toString())
        }

    } catch (e: any) {
        console.error('Error in initial setup server', e)
        return json({ success: false, message: e?.message ?? 'Something went wrong' }, { status: 400 });
    }
};