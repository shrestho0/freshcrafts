<script lang="ts">
    import DepInfo from "@/components/project-specifics/DepInfo.svelte";
    import ProjectSpecificWarningBox from "@/components/project-specifics/ProjectSpecificWarningBox.svelte";
    import { InternalDeploymentActions, ProjectStatus } from "@/types/enums.js";
    import CarbonArrowLeft from "@/ui/icons/CarbonArrowLeft.svelte";
    import DeploymentProcessingHistory from "../DeploymentProcessingHistory.svelte";
    import ProjectFileSelection from "@/components/project-specifics/ProjectFileSelection.svelte";
    import { browser } from "$app/environment";
    import ProjectBuildAndOutput from "@/components/project-specifics/ProjectBuildAndOutput.svelte";
    import ProjectEnvVars from "@/components/project-specifics/ProjectEnvVars.svelte";
    import ActionsButtons from "@/components/project-specifics/ActionsButtons.svelte";
    export let data;
    const project = data.project!;
    const currentDeployment = data.currentDeployment;
    const activeDeployment = data.activeDeployment;

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
        envFileContent: data.envFileContent ?? "",
        envKV: [],
        options: ["From .env file", "Key-Value Pairs"],
        selectedOptionIdx: 0,
    } as {
        projectName: string;
        // projectNameStatus: 'initially_idle' | 'checking' | 'invalid' | 'ok';
        // projectNameErrorMessage: string;

        filesLoading: boolean;
        projectSourceList: any[];
        projectRootSelectionStatus:
            | "initially_idle"
            | "checking"
            | "success"
            | "error";
        selectedFileRelativeUrl: string;
        projectRootSelectionErrorMessage: string;
        // projectSourceTreeActiveId: any;
        // projectSourceTreeSelectedIds: any;
        // projectSourceTreeTotalLevels: number;
        // projectSourceTreeFinalIotaVal: number;

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

        // if (setup.projectNameStatus !== 'ok') return true;
        if (!setup.projectName) return true;
        // if (!setup.buildCommand) return true;
        if (!setup.installCommand) return true;
        if (!setup.outputDir) return true;
        if (!setup.selectedFileRelativeUrl) return true;
        return false;
    })();
</script>

<div class="py-2">
    <h2 class="text-xl">Update deployment</h2>
    <p>Modify deployment data</p>
</div>

{#if project.status != ProjectStatus.ACTIVE}
    <ProjectSpecificWarningBox
        msg="Update deployment is only possible for active projects with active deployment"
        actionUrl="/projects/{project.id}"
        actionText="Back to project page"
        iconLeft={CarbonArrowLeft}
    />
{:else}
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
            action={InternalDeploymentActions.UPDATE_DEPLOYMENT}
            bind:submitBtnDisabled
            projectX={project}
            {activeDeployment}
            {currentDeployment}
            bind:setup
        />
    </section>
{/if}
