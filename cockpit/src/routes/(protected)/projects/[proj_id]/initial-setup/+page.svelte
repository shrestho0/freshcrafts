<script lang="ts">
import PreDebug from '@/components/dev/PreDebug.svelte';
import type { Project, ProjectDeployment } from '@/types/entities.js';
import { ProjectSetupCommand, ProjectType } from '@/types/enums';
import { EnvVarsUtil, toTitleCase } from '@/utils/utils';
import {
	Button,
	ComposedModal,
	InlineLoading,
	ModalBody,
	ModalFooter,
	ModalHeader,
	TextArea,
	TextInput,
	Tile
} from 'carbon-components-svelte';
import { GitBranch, Github, Slash } from 'lucide-svelte';
import { onMount } from 'svelte';
import FileTree from '../FileTree.svelte';
import BuildSettingsInputs from '../BuildSettingsInputs.svelte';
import CommonErrorBox from '@/components/CommonErrorBox.svelte';
import ExpandableSection from '@/components/ExpandableSection.svelte';
import CommonLoadingBox from '@/components/CommonLoadingBox.svelte';
import EnvVarsInputs from '../EnvVarsInputs.svelte';
import { enhance } from '$app/forms';
import type { ActionResult } from '@sveltejs/kit';
import { toast } from 'svelte-sonner';
import { invalidateAll } from '$app/navigation';
import type { EngineCommonResponseDto } from '@/types/dtos';
import { browser } from '$app/environment';
export let data: EngineCommonResponseDto<Project, null, null, ProjectDeployment>;
let loading = true;
const projectX: Project = data?.payload || {};
const deploymentX: ProjectDeployment = data?.payload3 || {};
const projectSetup = {
	projectName: '',
	projectNameStatus: 'initially_idle',
	projectNameErrorMessage: '',
	envFileContent: '',
	envKV: [],
	options: ['From .env file', 'Key-Value Pairs'],
	selectedOptionIdx: 0,
	buildCommand: 'npm run build',
	installCommand: 'npm install',
	postInstall: '',
	outputDir: './build',
	filesLoading: true,
	projectRootSelectionStatus: 'initially_idle',
	projectRootSelectionErrorMessage: '',
	projectSourceTreeActiveId: 0,
	projectSourceTreeSelectedIds: 0,
	projectSourceTreeTotalLevels: 0,
	projectSourceTreeFinalIotaVal: 0,
	projectSourceList: [],
	selectedFileRelativeUrl: ''
} as {
	projectName: string;
	projectNameStatus: 'initially_idle' | 'checking' | 'invalid' | 'ok';
	projectNameErrorMessage: string;
	envFileContent: string;
	envKV: { key: string; value: string }[];
	options: string[];
	selectedOptionIdx: number;
	buildCommand: string;
	installCommand: string;
	postInstall: string;
	filesLoading: boolean;
	outputDir: string;
	projectRootSelectionStatus: 'initially_idle' | 'checking' | 'success' | 'error';
	projectRootSelectionErrorMessage: string;
	projectSourceList: any[];
	projectSourceTreeActiveId: any;
	projectSourceTreeSelectedIds: any;
	projectSourceTreeTotalLevels: number;
	projectSourceTreeFinalIotaVal: number;
	selectedFileRelativeUrl: string;

	// runCommand: string;
};

const expandables = {
	env_vars: true,
	build_output: true,
	select_project_root: true
};

async function listSourceFile() {
	projectSetup.projectRootSelectionStatus = 'checking';
	const res = await fetch('', {
		method: 'PATCH',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({
			command: ProjectSetupCommand.LIST_SOURCE_FILES,
			data: deploymentX?.src
		})
	})
		.then((res) => res.json())
		.catch((e) => console.error(e));

	projectSetup.filesLoading = false;

	if (res?.success) {
		projectSetup.projectSourceList = res?.files;
		projectSetup.projectRootSelectionStatus = 'success';
		projectSetup.projectSourceTreeTotalLevels = res?.total_levels;
		projectSetup.projectSourceTreeFinalIotaVal = res?.iota;
	} else {
		console.error(res?.message);
		projectSetup.projectRootSelectionErrorMessage = res?.message || 'Error fetching source files';
		projectSetup.projectRootSelectionStatus = 'error';
		projectSetup.projectNameErrorMessage = res?.message || 'Error fetching source files';
	}
}

onMount(async () => {
	// setTimeout(async () => {
	await listSourceFile();
	loading = false;
	// }, 1000);
});

let timer: NodeJS.Timeout;
let _projectName = '';
const handleNameInput = (e: KeyboardEvent) => {
	const v = (e.target as HTMLInputElement).value;
	debouncedNameSearch(v);
};
const debouncedNameSearch = (v: string | null) => {
	projectSetup.projectNameStatus = 'checking';
	clearTimeout(timer);
	timer = setTimeout(async () => {
		// check project name and do stuff
		// _projectName = v;
		if (!v) {
			projectSetup.projectNameStatus = 'initially_idle';
			return;
		}
		const res = await fetch('', {
			method: 'PATCH',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({
				command: ProjectSetupCommand.CHECK_UNIQUE_NAME,
				data: v.trim()
			})
		})
			.then((res) => res.json())
			.catch((e) => console.error(e));
		if (res?.success) {
			projectSetup.projectNameStatus = 'ok';
			projectSetup.projectName = v;
		} else {
			projectSetup.projectNameStatus = 'invalid';
			projectSetup.projectNameErrorMessage = res?.message || 'Invalid project name';
		}

		console.warn('res', res);
	}, 750);
};

let confirmationModalOpen = false;
$: checkAndSubmitButtonDisabled = (() => {
	if (!browser) return true;
	if (projectSetup.projectNameStatus !== 'ok') return true;
	if (!projectSetup.buildCommand) return true;
	if (!projectSetup.installCommand) return true;
	if (!projectSetup.outputDir) return true;
	if (!projectSetup.selectedFileRelativeUrl) return true;
	return false;
})();

let actionLoading = false;
let loadingMessage = '';
let successMessage = '';
let errorMessage = '';

async function deployProject() {
	let envContentFinal = '';
	actionLoading = true;
	confirmationModalOpen = false;
	loadingMessage = 'Requested incomplete deployment creation.';
	if (projectSetup.selectedOptionIdx === 0) {
		envContentFinal = projectSetup.envFileContent;
	} else {
		envContentFinal = EnvVarsUtil.kvToContent(projectSetup.envKV);
	}

	console.log(projectSetup.projectName);

	const res = await fetch('', {
		method: 'PATCH',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({
			command: ProjectSetupCommand.DEPLOY_PROJECT,
			data: {
				projectName: projectSetup.projectName,
				buildCommand: projectSetup.buildCommand,
				installCommand: projectSetup.installCommand,
				postInstall: projectSetup.postInstall,
				outputDir: projectSetup.outputDir,
				selectedFileRelativeUrl: projectSetup.selectedFileRelativeUrl,
				envFileContent: envContentFinal,
				deployment: deploymentX,
				projectId: projectX?.id
			}
		})
	})
		.then((res) => res.json())
		.catch((e) => console.error(e));

	console.log('create project res', res);
	actionLoading = false;
	loadingMessage = '';
	if (res?.success) {
		successMessage = res?.message;
		console.log('Project created successfully');
		backToProjectSpecificPage();
	} else {
		errorMessage = res?.message;
		console.error(res?.message);
	}
}

async function deleteIncompleteProject() {
	actionLoading = true;
	loadingMessage = 'Requested incomplete deployment delete';

	const res = await fetch('', {
		method: 'PATCH',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({
			command: ProjectSetupCommand.DELETE_INCOMPLETE_PROJECT,
			data: {
				projectId: projectX?.id,
				deploymentId: deploymentX?.id,
				rawFileAbsPath: deploymentX?.rawFile?.absPath,
				srcFileAbsPath: deploymentX?.src?.filesDirAbsPath
			}
		})
	})
		.then((res) => res.json())
		.catch((e) => console.error(e));

	actionLoading = false;
	loadingMessage = '';
	if (res?.success) {
		successMessage = res?.message + ' Page will be redirected soon.';
		backToProjectCreationPage();
	} else {
		errorMessage = res?.message;
	}
}

function backToProjectCreationPage() {
	setTimeout(() => {
		window.location.href = '/projects/new';
	}, 1000);
}

function backToProjectSpecificPage() {
	setTimeout(() => {
		window.location.href = '';
	}, 1000);
}
</script>

{#if !loading}
	<Tile light class="px-0 mx-0 pt-0 mt-0">
		<h1 class="text-3xl py-2">Initial Setup</h1>
		<div class="flex items-center">
			<p>
				Project Source:
				<span class="">
					{toTitleCase(projectX?.type?.replace('_', ' ') || '')}
				</span>
			</p>

			<span class="px-2">/</span>
			<p>
				Deployment Version: <span class="">{(projectX?.totalVersions || 0) + 1}</span>
			</p>
		</div>
		{#if projectX?.type == ProjectType.GITHUB_REPO}
			<p class="flex items-center py-2">
				<Github class="h-5 w-5  " />
				<a
					href="https://github.com/{projectX?.githubRepo?.fullName}"
					target="_blank"
					class="hover:text-blue-500"
				>
					{projectX?.githubRepo?.fullName}
				</a>
				<span class="px-2">/</span>
				<GitBranch class="h-6 w-6  p-1" />
				<a
					href="https://github.com/{projectX?.githubRepo?.fullName}/tree/{projectX?.githubRepo
						?.defaultBranch}"
					target="_blank"
					class="hover:text-blue-500"
				>
					{projectX?.githubRepo?.defaultBranch}
				</a>
			</p>
		{/if}
	</Tile>

	<section class="">
		<!-- <Tile> -->
		<div>Project Name</div>
		<TextInput on:keyup={handleNameInput} placeholder="anonymous-monkey" />
		<div class="mb-3">
			{#if projectSetup.projectNameStatus != 'initially_idle'}
				<InlineLoading
					status={projectSetup.projectNameStatus === 'checking'
						? 'active'
						: projectSetup.projectNameStatus === 'ok'
							? 'finished'
							: projectSetup.projectNameStatus === 'invalid'
								? 'error'
								: 'inactive'}
					description={projectSetup.projectNameStatus === 'checking'
						? 'Checking...'
						: projectSetup.projectNameStatus === 'ok'
							? 'Available'
							: (projectSetup?.projectNameErrorMessage ?? '')}
				/>
			{/if}
		</div>

		<!-- 
-->
		<!-- 
	
	
	-->
		<div class="mb-3 select-none">
			<ExpandableSection bind:open={expandables.select_project_root} title="Select Project Root">
				<Tile>
					{#if projectSetup.projectRootSelectionStatus == 'checking'}
						<CommonLoadingBox message="Loading project" />
						<!-- {:else if projectSetup.projectRootSelectionStatus === 'error'}
						<CommonErrorBox error_msg={projectSetup.projectRootSelectionErrorMessage} /> -->
						<!-- 
		
						-->
					{:else if projectSetup.projectRootSelectionStatus == 'success'}
						<!-- <FileTree
							bind:items={projectSetup.projectSourceList}
							bind:selectedFileRelativeUrl={projectSetup.selectedFileRelativeUrl}
						/>
					{:else} -->
						<div>&nbsp;</div>
					{/if}
				</Tile>
			</ExpandableSection>
		</div>

		<div class="mb-3">
			<ExpandableSection bind:open={expandables.build_output} title="Build and output settings">
				<BuildSettingsInputs
					bind:buildCommand={projectSetup.buildCommand}
					bind:outputDir={projectSetup.outputDir}
					bind:installCommand={projectSetup.installCommand}
					bind:postInstall={projectSetup.postInstall}
				/>
			</ExpandableSection>
		</div>
		<div class="mb-3">
			<ExpandableSection bind:open={expandables.env_vars} title="Environment Variables">
				<Tile>
					<EnvVarsInputs
						bind:options={projectSetup.options}
						bind:selectedOptionIdx={projectSetup.selectedOptionIdx}
						bind:envFileContent={projectSetup.envFileContent}
						bind:envKV={projectSetup.envKV}
					/>
				</Tile>
			</ExpandableSection>
		</div>

		{#if actionLoading}
			<InlineLoading description={actionLoading ? loadingMessage : ''} />
		{:else if successMessage}
			<InlineLoading status="finished" description={successMessage} />
		{:else if errorMessage}
			<InlineLoading status="error" description={successMessage} />
		{/if}

		<div class="dep grid grid-cols-3 gap-2">
			<button
				class="py-4 col-span-1 my-6 w-full bx--btn--danger-tertiary
			disabled:bg-[var(--cds-disabled-02)] disabled:text-[var(--cds-disabled-03)]
			disabled:hover:bg-[var(--cds-disabled-02)] disabled:hover:text-[var(--cds-disabled-03)]
			disabled:border-[var(--cds-disabled-02)]
			"
				disabled={actionLoading}
				on:click={deleteIncompleteProject}>Cancel Project</button
			>
			<button
				disabled={checkAndSubmitButtonDisabled || actionLoading}
				on:click={() => {
					confirmationModalOpen = true;
				}}
				class="py-4 col-span-2 my-6 w-full bx--btn--primary
			disabled:bg-[var(--cds-disabled-02)] disabled:text-[var(--cds-disabled-03)]
			disabled:hover:bg-[var(--cds-disabled-02)] disabled:hover:text-[var(--cds-disabled-03)]
			disabled:cursor-not-allowed
			">Check & Deploy</button
			>
		</div>
	</section>
{:else}{/if}
<PreDebug {data} />
