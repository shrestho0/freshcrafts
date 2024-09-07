<script lang="ts">
import { page } from '$app/stores';
import PreDebug from '@/components/dev/PreDebug.svelte';
import ProjectSpecificWarningBox from '@/components/project-specifics/ProjectSpecificWarningBox.svelte';
import type { Project, ProjectDeployment } from '@/types/entities.js';
import { InternalDeploymentActions, ProjectStatus } from '@/types/enums';
import CarbonArrowRight from '@/ui/icons/CarbonArrowRight.svelte';
import DeploymentProcessing from './DeploymentProcessing.svelte';
import CurrentDeploymentFailure from './CurrentDeploymentFailure.svelte';
import ActiveDeployment from './ActiveDeployment.svelte';
import { invalidateAll } from '$app/navigation';

export let data;
const project: Project = data.project!;
const currentDeployment: ProjectDeployment | undefined = data.currentDeployment;
const activeDeployment: ProjectDeployment | undefined = data.activeDeployment;
</script>

{#if project.status === ProjectStatus.AWAIT_INITIAL_SETUP}
	<ProjectSpecificWarningBox
		msg="Initial setup not complete"
		actionUrl={`${$page.url}/${InternalDeploymentActions.INITIAL_SETUP}`}
		actionText="Complete initial setup"
		iconRight={CarbonArrowRight}
	/>
{:else if project.status === ProjectStatus.ACTIVE}
	<ActiveDeployment {project} {activeDeployment} {currentDeployment} />
{:else if project.status === ProjectStatus.INACTIVE}
	<CurrentDeploymentFailure {project} {currentDeployment} />
{:else if project.status.toString().startsWith('PROCESSING')}
	<!-- {#if project.status === ProjectStatus.PROCESSING_DEPLOYMENT} -->
	<DeploymentProcessing
		sse
		messages={project.partialMessageList}
		onRevalidate={() => {
			console.log('revalidate func called');
			// invalidateAll();
			window.location.reload();
		}}
	/>
	<!-- {:else if project.status === ProjectStatus.PROCESSING_DELETION} -->
	<!-- <DeploymentProcessing sse messages={project.partialMessageList} projectId={project.id} /> -->
	<!-- {/if} -->
{/if}
<pre>//////////////////////////////////////////////////////////////////////////////////////////////////////////</pre>
<pre>//////////////////////////////////////////////////////////////////////////////////////////////////////////</pre>
<PreDebug {data} />
