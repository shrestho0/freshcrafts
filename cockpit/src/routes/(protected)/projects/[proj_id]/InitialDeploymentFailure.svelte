<script lang="ts">
import CommonErrorBox from '@/components/CommonErrorBox.svelte';
import type { Project, ProjectDeployment } from '@/types/entities';
import { Button, CodeSnippet } from 'carbon-components-svelte';
import { ServerCrash, X } from 'lucide-svelte';

export let project: Project;
export let currentDeployment: ProjectDeployment;
let show_delete_modal = false;
</script>

<CommonErrorBox error_msg="Initial deployment failed">
	<div class="flex gap-3 py-4 w-full justify-center">
		<div></div>

		<Button
			kind="secondary"
			on:click={() => {
				show_delete_modal = true;
			}}
			class="flex gap-2 items-center rounded"
		>
			<X />
			<p>Delete Deployment</p>
		</Button>

		<Button href="./{project.id}/re-deploy" class="flex gap-2 items-center rounded">
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
	{/if}
</CommonErrorBox>
