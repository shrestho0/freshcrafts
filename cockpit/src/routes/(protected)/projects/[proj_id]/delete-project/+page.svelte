<script lang="ts">
import { browser } from '$app/environment';
import { goto } from '$app/navigation';
import PreDebug from '@/components/dev/PreDebug.svelte';
import ActionsButtons from '@/components/project-specifics/ActionsButtons.svelte';
import DepInfo from '@/components/project-specifics/DepInfo.svelte';
import type { Project, ProjectDeployment } from '@/types/entities';
import { InternalDeploymentActions, ProjectSetupCommand, ProjectStatus } from '@/types/enums';
import CarbonInformation from '@/ui/icons/CarbonInformation.svelte';
import { InlineLoading } from 'carbon-components-svelte';
import { onMount } from 'svelte';
import DeploymentProcessing from '../DeploymentProcessing.svelte';

export let data: {
	project: Project;
	currentDeployment: ProjectDeployment | undefined;
	activeDeployment: ProjectDeployment | undefined;
};

let actionMessage = '';
let actionStatus: 'active' | 'error' | 'finished' | 'inactive' = 'inactive';

async function gotoAfterMS(url: string, ms: number = 1000) {
	if (!browser) return;
	setTimeout(() => {
		goto(url, {
			invalidateAll: true
		});
	}, ms);
}

onMount(() => {
	// document.cookie = `error_fallback=${JSON.stringify({
	// 	href: `/projects/all`,
	// 	title: 'Back to projects'
	// })}; path=/; max-age=60000; secure=false; httpOnly=false;sameSite=lax`;
	// document.coo
});

let sse = false;
let messages: string[] = data?.project?.partialMessageList ?? [];

async function deleteProject() {
	actionStatus = 'active';
	actionMessage = 'Requested project delete';

	// messages = [`[RUNNING] Deleting project ${data.project.uniqueName}`];
	messages = [];
	sse = true;

	const projectId = data.project.id!;
	const res = await fetch('/projects', {
		method: 'PATCH',
		headers: {
			'Content-Type': 'application/json',
			'x-freshcrafts-project-setup-command': ProjectSetupCommand.DELETE_PROJECT
		},
		body: JSON.stringify({
			projectId
			// deploymentId: currentDeployment?.id ?? activeDeployment?.id
		})
	})
		.then((res) => res.json())
		.catch((e) => console.error(e));

	console.log('deleteProject res', res);

	if (res?.success) {
		actionStatus = 'finished';
		actionMessage = res?.message + ' Page will be redirected soon.';
		// if (data.project.status === ProjectStatus.PROCESSING_DELETION) {
		// gotoAfterMS(`/projects/all`);
		// this is not acceptable
		// cookies.set('error_fallback', JSON.stringify({
		//     href: `/projects/${proj_id}`,
		//     title: 'Back to project'

		// }), { path: '/', maxAge: 60 * 60 * 1, secure: false, httpOnly: false })
		// } else {
		// 	gotoAfterMS(`/projects/${projectId}`);
		// }

		// gotoAfterMS(`/projects/${projectId}`);
	} else {
		actionStatus = 'error';
		actionMessage = res?.message;
	}
}
</script>

<!-- {#if actionLoading}
	<InlineLoading description={actionLoading ? loadingMessage : ''} />
{:else if successMessage}
	<InlineLoading status="finished" description={successMessage} />
{:else if errorMessage} -->
<div class="grid grid-cols-2 gap-6">
	<div>
		<h2 class="text-xl pb-2">Project Information</h2>
		<DepInfo
			project={data.project}
			currentDeployment={data.currentDeployment}
			activeDeployment={data.activeDeployment}
		/>
	</div>
	<div>
		<h2 class="text-xl pb-2">Project Progress</h2>
		{#key sse}
			<DeploymentProcessing {messages} {sse} />
		{/key}
	</div>
</div>

{#if actionMessage}
	<InlineLoading status={actionStatus} description={actionMessage} />
{/if}
sdsd
<div class="w-full fullsize-btn col-span-3">
	<div class="flex items-center gap-2 text-lg">
		<CarbonInformation class="w-6 h-6" />
		Note: All previous deployment and project data will be lost
	</div>

	<button on:click={deleteProject} class="py-4 my-6 w-full bx--btn--danger-tertiary"
		>Delete Project</button
	>
</div>

<PreDebug {data} />
