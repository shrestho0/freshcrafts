<script lang="ts">
import type { ProjectDeployment } from '@/types/entities';
import { ProjectDeploymentStatus } from '@/types/enums';
import { toTitleCase } from '@/utils/utils';
import { Tag } from 'carbon-components-svelte';

export let status: ProjectDeploymentStatus;

type tagType =
	| 'red'
	| 'magenta'
	| 'purple'
	| 'blue'
	| 'cyan'
	| 'teal'
	| 'green'
	| 'gray'
	| 'cool-gray'
	| 'warm-gray'
	| 'high-contrast'
	| 'outline';

const colorMappping: Record<ProjectDeploymentStatus, tagType> = {
	[ProjectDeploymentStatus.PRE_CREATION]: 'red',
	[ProjectDeploymentStatus.READY_FOR_DEPLOYMENT]: 'blue',
	[ProjectDeploymentStatus.REQUESTED_DEPLOYMENT]: 'high-contrast',
	[ProjectDeploymentStatus.REQUESTED_REDEPLOYMENT]: 'high-contrast',
	[ProjectDeploymentStatus.DEPLOYMENT_COMPLETED]: 'teal',
	[ProjectDeploymentStatus.DEPLOYMENT_FAILED]: 'red',

	[ProjectDeploymentStatus.REQUESTED_DELETION]: 'red',
	[ProjectDeploymentStatus.DELETING_DEPLOYMENT]: 'red',
	[ProjectDeploymentStatus.COMPLETED_DELETION]: 'red'
};
</script>

{#if status}
	<Tag type={colorMappping[status]} class="my-0"
		>{toTitleCase(status.toString().replaceAll('_', ' '))}</Tag
	>
{/if}
