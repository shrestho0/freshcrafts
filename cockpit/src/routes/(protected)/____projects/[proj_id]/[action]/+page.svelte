<script lang="ts">
import PreDebug from '@/components/dev/PreDebug.svelte';
import type { Project, ProjectDeployment } from '@/types/entities.js';
import { InternalDeploymentActions, ProjectSetupCommand, ProjectStatus } from '@/types/enums';
import { EnvVarsUtil, toTitleCase } from '@/utils/utils';
import {
	ComposedModal,
	InlineLoading,
	ModalBody,
	ModalFooter,
	ModalHeader,
	Tile
} from 'carbon-components-svelte';
import { onMount } from 'svelte';
import CommonErrorBox from '@/components/CommonErrorBox.svelte';
import { browser } from '$app/environment';

import DepInfo from '@/components/project-specifics/DepInfo.svelte';
import ProjectName from '@/components/project-specifics/ProjectName.svelte';
import ProjectFileSelection from '@/components/project-specifics/ProjectFileSelection.svelte';
import ProjectBuildAndOutput from '@/components/project-specifics/ProjectBuildAndOutput.svelte';
import ProjectEnvVars from '@/components/project-specifics/ProjectEnvVars.svelte';
import ConfirmationModal from '@/components/project-specifics/ConfirmationModal.svelte';
import ActionsButtons from '@/components/project-specifics/ActionsButtons.svelte';

export let data: any;

let loading = true;
const projectX: Project = data?.payload ?? {};
const activeDeployment: ProjectDeployment = data?.payload2 ?? {};
const currentDeployment: ProjectDeployment = data?.payload3 ?? {};
const toRedeployDeployment: ProjectDeployment | null =
	projectX.status === ProjectStatus.ACTIVE && activeDeployment
		? activeDeployment
		: projectX.status === ProjectStatus.INACTIVE && currentDeployment
			? currentDeployment
			: null;

const action = data.action as InternalDeploymentActions;

const projectSetup = {
	projectName: '',
	projectNameStatus: 'initially_idle',
	projectNameErrorMessage: '',

	envFileContent: '',
	envKV: [],
	options: ['From .env file', 'Key-Value Pairs'],
	selectedOptionIdx: 0,

	buildCommand: 'npm run build',
	installCommand: 'npm install',
	postInstall: '',
	outputDir: './build',

	filesLoading: true,
	projectRootSelectionStatus: 'initially_idle',
	projectRootSelectionErrorMessage: '',
	projectSourceTreeActiveId: 0,
	projectSourceTreeSelectedIds: 0,
	projectSourceTreeTotalLevels: 0,
	projectSourceTreeFinalIotaVal: 0,
	projectSourceList: [],
	selectedFileRelativeUrl: ''
} as {
	projectName: string;
	projectNameStatus: 'initially_idle' | 'checking' | 'invalid' | 'ok';
	projectNameErrorMessage: string;

	envFileContent: string;
	envKV: { key: string; value: string }[];
	options: string[];
	selectedOptionIdx: number;
	buildCommand: string;
	installCommand: string;
	postInstall: string;
	outputDir: string;

	filesLoading: boolean;
	projectSourceList: any[];
	projectRootSelectionStatus: 'initially_idle' | 'checking' | 'success' | 'error';
	selectedFileRelativeUrl: string;
	projectRootSelectionErrorMessage: string;
	projectSourceTreeActiveId: any;
	projectSourceTreeSelectedIds: any;
	projectSourceTreeTotalLevels: number;
	projectSourceTreeFinalIotaVal: number;

	// runCommand: string;
};

// onMount(async () => {
// 	// setTimeout(async () => {
// 	await listSourceFile();
// 	loading = false;
// 	// }, 1000);
// });

let confirmationModalOpen = false;
$: checkAndSubmitButtonDisabled = (() => {
	if (!browser) return true;
	// if (projectSetup.projectNameStatus !== 'ok') return true;
	if (action == InternalDeploymentActions.INITIAL_SETUP && !projectSetup.projectName) return true;
	if (!projectSetup.buildCommand) return true;
	if (!projectSetup.installCommand) return true;
	if (!projectSetup.outputDir) return true;
	if (!projectSetup.selectedFileRelativeUrl) return true;
	return false;
})();

let actionLoading = false;
let loadingMessage = '';
let successMessage = '';
let errorMessage = '';

onMount(() => {
	loading = false;

	// set stuff
	// if (currentDeployment) {
	// 	projectSetup.selectedFileRelativeUrl = currentDeployment?.src?.rootDirPath ?? '';
	// } else if (activeDeployment) {
	// 	projectSetup.selectedFileRelativeUrl = activeDeployment?.src?.rootDirPath ?? '';
	// }

	// if (projectX.status === ProjectStatus.ACTIVE) {
	// projectSetup.selectedFileRelativeUrl = activeDeployment?.src?.rootDirPath ?? '';
	// } else if (projectX.status === ProjectStatus.INACTIVE) {
	// 	projectSetup.selectedFileRelativeUrl = currentDeployment?.src?.rootDirPath ?? '';
	// }

	// re-deployment data mapping

	// projectSetup.selectedFileRelativeUrl = toRedeployDeployment?.src?.rootDirPath ?? '';

	if (toRedeployDeployment?.src?.rootDirPath)
		projectSetup.selectedFileRelativeUrl = toRedeployDeployment?.src?.rootDirPath;
	if (toRedeployDeployment?.depCommands?.build)
		projectSetup.buildCommand = toRedeployDeployment?.depCommands?.build;
	if (toRedeployDeployment?.depCommands?.install)
		projectSetup.installCommand = toRedeployDeployment?.depCommands?.install;
	if (toRedeployDeployment?.depCommands?.postInstall)
		projectSetup.postInstall = toRedeployDeployment?.depCommands?.postInstall;

	if (projectSetup.projectName) projectSetup.projectName = projectX?.uniqueName;

	if (data?.envFileContent) {
		projectSetup.envFileContent = data.envFileContent;
	}
});

// async function deployProject() {
// 	let envContentFinal = '';
// 	actionLoading = true;
// 	confirmationModalOpen = false;
// 	loadingMessage = 'Requested incomplete deployment creation.';
// 	if (projectSetup.selectedOptionIdx === 0) {
// 		envContentFinal = projectSetup.envFileContent;
// 	} else {
// 		envContentFinal = EnvVarsUtil.kvToContent(projectSetup.envKV);
// 	}

// 	console.log(projectSetup.projectName);

// 	const res = await fetch('', {
// 		method: 'PATCH',
// 		headers: {
// 			'Content-Type': 'application/json'
// 		},
// 		body: JSON.stringify({
// 			command: ProjectSetupCommand.DEPLOY_PROJECT,
// 			data: {
// 				projectName: projectSetup.projectName,
// 				buildCommand: projectSetup.buildCommand,
// 				installCommand: projectSetup.installCommand,
// 				postInstall: projectSetup.postInstall,
// 				outputDir: projectSetup.outputDir,
// 				selectedFileRelativeUrl: projectSetup.selectedFileRelativeUrl,
// 				envFileContent: envContentFinal,
// 				deployment: currentDeployment,
// 				projectId: projectX?.id
// 			}
// 		})
// 	})
// 		.then((res) => res.json())
// 		.catch((e) => console.error(e));

// 	console.log('create project res', res);
// 	actionLoading = false;
// 	loadingMessage = '';
// 	if (res?.success) {
// 		successMessage = res?.message;
// 		console.log('Project created successfully');
// 		backToProjectSpecificPage();
// 	} else {
// 		errorMessage = res?.message;
// 		console.error(res?.message);
// 	}
// }
</script>

{projectX?.uniqueName}

{#if !loading}
	{#if projectX.status.startsWith('PROCESSING')}
		<p>Project can not be modified while be processed</p>
		<CommonErrorBox error_msg="Project is not in initial setup state" />
	{:else}
		<h1 class="text-3xl py-2">{toTitleCase(action.replace('-', ' ') ?? '')}</h1>

		{#if projectX.status != ProjectStatus.AWAIT_INITIAL_SETUP}
			<DepInfo {projectX} {currentDeployment} {activeDeployment} />
		{/if}

		<section class=" flex flex-col gap-3">
			{#if projectX.status == ProjectStatus.AWAIT_INITIAL_SETUP}
				<ProjectName
					bind:projectName={projectSetup.projectName}
					bind:projectNameStatus={projectSetup.projectNameStatus}
					bind:projectNameErrorMessage={projectSetup.projectNameErrorMessage}
				/>
			{/if}

			{#if action == InternalDeploymentActions.INITIAL_SETUP || action == InternalDeploymentActions.RE_DEPLOY}
				<ProjectFileSelection
					bind:filesLoading={projectSetup.filesLoading}
					bind:projectSourceList={projectSetup.projectSourceList}
					bind:projectRootSelectionStatus={projectSetup.projectRootSelectionStatus}
					bind:selectedFileRelativeUrl={projectSetup.selectedFileRelativeUrl}
					bind:projectRootSelectionErrorMessage={projectSetup.projectRootSelectionErrorMessage}
					bind:projectSourceTreeTotalLevels={projectSetup.projectSourceTreeTotalLevels}
					bind:projectSourceTreeFinalIotaVal={projectSetup.projectSourceTreeFinalIotaVal}
					{toRedeployDeployment}
				/>

				<ProjectBuildAndOutput
					bind:buildCommand={projectSetup.buildCommand}
					bind:installCommand={projectSetup.installCommand}
					bind:postInstall={projectSetup.postInstall}
					bind:outputDir={projectSetup.outputDir}
				/>

				<ProjectEnvVars
					bind:options={projectSetup.options}
					bind:selectedOptionIdx={projectSetup.selectedOptionIdx}
					bind:envFileContent={projectSetup.envFileContent}
					bind:envKV={projectSetup.envKV}
				/>
			{/if}

			{#if actionLoading}
				<InlineLoading description={actionLoading ? loadingMessage : ''} />
			{:else if successMessage}
				<InlineLoading status="finished" description={successMessage} />
			{:else if errorMessage}
				<InlineLoading status="error" description={successMessage} />
			{/if}

			<ActionsButtons
				{action}
				bind:loadingMessage
				bind:successMessage
				bind:errorMessage
				bind:actionLoading
				bind:checkAndSubmitButtonDisabled
				bind:confirmationModalOpen
				{projectX}
				{currentDeployment}
				{activeDeployment}
				{backToProjectSpecificPage}
				{backToProjectCreationPage}
			/>
		</section>
		<ConfirmationModal
			bind:confirmationModalOpen
			bind:projectName={projectSetup.projectName}
			bind:buildCommand={projectSetup.buildCommand}
			bind:installCommand={projectSetup.installCommand}
			bind:postInstall={projectSetup.postInstall}
			bind:outputDir={projectSetup.outputDir}
			bind:selectedFileRelativeUrl={projectSetup.selectedFileRelativeUrl}
			bind:envKV={projectSetup.envKV}
			bind:envFileContent={projectSetup.envFileContent}
			bind:selectedOptionIdx={projectSetup.selectedOptionIdx}
			bind:actionLoading
			bind:loadingMessage
			bind:successMessage
			bind:errorMessage
			{currentDeployment}
			projectId={projectX?.id}
			{backToProjectSpecificPage}
		/>
	{/if}
{:else}
	Loading...
{/if}

<PreDebug data={data?.payload} title="Project" />
<PreDebug data={data?.payload2} title="Active Deployment" />
<PreDebug data={data?.payload3} title="Current Deployment (deployment is being procceed)" />
