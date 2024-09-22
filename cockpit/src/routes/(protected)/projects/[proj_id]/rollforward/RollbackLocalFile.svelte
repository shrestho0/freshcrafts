<!--

TODO: This should be RollforwardLocalFile not RollbackLocalFile
-->
<script lang="ts">
    import { browser } from "$app/environment";
    import PreDebug from "@/components/dev/PreDebug.svelte";
    import DepInfo from "@/components/project-specifics/DepInfo.svelte";
    import FromDevice from "@/components/project-specifics/files/FromDevice.svelte";
    import DeploymentProcessingHistory from "../DeploymentProcessingHistory.svelte";
    import ProjectFileSelection from "@/components/project-specifics/ProjectFileSelection.svelte";
    import ProjectBuildAndOutput from "@/components/project-specifics/ProjectBuildAndOutput.svelte";
    import ProjectEnvVars from "@/components/project-specifics/ProjectEnvVars.svelte";
    import ActionsButtons from "@/components/project-specifics/ActionsButtons.svelte";
    import { InternalDeploymentActions } from "@/types/enums";

    export let project;
    export let activeDeployment;
    export let currentDeployment;
    export let envFileContent;

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

{#if project.rollforwardDeploymentId && project.currentDeploymentId}
    <div class="py-2">
        <h2 class="text-xl">Project Rollforward: Phase 2</h2>
        <p>Modify deployment data</p>
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
    <div class="py-2">
        <h2 class="text-xl">Project Rollforward: Phase 1</h2>
        <p>Upload new file</p>
    </div>

    <!-- if active and, if upcoming deployment, rollforward case

    <pre>
    1. Upload file and all necessary data
    2. Create deployment and set project rollforwardDeploymentId
    3. Rest is simple
    4. 
</pre> -->

    <h2 class="text-xl">Upload new file</h2>

    <FromDevice rollforward={true} projectId={project.id} />
{/if}
<PreDebug data={setup} />
<PreDebug data={project} title={"project"} />
<PreDebug data={activeDeployment} title={"active dep"} />
<PreDebug data={currentDeployment} title={"current dep"} />
<!-- there should not be any current dep-->
<!-- <PreDebug data={data.currentDeployment} title={"current dep"} /> -->
