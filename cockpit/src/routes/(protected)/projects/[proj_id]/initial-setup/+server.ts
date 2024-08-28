import { json, type RequestHandler } from '@sveltejs/kit';
import { EngineConnection } from "@/server/EngineConnection";
import { ProjectDeploymentStatus, ProjectSetupCommand } from '@/types/enums';
import { FilesHelper } from '@/server/FilesHelper';
import type { Project, ProjectDeployment, ProjectDeploymentSource } from '@/types/entities';
import { PRIMARY_DOMAIN } from '$env/static/private';
import type { EngineCommonResponseDto } from '@/types/dtos';

export const PATCH: RequestHandler = async ({ request }) => {

    const { command, data } = await request.json() as {
        command: ProjectSetupCommand,
        data: any
    }
    const filesHelper = FilesHelper.getInstance();
    switch (command) {
        case ProjectSetupCommand.CHECK_UNIQUE_NAME:

            //   only allowed laters, numbers, hyphens of length 5-32
            const regex = /^[a-z0-9-]{3,32}$/;

            if (!regex.test(data)) {
                return json({
                    success: false,
                    message: "Project name can only contain lowercase letters, numbers, and hyphens of length 3-32"
                }, { status: 400 });
            }

            const result = await EngineConnection.getInstance().getProjectByUniqueName(data);

            result.success = !result.success;

            let message = ''
            if (result.success) {
                message = "Project name is valid";
            } else {
                message = "Project name is already taken"
            }



            return json({
                success: result.success,
                message,
            }, { status: result.statusCode ?? 400 });
        case ProjectSetupCommand.LIST_SOURCE_FILES:
            const srcX: ProjectDeploymentSource = data;
            console.log('src', srcX)
            // filesDirAbsPath
            const fileTreeDetailed = await filesHelper.listFilesRecursive(srcX.filesDirAbsPath);
            // console.log('fileList', fileTreeDetailed)
            return json({ success: true, files: fileTreeDetailed.structure, total_levels: fileTreeDetailed.total_levels, iota: fileTreeDetailed.iota });

        // return json({ success: false, message: 'Not implemented' }, { status: 501 });

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
            } = data;

            // TODO: send only the abs path
            // create env file from env content
            const envFile = await filesHelper.writeProjectEnvFile(d.projectId, d.envFileContent);

            // whole source should be checked and saved
            const src = d.deployment.src;
            src.rootDirPath = filesHelper.joinPath(src.filesDirPath, d.selectedFileRelativeUrl)
            src.rootDirAbsPath = filesHelper.joinPath(src.filesDirAbsPath, d.selectedFileRelativeUrl)
            src.buildDirPath = filesHelper.joinPath(src.rootDirPath, d.outputDir)
            src.buildDirAbsPath = filesHelper.joinPath(src.rootDirAbsPath, d.outputDir)

            const depCommands = {
                build: d.buildCommand,
                install: d.installCommand,
            }

            const depUpdate = await EngineConnection.getInstance().updatePartialProjectDeployment(d.deployment.id, {
                src,
                envFile,
                depCommands,
                status: ProjectDeploymentStatus.READY_FOR_DEPLOYMENT
            } as Partial<ProjectDeployment>);

            // now create project

            const firstDeploy = await EngineConnection.getInstance().deployProject(d.projectId, {
                uniqueName: d.projectName,
                domain: `${d.projectName}.${PRIMARY_DOMAIN}`
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

            // TODO: create stuff
            break;
        case ProjectSetupCommand.DELETE_INCOMPLETE_PROJECT:
            const { projectId, deploymentId, rawFileAbsPath, srcFileAbsPath } = data;
            const res = await EngineConnection.getInstance().deleteIncompleteProject(projectId);
            console.log('deleteIncompleteProject', res, projectId, deploymentId, rawFileAbsPath, srcFileAbsPath)
            if (res.success) {
                // delete rawFileAbsPath, srcFileAbsPath
                try {

                    // await filesHelper.deleteFile(rawFileAbsPath);
                    await filesHelper.deleteProjectFiles(projectId);
                } catch (err) {
                    console.error('Failed to delete files', err)
                }
            }
            return json(res);
        default:
            break;
    }
    return new Response();
};
