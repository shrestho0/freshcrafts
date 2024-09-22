<script lang="ts">
	export let data;
	import ProjectSpecificWarningBox from "@/components/project-specifics/ProjectSpecificWarningBox.svelte";
	import PreDebug from "@/components/dev/PreDebug.svelte";
	import ProjectName from "@/components/project-specifics/ProjectName.svelte";
	import type { Project, ProjectDeployment } from "@/types/entities.js";
	import { InternalDeploymentActions, ProjectStatus } from "@/types/enums";
	import CarbonArrowLeft from "@/ui/icons/CarbonArrowLeft.svelte";
	import ProjectFileSelection from "@/components/project-specifics/ProjectFileSelection.svelte";
	import ProjectBuildAndOutput from "@/components/project-specifics/ProjectBuildAndOutput.svelte";
	import ProjectEnvVars from "@/components/project-specifics/ProjectEnvVars.svelte";
	import { InlineLoading, TextInput } from "carbon-components-svelte";
	import ActionsButtons from "@/components/project-specifics/ActionsButtons.svelte";
	import { browser } from "$app/environment";
	import DepInfo from "@/components/project-specifics/DepInfo.svelte";
	import DeploymentProcessingHistory from "../DeploymentProcessingHistory.svelte";

	const project: Project = data.project!;
	const currentDeployment: ProjectDeployment = data.currentDeployment!;

	let actionLoading = false;
	let loadingMessage = "";
	let successMessage = "";
	let errorMessage = "";

	let setup = {
		// Name related
		// projectNameStatus: 'initially_idle',
		projectName: project.uniqueName!,
		// projectNameErrorMessage: '',

		// files related
		filesLoading: true,
		projectSourceList: [],
		projectRootSelectionStatus: "initially_idle",
		selectedFileRelativeUrl: currentDeployment?.src?.rootDirPath,
		projectRootSelectionErrorMessage: "",

		// build related
		buildCommand: currentDeployment?.depCommands?.build ?? "",
		installCommand: currentDeployment?.depCommands?.install ?? "",
		postInstall: currentDeployment?.depCommands?.postInstall ?? "",
		outputDir: currentDeployment.src.buildDirPath,

		// env related
		envFileContent: data.envFileContent ?? "",
		envKV: [],
		options: ["From .env file", "Key-Value Pairs"],
		selectedOptionIdx: 0,
		version: currentDeployment.version,
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

		version: number;
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

{currentDeployment.src.rootDirPath}
{#if project.status != ProjectStatus.INACTIVE}
	<ProjectSpecificWarningBox
		msg="Re-deploy is only possible for inactive projects with failed current deployments"
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
				<DepInfo
					{project}
					{currentDeployment}
					activeDeployment={undefined}
				/>
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
			dep={currentDeployment}
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
			action={InternalDeploymentActions.RE_DEPLOY}
			bind:submitBtnDisabled
			projectX={project}
			{currentDeployment}
			activeDeployment={undefined}
			bind:setup
		/>
	</section>
{/if}

<pre>
    TODO: Set redeploy true flag
	Note:
    Re-deploy


    
    when it can be re-deployed:
    
    in case of failure
    
    if a project is okay, it can be updated not re-deployed
</pre>

<PreDebug data={project} title="Project" />
<PreDebug data={currentDeployment} title="Current Deployment" />
<PreDebug {data} />
