import { ENV, PRIMARY_DOMAIN } from "$env/static/private";
import { EngineConnection } from "@/server/EngineConnection";
import { FilesHelper } from "@/server/FilesHelper";
import type { EngineCommonResponseDto } from "@/types/dtos";
import type { Project, ProjectDeployment, ProjectDeploymentSource } from "@/types/entities";
import { ProjectDeploymentStatus, InternalProjectSetupCommand } from "@/types/enums";
import type { InternalDeployProjectData } from "@/types/internal";
import messages from "@/utils/messages";
import { error, json, type RequestHandler } from "@sveltejs/kit";

export const GET: RequestHandler = async ({ request, cookies }) => {
    return error(404, 'Not found');
}

export const PATCH: RequestHandler = async ({ request, cookies }) => {
    try {
        const command = request.headers.get('x-freshcrafts-project-setup-command') as InternalProjectSetupCommand
        const data = await request.json();
        const filesHelper = FilesHelper.getInstance();

        switch (command) {
            case InternalProjectSetupCommand.CHECK_UNIQUE_NAME:
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

            case InternalProjectSetupCommand.LIST_SOURCE_FILES:
                const srcX: ProjectDeploymentSource = data.src;
                console.log('src', srcX)
                // filesDirAbsPath
                const fileTreeDetailed = await filesHelper.listFilesRecursive(srcX.filesDirAbsPath);
                // console.log('fileList', fileTreeDetailed)
                return json({ success: true, files: fileTreeDetailed.structure, total_levels: fileTreeDetailed.total_levels, iota: fileTreeDetailed.iota });
            case InternalProjectSetupCommand.PROJECT_SCREEENSHOT:
                const projectId = data.projectId;
                // const projectDomain = data.projectDomain;
                const projectPort = data.projectPort;

                console.log('projId', projectId, 'projDomain', projectPort)

                const screenshotPath = await filesHelper.getProjectDir(projectId).path + '/screenshot.png';
                // if (await filesHelper.exists(screenshotPath)) {

                // } else {
                // try to take screenshot and save to the path

                // return json({ success: true, path: screenshotPath });

                try {

                    // taking screenshot
                    await filesHelper.takeProjectScreenshot(projectPort, screenshotPath);
                    // }
                    console.log('screenshotPath exists', screenshotPath)
                    // return file
                    const f = await filesHelper.readFileRaw(screenshotPath);
                    // console.log('file', f)

                    return new Response(f, {
                        headers: {
                            'Content-Type': 'image/png',
                            'Content-Length': f.length.toString(),
                        },
                    })
                } catch (e) {
                    // console.error('Error in reading screenshot file', e)
                    return json({ success: false, message: 'Error in reading screenshot file' }, { status: 400 });
                }


            // if no file for /projects/:id/screenshot.png

            // create a new file with the screenshot

            // return screenshot file path

            case InternalProjectSetupCommand.DEPLOY_PROJECT:
                const d: InternalDeployProjectData = data;
                d.domain = `${d.projectName}.${PRIMARY_DOMAIN}`;
                d.ssl = ENV == "prod"
                // d.version = 1; // as initial deployment

                // console.log("dddddd", d)

                const depUpdate = await updatePartialDeployment(d.deployment.id!, d);
                console.log("Update current deployment with data", depUpdate)

                const firstDeploy = await EngineConnection.getInstance().deployProject(d.projectId, {
                    uniqueName: d.projectName,
                    domain: d.domain,
                    ssl: d.ssl
                } as Partial<Project>
                ) as EngineCommonResponseDto


                console.log('firstDeploy', firstDeploy)


                return json(firstDeploy);

            case InternalProjectSetupCommand.RE_DEPLOY_PROJECT:
                const rd: InternalDeployProjectData = data;
                rd.domain = `${rd.projectName}.${PRIMARY_DOMAIN}`;
                rd.ssl = ENV == "prod"

                const rDepUpdate = await updatePartialDeployment(rd.deployment.id!, rd);

                console.log("Re-deploy project with data", rDepUpdate)
                const reDeploy = await EngineConnection.getInstance().reDeployProject(rd.projectId, {
                    uniqueName: rd.projectName,
                    domain: rd.domain,
                    ssl: rd.ssl
                } as Partial<Project>
                ) as EngineCommonResponseDto


                console.log("Re-deploy project with data", reDeploy)
                return json(reDeploy)

            case InternalProjectSetupCommand.DELETE_PROJECT:
                return json(await EngineConnection.getInstance().deleteProject(data.projectId));
            case InternalProjectSetupCommand.DELETE_INCOMPLETE_PROJECT:
                return json(await EngineConnection.getInstance().deleteIncompleteProject(data.projectId));
            case InternalProjectSetupCommand.UPDATE_DEPLOYMENT:
                // nothing to change in project itself from here
                // clone activeDeployment to new currentDeployment
                // update currentDeployment with new data
                // request update


                const u = data as InternalDeployProjectData;
                const processedFilesInfo = await generateUpdatedDepData(u);

                u.domain = `${u.projectName}.${PRIMARY_DOMAIN}`;
                u.ssl = ENV == "prod"

                let newCurrentDep = u.deployment;

                newCurrentDep.status = ProjectDeploymentStatus.READY_FOR_DEPLOYMENT;
                // newCurrentDep.id = undefined;
                newCurrentDep.iteration = 1;
                newCurrentDep.version = u.deployment.version;
                newCurrentDep = {
                    ...newCurrentDep,
                    src: processedFilesInfo.src,
                    envFile: processedFilesInfo.envFile,
                    depCommands: processedFilesInfo.depCommands,
                    prodFiles: processedFilesInfo.prodFiles
                }

                // console.log('dep to update', u)
                console.log("newCurrentDep", newCurrentDep)

                // create deployment

                const updated = await EngineConnection.getInstance().updateProject(data.projectId, newCurrentDep)

                console.log('update request', updated)
                return json(updated)
            case InternalProjectSetupCommand.ROLLFORWARD:
                // 1. Create a new deployment
                const rf = data as InternalDeployProjectData;
                const dep = rf.deployment;
                rf.version = dep.version

                rf.domain = `${rf.projectName}.${PRIMARY_DOMAIN}`;
                rf.ssl = ENV == "prod"

                //// this is active deployment
                // we just need the env file content from it, rest will be generated. 

                // console.warn("old active dep", dep)
                const filesStuff = await generateUpdatedDepData(rf);

                // update these, not everything
                // const xx = {
                //     // ...dep,
                //     src: filesStuff.src,
                //     envFile: filesStuff.envFile,
                //     depCommands: filesStuff.depCommands,
                //     prodFiles: filesStuff.prodFiles
                // }
                const initiated = await EngineConnection.getInstance().rollforwardProjectRequest(dep.projectId, {
                    src: filesStuff.src,
                    envFile: filesStuff.envFile,
                    depCommands: filesStuff.depCommands,
                    prodFiles: filesStuff.prodFiles
                })

                return json(initiated)


                // new current dep 

                // newDep.status = ProjectDeploymentStatus.READY_FOR_DEPLOYMENT;


                // const rfDepUpdate = await updatePartialDeployment(rf.deployment.id!, rf);
                // const rollforward = await EngineConnection.get


                // // copy current deployment version env content to new deployment content

                // console.log("Rollforward current deployment with data", rfDepUpdate)

                return json({
                    success: false,
                    message: "Rollforward not implemented yet",
                })
            // 2. send to rollforward
            case InternalProjectSetupCommand.ROLLBACK:
                const { project_id, rollback_id } = data as any;
                console.log(data)

                if (!project_id) throw new Error("Project id missing")
                if (!rollback_id) throw new Error("Rollback id missing")


                const res = await EngineConnection.getInstance().rollbackProject(project_id, {
                    rollbackId: rollback_id,
                })

                console.log('rollback', res)

                return json(res)

            // return json({
            //     success: false,
            //     message: 'Rollback not implimented yet'
            // })


            default:
                throw new Error("Invalid command provided for `x-freshcrafts-project-setup-command`: " + String(command));
        }

    } catch (e: any) {
        console.error('Error in initial setup server', e)
        return json({ success: false, message: e?.message ?? 'Something went wrong' }, { status: 400 });
    }
};

async function generateUpdatedDepData(d: InternalDeployProjectData) {
    const filesHelper = FilesHelper.getInstance();
    const src = d.deployment.src;
    // src.rootDirPath should be relative to the filesDirPath
    // src.rootDirPath = filesHelper.joinPath(d.selectedFileRelativeUrl)
    src.rootDirPath = d.selectedFileRelativeUrl
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

    const prodFiles = await filesHelper.writeProjectEcoSystemFile(
        d.projectId,
        d.version,
        d.projectName,
        d.domain,
        src.buildDirAbsPath,
        envFile.absPath,);

    return { src, envFile, depCommands, prodFiles }
}

async function updatePartialDeployment(depId: string, d: InternalDeployProjectData) {
    const {
        src,
        envFile,
        depCommands,
        prodFiles
    } = await generateUpdatedDepData(d);

    return await EngineConnection.getInstance().updatePartialDeployment(depId, {
        src,
        envFile,
        depCommands,
        prodFiles,
        status: ProjectDeploymentStatus.READY_FOR_DEPLOYMENT
    } as Partial<ProjectDeployment>);
}