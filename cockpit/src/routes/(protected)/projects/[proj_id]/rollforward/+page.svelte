<script lang="ts">
    import { ProjectStatus, ProjectType } from "@/types/enums";
    import RollbackLocalFile from "./RollbackLocalFile.svelte";
    import { Button } from "carbon-components-svelte";
    import ProjectSpecificWarningBox from "@/components/project-specifics/ProjectSpecificWarningBox.svelte";
    import CarbonArrowLeft from "@/ui/icons/CarbonArrowLeft.svelte";

    export let data;

    const project = data.project!;
    const currentDeployment = data.currentDeployment;
    const activeDeployment = data.activeDeployment;
    const envFileContent = data.envFileContent;
</script>

{#if project.status != ProjectStatus.ACTIVE}
    <ProjectSpecificWarningBox
        msg="Rollforward is only possible for active projects with active deployment"
        actionUrl="/projects/{project.id}"
        actionText="Back to project page"
        iconLeft={CarbonArrowLeft}
    />
{:else if project.type === ProjectType.LOCAL_FILES}
    <RollbackLocalFile
        {project}
        {currentDeployment}
        {activeDeployment}
        {envFileContent}
    />
{:else if project.type === ProjectType.GITHUB_REPO}
    Github Repo, This should be updated upon update on default branch of github
    repository the project initiated from

    <Button>Re-check if any update</Button>
{/if}
