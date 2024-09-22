<script lang="ts">
	import CommonErrorBox from "@/components/CommonErrorBox.svelte";
	import CommonLoadingBox from "@/components/CommonLoadingBox.svelte";
	import ExpandableSection from "@/components/ExpandableSection.svelte";
	import { Tile } from "carbon-components-svelte";
	import FileTree from "./files/FileTree.svelte";
	import { onMount } from "svelte";
	import { InternalProjectSetupCommand } from "@/types/enums";
	import type { ProjectDeployment } from "@/types/entities";

	let expanded = true;

	export let filesLoading: any;
	export let projectSourceList: any;
	export let projectRootSelectionStatus: any;
	export let selectedFileRelativeUrl: any;
	export let projectRootSelectionErrorMessage: any;
	// export let projectSourceTreeTotalLevels: any;
	// export let projectSourceTreeFinalIotaVal: any;

	// export let currentDeployment: ProjectDeployment;
	// export let activeDeployment: ProjectDeployment;

	export let dep: ProjectDeployment | null | undefined;

	onMount(async () => {
		// setTimeout(async () => {
		await listSourceFile();
		// }, 1000);
	});

	async function listSourceFile() {
		projectRootSelectionStatus = "checking";
		let src = dep?.src ?? "";
		if (!src) {
			projectRootSelectionErrorMessage = "No source files found";
			projectRootSelectionStatus = "error";
			return;
		}
		const res = await fetch("/projects", {
			method: "PATCH",
			headers: {
				"Content-Type": "application/json",
				"x-freshcrafts-project-setup-command":
					InternalProjectSetupCommand.LIST_SOURCE_FILES,
			},
			body: JSON.stringify({ src }),
		})
			.then((res) => res.json())
			.catch((e) => console.error(e));

		filesLoading = false;

		if (res?.success) {
			projectSourceList = res?.files;
			projectRootSelectionStatus = "success";
			// projectSourceTreeTotalLevels = res?.total_levels;
			// projectSourceTreeFinalIotaVal = res?.iota;
		} else {
			console.error(res?.message);
			projectRootSelectionErrorMessage =
				res?.message || "Error fetching source files";
			projectRootSelectionStatus = "error";
			// projectNameErrorMessage = res?.message || 'Error fetching source files';
		}
	}
</script>

<div class=" select-none">
	selectedFileRelativeUrl: {selectedFileRelativeUrl}
	<ExpandableSection bind:open={expanded} title="Select Project Root">
		<Tile>
			{#if projectRootSelectionStatus == "checking"}
				<CommonLoadingBox message="Loading project" />
			{:else if projectRootSelectionStatus === "error"}
				<CommonErrorBox error_msg={projectRootSelectionErrorMessage}>
					<p>Something went wrong while fetching source files</p>
				</CommonErrorBox>
			{:else if projectRootSelectionStatus == "success"}
				<FileTree
					bind:items={projectSourceList}
					bind:selectedFileRelativeUrl
				/>
			{:else}
				<div>Something went wrong while</div>
			{/if}
		</Tile>
	</ExpandableSection>
</div>
