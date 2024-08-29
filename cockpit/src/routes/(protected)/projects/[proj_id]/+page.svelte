<script lang="ts">
import { invalidateAll } from '$app/navigation';
import CommonErrorBox from '@/components/CommonErrorBox.svelte';
import PreDebug from '@/components/dev/PreDebug.svelte';
import type { EngineCommonResponseDto } from '@/types/dtos';
import type { Project, ProjectDeployment } from '@/types/entities';
import { ProjectStatus } from '@/types/enums';
import { onMount } from 'svelte';
import { Check, ServerCog } from 'lucide-svelte';
import DeploymentProcessing from './DeploymentProcessing.svelte';
import { Button } from 'carbon-components-svelte';
import { source } from 'sveltekit-sse';
import { page } from '$app/stores';
import { browser } from '$app/environment';

export let data: EngineCommonResponseDto<
	Project,
	null,
	ProjectDeployment | null,
	ProjectDeployment | null
>;

let project: Project = data?.payload;

const sse_url = `/sse/projects/${project.id}`;
const value = source(sse_url).select('message');

// let activeDeployment: ProjectDeployment = data?.payload2!;
let currentDeployment: ProjectDeployment = data?.payload3!;

let messages = currentDeployment?.partialDeploymentMsg.split('\n') ?? [];
if (browser) {
	value.subscribe((message) => {
		// check if message is empty
		if (message.trim() === '') return;

		// check if message is multiline
		if (message.includes('\n')) {
			const multiLineMessages = message.split('\n');
			messages = [...messages, ...multiLineMessages];
			invalidateAll();
			return;
		}

		console.log('Message', message);
		// messages.push(message);

		messages = [...messages, message];
		invalidateAll();
	});
	// console.log('Value', $value);
	// setTimeout(() => {
	// 	invalidateAll();
	// }, 1000);
}

let interval;

onMount(() => {
	console.log('just mounted');
	// if (data?.payload?.status == ProjectStatus.AC || true) {
	// TODO: Check logic later
	// internalRefresh();
	// }
});
// function internalRefresh() {
// 	setInterval(() => {
// 		console.log('internally refreshing', Date.now());
// 		invalidateAll();
// 	}, 2000);
// }
</script>

{#if data.success}
	{#if project.status === ProjectStatus.PROCESSING_DEPLOYMENT}
		Deployment is being proceed with deployment status: {data?.payload3?.status}
		{#key messages}
			<DeploymentProcessing {messages} />
		{/key}
	{:else if project.status === ProjectStatus.ACTIVE}
		<PreDebug data={data?.payload} title="Project" />
		<PreDebug data={data?.payload2} title="Active Deployment" />
	{:else if project.status === ProjectStatus.INACTIVE}
		{#if project.totalVersions == 0}
			<CommonErrorBox error_msg="Initial deployment failed">
				<div class="flex gap-3 py-4">
					<a href="./{project.id}/re-deploy" class="flex flex-col gap-2">
						<ServerCog />
						<p>Re-deploy</p>
					</a>
				</div>
			</CommonErrorBox>

			<!-- processing list -->
			<!-- <DeploymentProcessing bind:messages /> -->

			Initial deployment failed
		{/if}
		<!-- failed deployment-->
		<Check size="24" />
		Project is inactive.
	{/if}
	<!-- egula shob shomoy pabe -->
{:else}
	<CommonErrorBox error_msg={data?.message ?? 'Failed to retrieve data'} />
{/if}
<br />
<br />
<br />
<br />
<!-- <DeploymentProcessing {currentDeployment} /> -->

<!-- Being Deployed info-->

<PreDebug data={data?.payload} title="Project" />
<PreDebug data={data?.payload2} title="Active Deployment" />
<PreDebug data={data?.payload3} title="Current Deployment (deployment is being procceed)" />
