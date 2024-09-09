<script lang="ts">
	import type { Project, ProjectDeployment } from "@/types/entities";
	import { ProjectType } from "@/types/enums";
	import CarbonBranch from "@/ui/icons/CarbonBranch.svelte";
	import CarbonDocumentMultiple1 from "@/ui/icons/CarbonDocumentMultiple1.svelte";
	import CarbonLogoGithub from "@/ui/icons/CarbonLogoGithub.svelte";
	import Files from "@/ui/icons/Files.svelte";
	import GitBranch from "@/ui/icons/GitBranch.svelte";
	import Github from "@/ui/icons/Github.svelte";
	import { toTitleCase } from "@/utils/utils";
	import {
		StructuredList,
		StructuredListBody,
		StructuredListCell,
		StructuredListHead,
		StructuredListRow,
		Tag,
		Tile,
	} from "carbon-components-svelte";
	import ProjectStatusTag from "./ProjectStatusTag.svelte";
	import ProjectDeploymentStatusTag from "./ProjectDeploymentStatusTag.svelte";
	import SquareArrowOutUpRight from "@/ui/icons/SquareArrowOutUpRight.svelte";

	export let project: Project;
	export let currentDeployment: ProjectDeployment | undefined;
	export let activeDeployment: ProjectDeployment | undefined;
</script>

<StructuredList class="max-w-xl" condensed>
	<!-- <StructuredListHead>
		<StructuredListRow head>
			<StructuredListCell head>Column A</StructuredListCell>
			<StructuredListCell head>Column B</StructuredListCell>
			<StructuredListCell head>Column C</StructuredListCell>
		</StructuredListRow>
		-->

	<StructuredListBody>
		<StructuredListRow>
			<StructuredListCell head>Project</StructuredListCell>
			<StructuredListCell class="flex items-center gap-2">
				{#if project.domain}
					<a
						href="//{project?.domain}"
						target="_blank"
						class="hover:text-[var(--cds-link-02)] hover:cursor-pointer flex items-center gap-1"
						>{project.uniqueName}
						<SquareArrowOutUpRight class="h-4 w-4" />
					</a>
				{:else}
					{project.uniqueName}
				{/if}
				<ProjectStatusTag status={project.status} />
			</StructuredListCell>
		</StructuredListRow>
		<StructuredListRow>
			<StructuredListCell head>Current Deployment</StructuredListCell>
			<StructuredListCell class="flex items-center gap-2">
				{#if currentDeployment}
					<!-- <div>Version: {currentDeployment?.version}</div> -->
					<!-- <pre>/</pre> -->
					<!-- <div>Deployed: {toTitleCase(currentDeployment?.isDeployed.toString())}</div> -->
					Version: {currentDeployment?.version}
					Iterations: {currentDeployment?.iteration}
					<ProjectDeploymentStatusTag
						status={currentDeployment.status}
					/>
				{:else}
					No current/processing deployment
				{/if}
			</StructuredListCell>
		</StructuredListRow>

		<StructuredListRow>
			<StructuredListCell head>Active Deployment</StructuredListCell>
			<StructuredListCell class="flex items-center gap-2">
				{#if activeDeployment}
					<!-- <div>Version: {activeDeployment?.version}</div>
					<pre>/</pre>
					<div>Deployed: {toTitleCase(activeDeployment?.isDeployed.toString())}</div> -->
					Version: {activeDeployment?.version}
					Iterations: {activeDeployment?.iteration}
					<ProjectDeploymentStatusTag
						status={activeDeployment.status}
					/>
				{:else}
					No active deployment
				{/if}
			</StructuredListCell>
		</StructuredListRow>

		<StructuredListRow>
			<StructuredListCell head>URL</StructuredListCell>
			<StructuredListCell
				><a
					href="//{project?.domain}"
					target="_blank"
					class=" hover:text-[var(--cds-link-02)] hover:cursor-pointer"
					>{project?.domain}</a
				></StructuredListCell
			>
		</StructuredListRow>
		<StructuredListRow>
			<StructuredListCell head>Total Deployments</StructuredListCell>
			<StructuredListCell>{project.totalVersions}</StructuredListCell>
		</StructuredListRow>

		<StructuredListRow>
			<StructuredListCell head>Project Source</StructuredListCell>
			<StructuredListCell class="flex gap-2 items-center">
				{#if project.type == ProjectType.LOCAL_FILES}
					<CarbonDocumentMultiple1 class="h-5 w-5" />
				{:else}
					<CarbonLogoGithub class="h-5 w-5" />
				{/if}
				{toTitleCase(project.type.toString().replace("_", " "))}
			</StructuredListCell>
		</StructuredListRow>
		{#if project.type === ProjectType.GITHUB_REPO}
			<StructuredListRow>
				<StructuredListCell head>Github Info:</StructuredListCell>
				<StructuredListCell class="flex gap-3">
					<a
						href="https://github.com/{project?.githubRepo
							?.fullName}"
						target="_blank"
						class="hover:text-blue-500 flex gap-2"
					>
						<CarbonLogoGithub class="h-5 w-5" />

						{project?.githubRepo?.fullName}
					</a>
					<a
						href="https://github.com/{project?.githubRepo
							?.fullName}/tree/{project?.githubRepo
							?.defaultBranch}"
						target="_blank"
						class="hover:text-blue-500 flex gap-2"
					>
						<CarbonBranch class="h-5 w-5" />

						{project?.githubRepo?.defaultBranch}
					</a>
				</StructuredListCell>
			</StructuredListRow>
		{/if}
	</StructuredListBody>
</StructuredList>
