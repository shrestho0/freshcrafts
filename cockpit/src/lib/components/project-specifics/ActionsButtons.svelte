<script lang="ts">
import { InternalDeploymentActions, ProjectSetupCommand } from '@/types/enums';
import ConfirmationModal from './ConfirmationModal.svelte';
import { InlineLoading } from 'carbon-components-svelte';
import { EnvVarsUtil } from '@/utils/utils';

export let action: InternalDeploymentActions;
export let loadingMessage: any;
export let successMessage: any;
export let errorMessage: any;
export let actionLoading: any;
export let submitBtnDisabled: any;
export let projectX: any;
export let currentDeployment: any;
export let activeDeployment: any;
export let setup: any;

let confirmationModalOpen: any;

async function deleteIncompleteProject() {
	console.log('deleteIncompleteProject', currentDeployment, activeDeployment);

	actionLoading = true;
	loadingMessage = 'Requested incomplete deployment delete';

	const deploymentId = currentDeployment?.id ?? activeDeployment?.id ?? null;
	console.log('deploymentId', deploymentId);

	if (!deploymentId) {
		errorMessage = 'Deployment ID not found';
		actionLoading = false;
		return;
	}
	const res = await fetch('/projects', {
		method: 'PATCH',
		headers: {
			'Content-Type': 'application/json',
			'x-freshcrafts-project-setup-command': ProjectSetupCommand.DELETE_INCOMPLETE_PROJECT
		},
		body: JSON.stringify({
			projectId: projectX?.id,
			deploymentId,
			rawFileAbsPath: currentDeployment?.rawFile?.absPath,
			srcFileAbsPath: currentDeployment?.src?.filesDirAbsPath
		})
	})
		.then((res) => res.json())
		.catch((e) => console.error(e));

	actionLoading = false;
	loadingMessage = '';
	if (res?.success) {
		successMessage = res?.message + ' Page will be redirected soon.';
		backToProjectCreationPage();
	} else {
		errorMessage = res?.message;
	}
}

async function deployProject() {
	let envContentFinal = '';
	actionLoading = true;
	confirmationModalOpen = false;
	loadingMessage = 'Requested incomplete deployment creation.';

	if (setup.selectedOptionIdx === 0) {
		envContentFinal = setup.envFileContent;
	} else {
		envContentFinal = EnvVarsUtil.kvToContent(setup.envKV);
	}

	console.log(setup.projectName);

	const res = await fetch('/projects', {
		method: 'PATCH',
		headers: {
			'Content-Type': 'application/json',
			'x-freshcrafts-project-setup-command': ProjectSetupCommand.DEPLOY_PROJECT
		},
		body: JSON.stringify({
			projectId: projectX?.id,
			projectName: setup.projectName,
			buildCommand: setup.buildCommand,
			installCommand: setup.installCommand,
			postInstall: setup.postInstall,
			outputDir: setup.outputDir,
			selectedFileRelativeUrl: setup.selectedFileRelativeUrl,
			envFileContent: envContentFinal,
			deployment: currentDeployment
		})
	})
		.then((res) => res.json())
		.catch((e) => console.error(e));

	console.log('create project res', res);
	actionLoading = false;
	loadingMessage = '';
	if (res?.success) {
		successMessage = res?.message;
		console.log('Project created successfully');
		backToProjectSpecificPage();
	} else {
		errorMessage = res?.message;
		console.error(res?.message);
	}
}
function backToProjectCreationPage() {
	setTimeout(() => {
		window.location.href = '/projects/all';
	}, 1000);
}

function backToProjectSpecificPage() {
	console.log('backToProjectSpecificPage', projectX?.id);
	setTimeout(() => {
		window.location.href = '/projects/' + projectX?.id;
	}, 1000);
}

let btn_common_class = `py-4 my-6 w-full `;
let left_btn_class = `bx--btn--danger-tertiary
disabled:bg-[var(--cds-disabled-02)] disabled:text-[var(--cds-disabled-03)]
disabled:hover:bg-[var(--cds-disabled-02)] disabled:hover:text-[var(--cds-disabled-03)]
disabled:border-[var(--cds-disabled-02)]`;
let right_btn_class = `bx--btn--primary
        disabled:bg-[var(--cds-disabled-02)] disabled:text-[var(--cds-disabled-03)]
        disabled:hover:bg-[var(--cds-disabled-02)] disabled:hover:text-[var(--cds-disabled-03)]
        disabled:cursor-not-allowed`;
</script>

{action}

{#if actionLoading}
	<InlineLoading description={actionLoading ? loadingMessage : ''} />
{:else if successMessage}
	<InlineLoading status="finished" description={successMessage} />
{:else if errorMessage}
	<InlineLoading status="error" description={successMessage} />
{/if}

<div class="dep grid grid-cols-3 gap-2">
	<div class="w-full btn-left col-span-1">
		{#if action == InternalDeploymentActions.INITIAL_SETUP}
			<button
				class=" {btn_common_class} {left_btn_class}"
				disabled={actionLoading}
				on:click={deleteIncompleteProject}>Cancel Project</button
			>
		{:else if action == InternalDeploymentActions.RE_DEPLOY}
			<button
				class="{btn_common_class} {left_btn_class}"
				disabled={actionLoading}
				on:click={backToProjectSpecificPage}>Back to project</button
			>
		{/if}
	</div>
	<div class="w-full btn-right col-span-2">
		<!-- something -->
		{#if action == InternalDeploymentActions.INITIAL_SETUP || InternalDeploymentActions.RE_DEPLOY}
			<button
				disabled={submitBtnDisabled || actionLoading}
				on:click={() => {
					confirmationModalOpen = true;
				}}
				class="{right_btn_class} {btn_common_class}">Check & Deploy</button
			>
		{/if}
	</div>
</div>

<ConfirmationModal bind:confirmationModalOpen bind:setup {deployProject} />
