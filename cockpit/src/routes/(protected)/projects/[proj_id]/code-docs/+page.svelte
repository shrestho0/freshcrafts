<script lang="ts">
    import { applyAction, enhance } from "$app/forms";
    import { invalidateAll } from "$app/navigation";
    import PreDebug from "@/components/dev/PreDebug.svelte";
    import MdCodeBlock from "@/components/MDCodeBlock.svelte";
    import type {
        AICodeDoc,
        Project,
        ProjectDeployment,
    } from "@/types/entities.js";
    import { InternalDocGenerationCommands } from "@/types/enums";
    import { ulidToDate, humanizedTimeDifference } from "@/utils/utils";
    import {
        InlineLoading,
        Button,
        ComposedModal,
        ModalHeader,
        ModalBody,
        ModalFooter,
    } from "carbon-components-svelte";
    import SvelteMarkdown from "svelte-markdown";

    export let data;
    console.log(data);
    /**
     * It must have project and activeDeployment
     */
    let project: Project = data.project!;
    let activeDeployment: ProjectDeployment = data.activeDeployment!;

    let generationRequestStuff = {
        status: "idle",
    } as {
        status: "idle" | "requested" | "generated" | "failed";
    };

    let selectedIdx: number | null = null;
    let selectedItem: null | AICodeDoc;

    async function generateDoc() {
        generationRequestStuff.status = "requested";
        const res = await fetch("", {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                command: InternalDocGenerationCommands.GENERATE_DOC,
                project: project,
                activeDeployment: activeDeployment,
            }),
        });

        handleGenerateDoc(res);
    }

    async function handleGenerateDoc(res: Response) {
        const dataReturned = await res.json().catch((e) => {
            return {
                success: false,
                message: "Failed to parse response",
            };
        });

        if (dataReturned.success) {
            generationRequestStuff.status = "generated";
        } else {
            generationRequestStuff.status = "failed";
        }

        invalidateAll();
    }

    const svelteMarkdownRenderers = {
        code: MdCodeBlock,
    };
</script>

<!-- <PreDebug data={{ generationRequestStuff }} />
Generated Docs Generate New Doc
{data.codeDocs} -->
<div class="flex flex-col">
    <Button on:click={generateDoc}>Generate New Documentation</Button>
    {#if generationRequestStuff.status === "requested"}
        <p>Generating doc...</p>
    {/if}
</div>
{#if data?.codeDocs?.length === 0}
    <p>No code docs found</p>
{:else}
    {#each data?.codeDocs as doc, idx}
        <!-- {#if doc.status != "REQUESTED"} -->
        <button
            class=" w-full bx--link flex items-center justify-between rounded my-2 bx--tile bx--tile--clickable"
            on:click={() => {
                selectedIdx = idx;
                selectedItem = doc;
            }}
        >
            <div class="flex items-center gap-2">
                <h2 class="text-lg">
                    {(doc.content || "Unnamed").slice(0, 10)}
                </h2>
                {#if doc.status === "CREATED"}
                    <span class="bx--tag bx--tag--blue">
                        {doc.status}
                    </span>
                {:else}
                    <span class="bx--tag bx--tag--red">
                        {doc.status}
                    </span>
                {/if}
            </div>
            <div>
                <!-- {humanizedTimeDifference(ulidToDate(doc.id))} -->
            </div>
        </button>
        <!-- {/if} -->
    {/each}
{/if}

<ComposedModal
    open={selectedIdx != null}
    on:close={() => {
        selectedIdx = null;
    }}
    size="lg"
>
    <ModalHeader title={"Chat History: "} />
    <ModalBody class="max-h-[500px]" style="overflow-y:auto;">
        {#if selectedItem}
            <!-- {#each selectedItem.messages as message} -->
            <!-- <div
                    title={message.timestamp}
                    class="p-2 w-full flex items-center gap-2 {message.role ===
                    'user'
                        ? 'justify-end'
                        : 'justify-start'} "
                >
                    <div
                        class="p-2 rounded-lg {message.role === 'user'
                            ? ' bg-[var(--cds-interactive-01)] text-white '
                            : ' bg-gray-300 '} break-all"
                    > -->
            <SvelteMarkdown
                source={selectedItem.content}
                renderers={svelteMarkdownRenderers}
            />
            <!-- </div> -->
            <!-- </div> -->
            <!-- {/each} -->
        {/if}
    </ModalBody>

    <ModalFooter class="w-full grid grid-cols-2">
        <Button
            kind="secondary"
            on:click={() => {
                selectedIdx = null;
            }}>Close</Button
        >
        <form
            action="?/deleteDoc"
            method="post"
            use:enhance={() => {
                return async ({ result }) => {
                    applyAction(result);
                    invalidateAll();
                };
            }}
        >
            <input type="hidden" name="id" value={selectedItem?.id} />
            <Button class="w-full" kind="danger" type="submit">Delete</Button>
        </form>
    </ModalFooter>
</ComposedModal>
