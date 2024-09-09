<script lang="ts">
	import { InternalProjectSetupCommand } from "@/types/enums";
	import { EnvVarsUtil } from "@/utils/utils";
	import {
		ComposedModal,
		ModalHeader,
		ModalBody,
		ModalFooter,
	} from "carbon-components-svelte";

	export let confirmationModalOpen: boolean;
	export let setup: any;
	// func
	export let deployProject: any;
</script>

<ComposedModal bind:open={confirmationModalOpen} preventCloseOnClickOutside>
	<ModalHeader title="Confirm Deployment" closeClass="hidden" />
	<ModalBody>
		<div class="flex flex-col gap-3">
			<p class="text-lg">
				You are about to deploy a new version of the project. Please
				confirm the details below.
			</p>
			<div class="flex flex-col gap-2">
				<div
					class="flex items
                    justify-between"
				>
					<p>Project Name</p>
					<p>{setup.projectName}</p>
				</div>
				<div
					class="flex items
                    justify-between"
				>
					<p>Build Command</p>
					<p>{setup.buildCommand}</p>
				</div>
				<div
					class="flex items
                    justify-between"
				>
					<p>Install Command</p>
					<p>{setup.installCommand}</p>
				</div>

				<div
					class="flex items
                justify-between"
				>
					<p>Post Install Command</p>
					<p>{setup.postInstall}</p>
				</div>

				<div
					class="flex items
                    justify-between"
				>
					<p>Output Directory</p>
					<p>{setup.outputDir}</p>
				</div>
				<div
					class="flex items
                    justify-between"
				>
					<p>Project Root</p>
					<p>./{setup.selectedFileRelativeUrl}</p>
				</div>
				<div
					class="flex items
                    justify-between"
				>
					<p>Environment Variables</p>
				</div>
				<div
					class="w-full {setup.envFileContent
						? 'h-60 overflow-y-scroll'
						: ''}"
				>
					<p>{setup.envFileContent}</p>
				</div>
			</div>
		</div>
	</ModalBody>
	<ModalFooter>
		<button
			class="bx--btn bx--btn--secondary"
			on:click={() => {
				confirmationModalOpen = false;
			}}
		>
			Back to initial setup
		</button>
		<button class="bx--btn bx--btn--primary" on:click={deployProject}>
			Deploy
		</button>
	</ModalFooter>
</ComposedModal>
