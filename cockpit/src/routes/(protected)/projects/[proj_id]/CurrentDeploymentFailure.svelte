<script lang="ts">
	import CommonErrorBox from "@/components/CommonErrorBox.svelte";
	import type { Project, ProjectDeployment } from "@/types/entities";
	import { Button, CodeSnippet } from "carbon-components-svelte";

	import { InternalDeploymentActions } from "@/types/enums";
	import Rollback from "@/ui/icons/Rollback.svelte";
	import ServerCrash from "@/ui/icons/ServerCrash.svelte";
	import X from "@/ui/icons/X.svelte";
	import DeploymentProcessingHistory from "./DeploymentProcessingHistory.svelte";

	export let project: Project;
	export let currentDeployment: ProjectDeployment | undefined;
</script>

<CommonErrorBox
	error_msg={project.totalVersions > 1
		? "Deployment failed"
		: "Initial deployment failed"}
>
	<DeploymentProcessingHistory messages={project.partialMessageList ?? []} />
	<div class="flex gap-3 py-4 w-full justify-center">
		<div></div>

		{#if project.totalVersions <= 1}
			<Button
				kind="secondary"
				href="./{project.id}/{InternalDeploymentActions.DELETE_PROJECT}"
				class="flex gap-2 items-center rounded"
			>
				<X />
				<p>Delete Project</p>
			</Button>
		{:else}
			<Button
				kind="secondary"
				href="./{project.id}/{InternalDeploymentActions.ROLLBACK}"
				class="flex gap-2 items-center rounded"
			>
				<Rollback />
				<p>Rollback</p>
			</Button>
		{/if}

		<Button
			href="./{project.id}/{InternalDeploymentActions.RE_DEPLOY}"
			class="flex gap-2 items-center rounded"
		>
			<ServerCrash />
			<p>Re-deploy</p>
		</Button>
	</div>
	<!-- show error traceback -->
	{#if currentDeployment?.errorTraceback}
		<!-- <pre class="">{currentDeployment.errorTraceback}</pre> -->
		<CodeSnippet type="multi" light>
			{currentDeployment.errorTraceback}
		</CodeSnippet>
	{:else}
		<p class="text-center">No error traceback available</p>
	{/if}
</CommonErrorBox>
