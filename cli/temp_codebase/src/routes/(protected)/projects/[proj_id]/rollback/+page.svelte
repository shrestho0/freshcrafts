<script lang="ts">
    import PreDebug from "@/components/dev/PreDebug.svelte";
    import ExpandableSection from "@/components/ExpandableSection.svelte";
    import ActionsButtons from "@/components/project-specifics/ActionsButtons.svelte";
    import DepInfo from "@/components/project-specifics/DepInfo.svelte";
    import ProjectDeploymentStatusTag from "@/components/project-specifics/ProjectDeploymentStatusTag.svelte";
    import type { ProjectDeployment } from "@/types/entities";
    import { humanizedTimeDifference, ulidToDate } from "@/utils/utils.js";
    import {
        Button,
        ClickableTile,
        ComposedModal,
        InlineLoading,
        ModalBody,
        ModalFooter,
        ModalHeader,
        SelectableTile,
        Tag,
        Tile,
    } from "carbon-components-svelte";
    import ProjectScreenshot from "../ProjectScreenshot.svelte";
    import LucideHistory from "@/ui/icons/LucideHistory.svelte";
    import {
        InternalProjectSetupCommand,
        ProjectDeploymentStatus,
    } from "@/types/enums.js";
    import { goto } from "$app/navigation";

    export let data;

    const project = data.project!;
    const currentDeployment = data.currentDeployment;
    const activeDeployment = data.activeDeployment;

    const { total, rollbacks } = data.rollbackInfo as {
        total: number;
        rollbacks: ProjectDeployment[];
    };

    let selectedRollbackDep: ProjectDeployment | null = null;
    // let selectedRollbackDep: ProjectDeployment | null = rollbacks[1];

    let serverMessage = "";
    let serverMessageType:
        | "error"
        | "active"
        | "inactive"
        | "finished"
        | undefined = "inactive";

    async function proceedRollback() {
        if (!selectedRollbackDep) alert("No rollback selected");
        // rollback
        // do stuff
        // nullify selectedRollback
        serverMessageType = "active";
        serverMessage = "Requesting rollback...";

        const res = await fetch("/projects", {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
                "x-freshcrafts-project-setup-command":
                    InternalProjectSetupCommand.ROLLBACK,
            },
            body: JSON.stringify({
                project_id: project.id,
                rollback_id: selectedRollbackDep?.id,
            }),
        })
            .then((res) => res.json())
            .catch((err) => {
                return {
                    success: false,
                    message: "Failed to request rollback",
                };
            });
        console.log("server responsed", res);
        if (res.success) {
            serverMessageType = "finished";
            serverMessage = "Rollback requested! redirecting...";

            goto(`/projects/${project.id}/processing`);

            // goto
        } else {
            serverMessageType = "error";
            serverMessage = res.message;
        }

        // setTimeout(() => {
        //     selectedRollbackDep = null;
        //     serverMessageType = "finished";
        //     serverMessage = "Rollback requested!";
        // }, 5000);
    }
</script>

<Tile class="projectInfo m rounded y-4 grid grid-cols-2">
    <DepInfo {project} {currentDeployment} {activeDeployment} />
    <ProjectScreenshot {project} />
</Tile>

<h2 class="text-2xl py-2">Rollbacks</h2>

<div class="flex gap-3 flex-col">
    <div role="group" aria-label="selectable tiles">
        {#each rollbacks as r, idx}
            <Tile class="my-1 rounded  flex items-center justify-between">
                <div class="flex gap-3">
                    <div class="text-lg">v{r.version}</div>

                    <!-- 
                    {#if r.status != ProjectDeploymentStatus.DEPLOYMENT_COMPLETED}
                        <ProjectDeploymentStatusTag status={r.status} />
                        {/if} -->
                    <ProjectDeploymentStatusTag status={r.status} />
                    {#if r.id === activeDeployment?.id}
                        <Tag class="my-0" type="green">Active</Tag>
                    {/if}
                </div>
                <button
                    disabled={r.id === activeDeployment?.id ||
                        !(
                            r.status ==
                                ProjectDeploymentStatus.DEPLOYMENT_COMPLETED ||
                            r.status ==
                                ProjectDeploymentStatus.READY_FOR_DEPLOYMENT
                        )}
                    on:click={() => {
                        selectedRollbackDep = r;
                    }}
                    class=" p-2 outline-none disabled:bg-gray-600 bg-indigo-600 disabled:cursor-not-allowed disabled: text-white/80 rounded flex justify-center items-center gap-2"
                    ><div class="">Rollbacks</div>
                    <LucideHistory class=" w-6 h-6" />
                </button>
            </Tile>
        {/each}
    </div>
</div>
<PreDebug {data} />
<ComposedModal open={selectedRollbackDep != null} preventCloseOnClickOutside>
    <ModalHeader
        closeClass="hidden"
        class="flex items-center w-full gap-3 text-2xl"
    >
        <div>
            Rollback<span class="text-indigo-500"
                >v{selectedRollbackDep?.version}</span
            >
        </div>
        <div class="loading-message">
            <InlineLoading
                status={serverMessageType}
                description={serverMessage}
            />
        </div>
    </ModalHeader>
    <ModalBody>
        <div>
            Created: ({humanizedTimeDifference(
                ulidToDate(selectedRollbackDep?.id),
            )})
        </div>
        <div>Last Active time: [fix it]</div>
        {#if selectedRollbackDep}
            <PreDebug data={selectedRollbackDep} />
        {/if}
    </ModalBody>
    <ModalFooter>
        <button
            class="bx--btn bx--btn--secondary"
            on:click={() => {
                selectedRollbackDep = null;
            }}
        >
            Cancel
        </button>
        <button class="bx--btn bx--btn--primary" on:click={proceedRollback}>
            Proceed Rollback
        </button>
    </ModalFooter>
</ComposedModal>
