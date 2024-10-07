<script lang="ts">
    import { ProjectStatus, ProjectType } from "@/types/enums";
    import RollforwardLocalFile from "./RollforwardLocalFile.svelte";
    import { Button } from "carbon-components-svelte";
    import ProjectSpecificWarningBox from "@/components/project-specifics/ProjectSpecificWarningBox.svelte";
    import CarbonArrowLeft from "@/ui/icons/CarbonArrowLeft.svelte";
    import RollforwardGithub from "./RollforwardGithub.svelte";
    import PreDebug from "@/components/dev/PreDebug.svelte";
    import type { EngineSystemConfigResponseDto } from "@/types/dtos";

    export let data;

    const project = data.project!;
    const currentDeployment = data.currentDeployment;
    const activeDeployment = data.activeDeployment;
    const envFileContent = data.envFileContent;
    const githubBranchInfoCurrent = data.githubBranchInfoCurrent;
    const sysConf: EngineSystemConfigResponseDto = data.sysConf!;
</script>

{#if project.status != ProjectStatus.ACTIVE}
    <ProjectSpecificWarningBox
        msg="Rollforward is only possible for active projects with active deployment"
        actionUrl="/projects/{project.id}"
        actionText="Back to project page"
        iconLeft={CarbonArrowLeft}
    />
{:else if project.type === ProjectType.LOCAL_FILES}
    <RollforwardLocalFile
        {project}
        {currentDeployment}
        {activeDeployment}
        {envFileContent}
    />
{:else if project.type === ProjectType.GITHUB_REPO}
    <RollforwardGithub
        {project}
        {currentDeployment}
        {activeDeployment}
        {envFileContent}
        {githubBranchInfoCurrent}
        {sysConf}
    />
{/if}
