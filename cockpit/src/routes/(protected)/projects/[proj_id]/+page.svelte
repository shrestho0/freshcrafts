<script lang="ts">
	import { page } from "$app/stores";
	import PreDebug from "@/components/dev/PreDebug.svelte";
	import ProjectSpecificWarningBox from "@/components/project-specifics/ProjectSpecificWarningBox.svelte";
	import type { Project, ProjectDeployment } from "@/types/entities.js";
	import { InternalDeploymentActions, ProjectStatus } from "@/types/enums";
	import CarbonArrowRight from "@/ui/icons/CarbonArrowRight.svelte";
	import DeploymentProcessing from "./DeploymentProcessingProgress.svelte";
	import CurrentDeploymentFailure from "./CurrentDeploymentFailure.svelte";
	import ActiveDeployment from "./ActiveDeployment.svelte";
	import { invalidateAll } from "$app/navigation";
	import DeploymentProcessingProgress from "./DeploymentProcessingProgress.svelte";
	import { onMount } from "svelte";

	export let data;
	const project: Project = data.project!;
	const currentDeployment: ProjectDeployment | undefined =
		data.currentDeployment;
	const activeDeployment: ProjectDeployment | undefined =
		data.activeDeployment;

	onMount(() => {
		const inv = $page.url.searchParams.get("invalidate") ?? "false";
		if (JSON.parse(inv)) {
			invalidateAll();
		}
	});
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
{:else if project.status.toString().startsWith("PROCESSING")}
	<ProjectSpecificWarningBox
		msg="Deployment processing in progress"
		actionUrl={`${$page.url}/processing`}
		actionText="View processing status"
		iconRight={CarbonArrowRight}
	/>
{/if}
<!-- <pre>//////////////////////////////////////////////////////////////////////////////////////////////////////////</pre>
<pre>//////////////////////////////////////////////////////////////////////////////////////////////////////////</pre> -->
<!-- <PreDebug {data} /> -->
