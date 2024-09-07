<script lang="ts">
import { invalidateAll } from '$app/navigation';
import CommonErrorBox from '@/components/CommonErrorBox.svelte';
import PreDebug from '@/components/dev/PreDebug.svelte';
import type { EngineCommonResponseDto } from '@/types/dtos';
import type { Project, ProjectDeployment } from '@/types/entities';
import { ProjectStatus } from '@/types/enums';
import { onMount } from 'svelte';
import DeploymentProcessing from '../../projects/[proj_id]/DeploymentProcessing.svelte';

import { toTitleCase } from '@/utils/utils';
import CurrentDeploymentFailure from './CurrentDeploymentFailure.svelte';

export let data: EngineCommonResponseDto<
	Project,
	null,
	ProjectDeployment | null,
	ProjectDeployment | null
>;

let project: Project | null = data?.payload ?? null;
let currentDeployment: ProjectDeployment | null = data?.payload3 ?? null;
let activeDeployment: ProjectDeployment | null = data?.payload2 ?? null;

// let activeDeployment: ProjectDeployment = data?.payload2!;

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
	<h1 class="text-3xl py-2">{toTitleCase(project?.uniqueName ?? '')}</h1>

	{#if project.status === ProjectStatus.PROCESSING_DEPLOYMENT}
		Deployment is being proceed with deployment status: {data?.payload3?.status}
		{#if currentDeployment}
			<DeploymentProcessing {currentDeployment} sse />
		{/if}
	{:else if project.status === ProjectStatus.ACTIVE}
		Active Deployment
	{:else if project.status === ProjectStatus.INACTIVE}
		{#if currentDeployment}
			<CurrentDeploymentFailure {project} {currentDeployment} />
		{:else}
			<!-- failed deployment-->
		{/if}
		<!-- failed deployment-->
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
