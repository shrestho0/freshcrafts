<script lang="ts">
import { invalidateAll } from '$app/navigation';
import CommonErrorBox from '@/components/CommonErrorBox.svelte';
import PreDebug from '@/components/dev/PreDebug.svelte';
import type { EngineCommonResponseDto } from '@/types/dtos';
import type { Project, ProjectDeployment } from '@/types/entities';
import { ProjectStatus } from '@/types/enums';
import { onMount } from 'svelte';
import EnvUpdateOption from './EnvUpdateOption.svelte';

export let data: EngineCommonResponseDto<
	Project,
	null,
	ProjectDeployment | null,
	ProjectDeployment | null
>;

let interval;

onMount(() => {
	console.log('just mounted');
	if (data?.payload?.status == ProjectStatus.PROCESSING_DEPLOYMENT || true) {
		// TODO: Check logic later
		internalRefresh();
	}
});
function internalRefresh() {
	setInterval(() => {
		console.log('internally refreshing', Date.now());
		invalidateAll();
	}, 2000);
}
</script>

{#if data.success}
	{#if data?.payload?.status === ProjectStatus.PROCESSING_DEPLOYMENT}
		Deployment is being proceed with deployment status: {data?.payload3?.status}
		{data?.payload3?.partialDeploymentMsg}
	{/if}
	<!-- egula shob shomoy pabe -->
	<EnvUpdateOption />
{:else}
	<CommonErrorBox error_msg={data?.message ?? 'Failed to retrieve data'} />
{/if}

<PreDebug data={data?.payload} title="Project" />
<PreDebug data={data?.payload2} title="Active Deployment" />
<PreDebug data={data?.payload3} title="Current Deployment (deployment is being procceed)" />
