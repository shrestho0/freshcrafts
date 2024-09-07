<script lang="ts">
import CommonErrorBox from '@/components/CommonErrorBox.svelte';
import type { Project, ProjectDeployment } from '@/types/entities';
import { Button, CodeSnippet } from 'carbon-components-svelte';
import DeploymentProcessing from './DeploymentProcessing.svelte';
import { InternalDeploymentActions } from '@/types/enums';
import Rollback from '@/ui/icons/Rollback.svelte';
import ServerCrash from '@/ui/icons/ServerCrash.svelte';
import X from '@/ui/icons/X.svelte';

export let project: Project;
export let currentDeployment: ProjectDeployment | undefined;
</script>

<CommonErrorBox error_msg="Initial deployment failed">
	<DeploymentProcessing messages={project?.partialMessageList ?? []} sse={false} />
	<div class="flex gap-3 py-4 w-full justify-center">
		<div></div>

		{#if project.totalVersions == 0}
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
<!-- 
<ComposedModal preventCloseOnClickOutside bind:open={show_delete_modal}>
	<ModalHeader closeClass="hidden">Are you sure you want to delete this deployment?</ModalHeader>
	<ModalBody>
		<p>
			Deleting this deployment will remove all the data associated with it. This action cannot be
			undone.
		</p>
		<div class="delete_progress">
			<p>Deleting deployment...</p>
		</div>
	</ModalBody>
	<ModalFooter>
		<div class="flex gap-2 justify-end">
			<Button kind="secondary" on:click={() => (show_delete_modal = false)}>Cancel</Button>
			<Button kind="danger" on:click={() => (show_delete_modal = false)}>Delete</Button>
		</div>
	</ModalFooter>
</ComposedModal> -->
