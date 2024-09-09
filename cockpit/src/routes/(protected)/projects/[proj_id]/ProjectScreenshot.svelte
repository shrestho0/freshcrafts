<script lang="ts">
    import type { Project } from "@/types/entities";
    import { InternalProjectSetupCommand } from "@/types/enums";
    import { SkeletonPlaceholder } from "carbon-components-svelte";
    import { onMount } from "svelte";

    export let project: Project;

    let loading = true;
    onMount(async () => {
        try {
            await getScreenshot();
        } catch (error) {
            console.error("Error fetching screenshot", error);
        } finally {
            loading = false;
        }
    });

    // let image: HTMLImageElement;
    let url: string;
    async function getScreenshot() {
        // get screenshot from server
        const res = await fetch(`/projects`, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
                "x-freshcrafts-project-setup-command":
                    InternalProjectSetupCommand.PROJECT_SCREEENSHOT,
            },
            body: JSON.stringify({
                projectId: project.id,
                projectPort: project.portAssigned,
            }),
        });
        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        if (res.headers.get("content-type") !== "image/png") {
            console.error("Invalid content type");
            // url = "/.png";
            return;
        }

        // blob

        // else it's a png
        // const blob = await res.blob();
        // url = URL.createObjectURL(blob);

        const blob = await res.blob();
        let reader = new FileReader();
        reader.readAsDataURL(blob);
        reader.onloadend = function () {
            url = reader.result as string;
        };
        // url = URL.createObjectURL(blob);

        console.log("blob", blob);
        // url = URL.createObjectURL(blob);
    }
</script>

<!-- {#if loading}
    <SkeletonPlaceholder style="height: 100px" />
{:else if url}
    {#key url}
        <img src={url} alt="some image" />
    {/key}
{/if}
{url} -->
<!-- 
{#if loading}
    <SkeletonPlaceholder style="height: 100px" />
{:else if url}
    <img src={url} alt="some adfsf" />
{/if}
{url} -->
<!-- {url} -->

{#if loading}
    <SkeletonPlaceholder class="w-full h-auto max-h-[300px]" />
{:else if url}
    <img
        class="rounded"
        src={url}
        alt="Project screenshot"
        style="width: 100%;"
    />
{:else}
    <div
        class="w-full rounded bg-indigo-600 text-white flex items-center justify-center h-auto max-h-[300px]"
    >
        No screenshot available.
    </div>
{/if}
