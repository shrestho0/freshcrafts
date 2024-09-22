<script lang="ts">
	import {
		InternalDeploymentActions,
		InternalProjectSetupCommand,
	} from "@/types/enums";
	import ConfirmationModal from "./ConfirmationModal.svelte";
	import { InlineLoading } from "carbon-components-svelte";
	import { EnvVarsUtil } from "@/utils/utils";
	import { goto, invalidate, invalidateAll } from "$app/navigation";

	// dileo chole na dileo chole
	export let loadingMessage: any = "";
	export let successMessage: any = "";
	export let errorMessage: any = "";
	export let actionLoading: any = false;
	export let confirmationModalOpen: any = false;

	export let action: InternalDeploymentActions;
	export let submitBtnDisabled: any;
	export let projectX: any;
	export let currentDeployment: any;
	export let activeDeployment: any;
	export let setup: any;

	async function deleteIncompleteProject() {
		console.log(
			"deleteIncompleteProject",
			currentDeployment,
			activeDeployment,
		);

		actionLoading = true;
		loadingMessage = "Requested incomplete deployment delete";

		const deploymentId =
			currentDeployment?.id ?? activeDeployment?.id ?? null;
		console.log("deploymentId", deploymentId);

		if (!deploymentId) {
			errorMessage = "Deployment ID not found";
			actionLoading = false;
			return;
		}

		const res = await fetch("/projects", {
			method: "PATCH",
			headers: {
				"Content-Type": "application/json",
				"x-freshcrafts-project-setup-command":
					InternalProjectSetupCommand.DELETE_INCOMPLETE_PROJECT,
			},
			body: JSON.stringify({
				projectId: projectX?.id,
				deploymentId,
				rawFileAbsPath: currentDeployment?.rawFile?.absPath,
				srcFileAbsPath: currentDeployment?.src?.filesDirAbsPath,
			}),
		})
			.then((res) => res.json())
			.catch((e) => console.error(e));

		actionLoading = false;
		loadingMessage = "";
		if (res?.success) {
			successMessage = res?.message + " Page will be redirected soon.";
			backToProjectCreationPage();
		} else {
			errorMessage = res?.message;
		}
	}

	async function deployProject() {
		let envContentFinal = "";
		actionLoading = true;
		confirmationModalOpen = false;
		loadingMessage = "Requested incomplete deployment creation.";

		if (setup.selectedOptionIdx === 0) {
			envContentFinal = setup.envFileContent;
		} else {
			envContentFinal = EnvVarsUtil.kvToContent(setup.envKV);
		}

		console.log(setup.projectName);

		const res = await fetch("/projects", {
			method: "PATCH",
			headers: {
				"Content-Type": "application/json",
				"x-freshcrafts-project-setup-command":
					action == InternalDeploymentActions.INITIAL_SETUP
						? InternalProjectSetupCommand.DEPLOY_PROJECT
						: InternalProjectSetupCommand.RE_DEPLOY_PROJECT,
			},
			body: JSON.stringify({
				projectId: projectX?.id,
				projectName: setup.projectName,
				buildCommand: setup.buildCommand,
				installCommand: setup.installCommand,
				postInstall: setup.postInstall,
				outputDir: setup.outputDir,
				selectedFileRelativeUrl: setup.selectedFileRelativeUrl,
				envFileContent: envContentFinal,
				deployment: currentDeployment,
				version: currentDeployment?.version,
			}),
		})
			.then((res) => res.json())
			.catch((e) => console.error(e));

		console.log("create project res", res);
		actionLoading = false;
		loadingMessage = "";
		if (res?.success) {
			successMessage = res?.message;
			console.log("Project created successfully");
			// invalidateAll();
			goto(`/projects/${projectX?.id}/processing`);
			// scroll to top
			// window.scrollTo(0, 0);
		} else {
			errorMessage = res?.message;
			// console.error(res?.message);
		}
	}

	async function updateDeployment() {
		let envContentFinal = "";
		actionLoading = true;
		confirmationModalOpen = false;
		loadingMessage = "Requested deployment update.";

		if (setup.selectedOptionIdx === 0) {
			envContentFinal = setup.envFileContent;
		} else {
			envContentFinal = EnvVarsUtil.kvToContent(setup.envKV);
		}

		console.log(setup.projectName);

		const res = await fetch("/projects", {
			method: "PATCH",
			headers: {
				"Content-Type": "application/json",
				"x-freshcrafts-project-setup-command":
					InternalDeploymentActions.UPDATE_DEPLOYMENT,
			},
			body: JSON.stringify({
				projectId: projectX?.id,
				projectName: setup.projectName,
				buildCommand: setup.buildCommand,
				installCommand: setup.installCommand,
				postInstall: setup.postInstall,
				outputDir: setup.outputDir,
				selectedFileRelativeUrl: setup.selectedFileRelativeUrl,
				envFileContent: envContentFinal,
				deployment: activeDeployment,
				version: activeDeployment?.version,
			}),
		})
			.then((res) => res.json())
			.catch((e) => console.error(e));

		console.log("create project res", res);
		actionLoading = false;
		loadingMessage = "";
		if (res?.success) {
			successMessage = res?.message;
			console.log("Project update requested", res);
			// invalidateAll();

			goto(`/projects/${projectX?.id}/processing`);
			// scroll to top
			// window.scrollTo(0, 0);
		} else {
			errorMessage = res?.message;
			// console.error(res?.message);
		}
	}

	async function initiateRollforward() {
		let envContentFinal = "";
		actionLoading = true;
		confirmationModalOpen = false;
		loadingMessage = "Requested deployment update.";

		if (setup.selectedOptionIdx === 0) {
			envContentFinal = setup.envFileContent;
		} else {
			envContentFinal = EnvVarsUtil.kvToContent(setup.envKV);
		}

		// console.log(setup.projectName);

		const res = await fetch("/projects", {
			method: "PATCH",
			headers: {
				"Content-Type": "application/json",
				"x-freshcrafts-project-setup-command":
					InternalDeploymentActions.ROLLFORWARD,
			},
			body: JSON.stringify({
				projectId: projectX?.id,
				projectName: setup.projectName,
				buildCommand: setup.buildCommand,
				installCommand: setup.installCommand,
				postInstall: setup.postInstall,
				outputDir: setup.outputDir,
				selectedFileRelativeUrl: setup.selectedFileRelativeUrl,
				envFileContent: envContentFinal,
				deployment: currentDeployment,
				version: currentDeployment?.version, // doesn't matter here
			}),
		})
			.then((res) => res.json())
			.catch((e) => console.error(e));

		console.log("create project res", res);
		actionLoading = false;
		loadingMessage = "";

		if (res?.success) {
			successMessage = res?.message;
			console.log("Project update requested", res);
			// invalidateAll();

			goto(`/projects/${projectX?.id}/processing`);
			// scroll to top
			// window.scrollTo(0, 0);
		} else {
			errorMessage = res?.message;
			// console.error(res?.message);
		}
	}

	function backToProjectCreationPage() {
		setTimeout(() => {
			window.location.href = "/projects/all";
		}, 1000);
	}

	function backToProjectSpecificPage() {
		console.log("backToProjectSpecificPage", projectX?.id);
		setTimeout(() => {
			window.location.href = "/projects/" + projectX?.id;
		}, 500);
	}

	let btn_common_class = `py-4 my-6 w-full `;
	let left_btn_class = `bx--btn--danger-tertiary
disabled:bg-[var(--cds-disabled-02)] disabled:text-[var(--cds-disabled-03)]
disabled:hover:bg-[var(--cds-disabled-02)] disabled:hover:text-[var(--cds-disabled-03)]
disabled:border-[var(--cds-disabled-02)]`;
	let right_btn_class = `bx--btn--primary
        disabled:bg-[var(--cds-disabled-02)] disabled:text-[var(--cds-disabled-03)]
        disabled:hover:bg-[var(--cds-disabled-02)] disabled:hover:text-[var(--cds-disabled-03)]
        disabled:cursor-not-allowed`;
</script>

{action}

{#if actionLoading}
	<InlineLoading description={actionLoading ? loadingMessage : ""} />
{:else if successMessage}
	<InlineLoading status="finished" description={successMessage} />
{:else if errorMessage}
	<InlineLoading status="error" description={errorMessage} />
{/if}

<div class="dep grid grid-cols-3 gap-2">
	<div class="w-full btn-left col-span-1">
		{#if action == InternalDeploymentActions.INITIAL_SETUP}
			<button
				class=" {btn_common_class} {left_btn_class}"
				disabled={actionLoading}
				on:click={deleteIncompleteProject}>Cancel Project</button
			>
		{:else if action == InternalDeploymentActions.RE_DEPLOY || action == InternalDeploymentActions.UPDATE_DEPLOYMENT || InternalDeploymentActions.ROLLFORWARD}
			<button
				class="{btn_common_class} {left_btn_class}"
				disabled={actionLoading}
				on:click={backToProjectSpecificPage}>Back to project</button
			>
		{/if}
	</div>
	<div class="w-full btn-right col-span-2">
		<!-- something -->
		{#if action == InternalDeploymentActions.INITIAL_SETUP || action == InternalDeploymentActions.RE_DEPLOY}
			<button
				disabled={submitBtnDisabled || actionLoading}
				on:click={() => {
					confirmationModalOpen = true;
				}}
				class="{right_btn_class} {btn_common_class}"
				>{action == InternalDeploymentActions.INITIAL_SETUP
					? "Check & Deploy"
					: "Check & Redeploy"}</button
			>
		{:else if action == InternalDeploymentActions.UPDATE_DEPLOYMENT}
			<button
				disabled={submitBtnDisabled || actionLoading}
				on:click={updateDeployment}
				class="{right_btn_class} {btn_common_class}"
				>Update Deployment</button
			>
		{:else if action == InternalDeploymentActions.ROLLFORWARD}
			<button
				disabled={submitBtnDisabled || actionLoading}
				on:click={initiateRollforward}
				class="{right_btn_class} {btn_common_class}"
				>Initiate Rollforward</button
			>
		{/if}
	</div>
</div>

<ConfirmationModal bind:confirmationModalOpen bind:setup {deployProject} />
