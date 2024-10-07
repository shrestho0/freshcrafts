<script lang="ts">
    import ProjectDeploymentStatusTag from "@/components/project-specifics/ProjectDeploymentStatusTag.svelte";
    import type { ProjectDeployment } from "@/types/entities";
    import { ProjectDeploymentStatus } from "@/types/enums";
    import LucideServerIcon from "@/ui/icons/LucideServerIcon.svelte";
    import { humanizedTimeDifference, ulidToDate } from "@/utils/utils";

    export let dep: ProjectDeployment;
</script>

<div class="py-1">
    <div class="space-y-2">
        <a
            href="/projects/{dep.projectId}"
            class="flex items-center justify-between rounded p-2
             hover:bg-[var(--cds-ui-01)]
            hover:cursor-pointer transition-colors duration-200 ease-in-out
            "
        >
            <div class="flex items-center space-x-4">
                <LucideServerIcon />
                <div>
                    <p class="text-sm font-medium">
                        {dep.project?.uniqueName || "Not Set"} - v{dep.version}
                    </p>
                    <p class="text-sm text-gray-500">
                        {humanizedTimeDifference(ulidToDate(dep.id))}
                    </p>
                </div>
            </div>
            <ProjectDeploymentStatusTag status={dep.status} />
        </a>
    </div>
</div>
