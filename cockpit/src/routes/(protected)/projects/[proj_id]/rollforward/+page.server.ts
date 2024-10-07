import { EngineConnection } from "@/server/EngineConnection";
import type { PageServerLoad } from "./$types";
import { FilesHelper } from "@/server/FilesHelper";
import { ProjectType } from "@/types/enums";
import { GithubWebhookHelper } from "@/server/GithubWebhookHelper";


export const load: PageServerLoad = async ({ parent }) => {
    const pd = await parent()

    // if (pd?.project?.rollforwardDeploymentId) {
    //     // const currentDeployment = await EngineConnection.getInstance().get
    //     return {

    //     }
    // }



    const returnObj = {

    } as {
        githubBranchInfoCurrent?: {
            sha: string;
            date: string;
            message: string;
        },
        envFileContent?: string
    }

    if (pd.project?.type === ProjectType.GITHUB_REPO) {

        if (!pd.sysConf?.systemUserOauthGithubData?.user_access_token) throw new Error('No Github Access Token Found')

        if (!pd.project?.githubRepo?.owner_login) throw new Error('No Github Owner Login Found')
        if (!pd.project?.githubRepo?.name) throw new Error('No Github Owner Login Found')
        if (!pd.project?.githubRepo?.defaultBranch) throw new Error('No Github Owner Login Found')

        returnObj.githubBranchInfoCurrent = await GithubWebhookHelper.getInstance().getLastCommitShortInfo(
            pd.sysConf?.systemUserOauthGithubData?.user_access_token,
            pd.project?.githubRepo?.owner_login,
            pd.project?.githubRepo?.name,
            pd.project?.githubRepo?.defaultBranch
        )
    }

    const envFileLoc = pd?.activeDeployment?.envFile?.absPath;

    if (envFileLoc) {
        returnObj.envFileContent = await FilesHelper.getInstance().readFile(envFileLoc);
    }

    return returnObj;

};