<script lang="ts">
    import { page } from "$app/stores";
    import PreDebug from "@/components/dev/PreDebug.svelte";
    import DepInfo from "@/components/project-specifics/DepInfo.svelte";
    import type { Project, ProjectDeployment } from "@/types/entities";
    import { InternalDeploymentActions } from "@/types/enums";
    import ProjectScreenshot from "./ProjectScreenshot.svelte";
    import { toTitleCase } from "@/utils/utils";

    export let project: Project;
    export let currentDeployment: ProjectDeployment | undefined;
    export let activeDeployment: ProjectDeployment | undefined;
</script>

<div class="grid md:grid-cols-2 gap-6">
    <DepInfo {project} {activeDeployment} {currentDeployment} />
    <ProjectScreenshot {project} />
</div>

<!-- <a href={`${$page.url}/${InternalDeploymentActions.DELETE_PROJECT}`}>
    DELETE_PROJECT
</a> -->

<div>
    {#each Object.values(InternalDeploymentActions) as action}
        <a
            class="bx--btn bg-indigo-500 m-2 rounded text-white"
            href={`${$page.url}/${action}`}
            >{toTitleCase(action?.replace("-", " "))}</a
        >
    {/each}
</div>

<PreDebug data={project} title="proj" />
<PreDebug data={currentDeployment} title="current " />
<PreDebug data={activeDeployment} title="active " />
