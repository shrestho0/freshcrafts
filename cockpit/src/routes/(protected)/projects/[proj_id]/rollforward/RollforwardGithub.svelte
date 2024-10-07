<script lang="ts">
    import { browser } from "$app/environment";
    import PreDebug from "@/components/dev/PreDebug.svelte";
    import DepInfo from "@/components/project-specifics/DepInfo.svelte";
    import ProjectSpecificWarningBox from "@/components/project-specifics/ProjectSpecificWarningBox.svelte";
    import type { EngineSystemConfigResponseDto } from "@/types/dtos";
    import type { Project, ProjectDeployment } from "@/types/entities";
    import {
        InternalDeploymentActions,
        InternalNewProjectType,
    } from "@/types/enums";
    import CarbonArrowLeft from "@/ui/icons/CarbonArrowLeft.svelte";
    import CarbonArrowRight from "@/ui/icons/CarbonArrowRight.svelte";
    import { ClickableTile, Tile } from "carbon-components-svelte";
    import DeploymentProcessingHistory from "../DeploymentProcessingHistory.svelte";
    import ProjectFileSelection from "@/components/project-specifics/ProjectFileSelection.svelte";
    import ProjectBuildAndOutput from "@/components/project-specifics/ProjectBuildAndOutput.svelte";
    import ProjectEnvVars from "@/components/project-specifics/ProjectEnvVars.svelte";
    import ActionsButtons from "@/components/project-specifics/ActionsButtons.svelte";

    export let project: Project;
    export let activeDeployment: ProjectDeployment | undefined;
    export let currentDeployment: ProjectDeployment | undefined;
    export let envFileContent;
    export let githubBranchInfoCurrent;
    export let sysConf: EngineSystemConfigResponseDto;

    export let githubBranchInfoActive = {
        sha: project?.githubRepo?.default_branch_commit_sha,
        date: project?.githubRepo?.default_branch_commit_date,
        message: "",
    };

    async function initiateRollforward() {
        console.log("initiateRollforward");

        const res = await fetch("/projects/new", {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
                "x-freshcraft-project-type":
                    InternalNewProjectType.ROLLFORWARD_FROM_GITHUB,
            },
            body: JSON.stringify({
                projectId: project.id,
                gh_token: sysConf.systemUserOauthGithubData?.user_access_token,
            }),
        });
    }

    let setup = {
        // Name related
        // projectNameStatus: 'initially_idle',
        projectName: project.uniqueName!,
        // projectNameErrorMessage: '',

        // files related
        filesLoading: true,
        projectSourceList: [],
        projectRootSelectionStatus: "initially_idle",
        selectedFileRelativeUrl: activeDeployment?.src?.rootDirPath,
        projectRootSelectionErrorMessage: "",

        // build related
        buildCommand: activeDeployment?.depCommands?.build ?? "",
        installCommand: activeDeployment?.depCommands?.install ?? "",
        postInstall: activeDeployment?.depCommands?.postInstall ?? "",
        outputDir: activeDeployment?.src?.buildDirPath,

        // env related
        envFileContent: envFileContent ?? "",
        envKV: [],
        options: ["From .env file", "Key-Value Pairs"],
        selectedOptionIdx: 0,
    } as {
        projectName: string;

        filesLoading: boolean;
        projectSourceList: any[];
        projectRootSelectionStatus:
            | "initially_idle"
            | "checking"
            | "success"
            | "error";
        selectedFileRelativeUrl: string;
        projectRootSelectionErrorMessage: string;

        buildCommand: string;
        installCommand: string;
        postInstall: string;
        outputDir: string;

        envFileContent: string;
        envKV: { key: string; value: string }[];
        options: string[];
        selectedOptionIdx: number;
    };

    $: submitBtnDisabled = (() => {
        if (!browser) return true;

        if (!setup.projectName) return true;
        if (!setup.installCommand) return true;
        if (!setup.outputDir) return true;
        if (!setup.selectedFileRelativeUrl) return true;
        return false;
    })();
</script>

<!-- project:{JSON.stringify(project.githubRepo, null, 2)}<br />
<pre>
    {JSON.stringify(githubBranchInfoActive, null, 2)}<br />
</pre>
<pre>
    {JSON.stringify(githubBranchInfoCurrent, null, 2)}<br />
</pre> -->
{#if project.rollforwardDeploymentId && project.currentDeploymentId}
    <div class="py-2">
        <p class="text-xl">Phase 2:</p>
    </div>

    <section class=" flex flex-col gap-3">
        <!-- <h3>{project.uniqueName}</h3> -->
        <div class="grid grid-cols-2">
            <div class="projectInfo">
                <h2 class="text-lg py-2">Project Info</h2>
                <DepInfo {project} {currentDeployment} {activeDeployment} />
            </div>
            <div class="last-status">
                <h2 class="text-lg py-2">Last deployment processing status</h2>
                <DeploymentProcessingHistory
                    messages={project?.partialMessageList ?? []}
                />
            </div>
        </div>

        <ProjectFileSelection
            bind:filesLoading={setup.filesLoading}
            bind:projectSourceList={setup.projectSourceList}
            bind:projectRootSelectionStatus={setup.projectRootSelectionStatus}
            bind:selectedFileRelativeUrl={setup.selectedFileRelativeUrl}
            bind:projectRootSelectionErrorMessage={setup.projectRootSelectionErrorMessage}
            dep={activeDeployment}
        />

        <ProjectBuildAndOutput
            bind:buildCommand={setup.buildCommand}
            bind:installCommand={setup.installCommand}
            bind:postInstall={setup.postInstall}
            bind:outputDir={setup.outputDir}
        />

        <ProjectEnvVars
            bind:options={setup.options}
            bind:selectedOptionIdx={setup.selectedOptionIdx}
            bind:envFileContent={setup.envFileContent}
            bind:envKV={setup.envKV}
        />

        <ActionsButtons
            action={InternalDeploymentActions.ROLLFORWARD}
            bind:submitBtnDisabled
            projectX={project}
            {activeDeployment}
            {currentDeployment}
            bind:setup
        />
    </section>
{:else}
    <!-- Phase 1 -->
    {#if githubBranchInfoActive.sha === githubBranchInfoCurrent.sha}
        <h2 class="text-xl">Project Rollforward: Phase 2</h2>
        <p>Deployment Setup</p>
        <br />
        <ProjectSpecificWarningBox
            msg="No new commits found in the default branch"
            actionText="Back to project"
            actionUrl="/projects/{project.id}"
            iconLeft={CarbonArrowLeft}
        ></ProjectSpecificWarningBox>
    {:else}
        <h2 class="text-xl">Project Rollforward: Phase 1</h2>
        <p>Initiate rollforward</p>
        <br />
        <div
            class="w-full flex flex-col items-center justify-center gap-2 bx--tile py-8"
        >
            <div class="text-center">
                <div class="text-lg font-normal">
                    New commits found in the default branch
                </div>
                <div class="message">
                    Commit message: <span class="font-mono text-indigo-600">
                        {githubBranchInfoCurrent?.message ?? ""}</span
                    >
                </div>
            </div>
            <button
                class="w-full flex gap-2 items-center justify-center max-w-sm py-2.5 rounded mt-3 bx--btn--secondary"
                on:click={initiateRollforward}
            >
                Initiate rollforward
                <CarbonArrowRight class="h-4 w-4 mr-2" />
            </button>
        </div>
    {/if}
{/if}
<!-- {sysConf}
activeDeployment:{activeDeployment}<br />
currentDeployment:{currentDeployment}<br />
envFileContent:{envFileContent}<br />
githubBranchInfoCurrent:{JSON.stringify(githubBranchInfoCurrent, null, 2)}<br />

-------------------------------- <br />

<pre>
    Idea:
    1. if project.rollforwardDeploymentId && project.currentDeploymentId, then it's phase 2: user sets the deployment up
    2. Else, phase 1: user initiates the rollforward

</pre> -->
