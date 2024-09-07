<script lang="ts">
import { page } from '$app/stores';
import PreDebug from '@/components/dev/PreDebug.svelte';
import type { Project, ProjectDeployment } from '@/types/entities';
import { InternalDeploymentActions } from '@/types/enums';

export let project: Project;
export let currentDeployment: ProjectDeployment | undefined;
export let activeDeployment: ProjectDeployment | undefined;
</script>

Active bara
<a href={`${$page.url}/${InternalDeploymentActions.DELETE_PROJECT}`}> DELETE_PROJECT </a>

<PreDebug data={project} title="proj" />
<PreDebug data={currentDeployment} title="current " />
<PreDebug data={activeDeployment} title="active " />
