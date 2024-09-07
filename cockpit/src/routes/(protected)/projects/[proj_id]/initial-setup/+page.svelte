<script lang="ts">
export let data;
import CommonErrorBox from '@/components/CommonErrorBox.svelte';
import CommonWarningBox from '@/components/project-specifics/ProjectSpecificWarningBox.svelte';
import PreDebug from '@/components/dev/PreDebug.svelte';
import ProjectName from '@/components/project-specifics/ProjectName.svelte';
import type { Project, ProjectDeployment } from '@/types/entities.js';
import { InternalDeploymentActions, ProjectStatus } from '@/types/enums';
import CarbonArrowLeft from '@/ui/icons/CarbonArrowLeft.svelte';
import ProjectFileSelection from '@/components/project-specifics/ProjectFileSelection.svelte';
import ProjectBuildAndOutput from '@/components/project-specifics/ProjectBuildAndOutput.svelte';
import ProjectEnvVars from '@/components/project-specifics/ProjectEnvVars.svelte';
import { InlineLoading } from 'carbon-components-svelte';
import ActionsButtons from '@/components/project-specifics/ActionsButtons.svelte';
import { browser } from '$app/environment';

const project: Project = data.project!;
const currentDeployment: ProjectDeployment = data.currentDeployment!;

let actionLoading = false;
let loadingMessage = '';
let successMessage = '';
let errorMessage = '';

let setup = {
	// Name related
	projectNameStatus: 'initially_idle',
	projectName: '',
	projectNameErrorMessage: '',

	// files related
	filesLoading: true,
	projectSourceList: [],
	projectRootSelectionStatus: 'initially_idle',
	selectedFileRelativeUrl: '',
	projectRootSelectionErrorMessage: '',

	// build related
	buildCommand: 'npm run build',
	installCommand: 'npm install',
	postInstall: '',
	outputDir: './build',

	// env related
	envFileContent: '',
	envKV: [],
	options: ['From .env file', 'Key-Value Pairs'],
	selectedOptionIdx: 0
} as {
	projectName: string;
	projectNameStatus: 'initially_idle' | 'checking' | 'invalid' | 'ok';
	projectNameErrorMessage: string;

	filesLoading: boolean;
	projectSourceList: any[];
	projectRootSelectionStatus: 'initially_idle' | 'checking' | 'success' | 'error';
	selectedFileRelativeUrl: string;
	projectRootSelectionErrorMessage: string;
	// projectSourceTreeActiveId: any;
	// projectSourceTreeSelectedIds: any;
	// projectSourceTreeTotalLevels: number;
	// projectSourceTreeFinalIotaVal: number;

	buildCommand: string;
	installCommand: string;
	postInstall: string;
	outputDir: string;

	envFileContent: string;
	envKV: { key: string; value: string }[];
	options: string[];
	selectedOptionIdx: number;
};

$: submitBtnDisabled = (() => {
	if (!browser) return true;

	if (setup.projectNameStatus !== 'ok') return true;
	if (!setup.projectName) return true;
	// if (!setup.buildCommand) return true;
	if (!setup.installCommand) return true;
	if (!setup.outputDir) return true;
	if (!setup.selectedFileRelativeUrl) return true;
	return false;
})();
</script>

{#if project.status != ProjectStatus.AWAIT_INITIAL_SETUP}
	<CommonWarningBox
		msg="Initial setup is already complete for this project"
		actionUrl="/projects/{project.id}"
		actionText="Back to project page"
		iconLeft={CarbonArrowLeft}
	/>
{:else}
	<section class=" flex flex-col gap-3">
		<ProjectName
			bind:projectName={setup.projectName}
			bind:projectNameStatus={setup.projectNameStatus}
			bind:projectNameErrorMessage={setup.projectNameErrorMessage}
		/>
		<ProjectFileSelection
			bind:filesLoading={setup.filesLoading}
			bind:projectSourceList={setup.projectSourceList}
			bind:projectRootSelectionStatus={setup.projectRootSelectionStatus}
			bind:selectedFileRelativeUrl={setup.selectedFileRelativeUrl}
			bind:projectRootSelectionErrorMessage={setup.projectRootSelectionErrorMessage}
			dep={currentDeployment}
		/>

		<ProjectBuildAndOutput
			bind:buildCommand={setup.buildCommand}
			bind:installCommand={setup.installCommand}
			bind:postInstall={setup.postInstall}
			bind:outputDir={setup.outputDir}
		/>

		<ProjectEnvVars
			bind:options={setup.options}
			bind:selectedOptionIdx={setup.selectedOptionIdx}
			bind:envFileContent={setup.envFileContent}
			bind:envKV={setup.envKV}
		/>

		<ActionsButtons
			action={InternalDeploymentActions.INITIAL_SETUP}
			bind:loadingMessage
			bind:successMessage
			bind:errorMessage
			bind:actionLoading
			bind:submitBtnDisabled
			projectX={project}
			{currentDeployment}
			activeDeployment={undefined}
			bind:setup
		/>
	</section>
{/if}

<PreDebug data={project} title="Project" />
<PreDebug data={currentDeployment} title="Current Deployment" />
<PreDebug {data} />
