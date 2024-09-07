<script lang="ts">
import type { Project } from '@/types/entities';
import { ProjectStatus, ProjectType } from '@/types/enums';
import { toTitleCase } from '@/utils/utils';
import { Tag } from 'carbon-components-svelte';

export let status: ProjectStatus;
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

const colorMappping: Record<ProjectStatus, tagType> = {
	[ProjectStatus.ACTIVE]: 'teal',
	[ProjectStatus.INACTIVE]: 'magenta',
	[ProjectStatus.AWAIT_INITIAL_SETUP]: 'blue',
	[ProjectStatus.PROCESSING_SETUP]: 'high-contrast',
	[ProjectStatus.PROCESSING_DEPLOYMENT]: 'high-contrast',
	[ProjectStatus.PROCESSING_UPDATE]: 'high-contrast',
	[ProjectStatus.PROCESSING_ROLLBACK]: 'high-contrast',
	[ProjectStatus.PROCESSING_REDEPLOYMENT]: 'high-contrast',
	[ProjectStatus.PROCESSING_DELETION]: 'high-contrast'
};
</script>

{#if status}
	<Tag type={colorMappping[status]} class="my-0"
		>{toTitleCase(status.toString().replaceAll('_', ' '))}</Tag
	>
{/if}
