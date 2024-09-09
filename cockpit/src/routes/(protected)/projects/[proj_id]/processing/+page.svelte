<script lang="ts">
    import { browser } from "$app/environment";
    import { page } from "$app/stores";
    import { source } from "sveltekit-sse";
    import DeploymentProcessingHistory from "../DeploymentProcessingHistory.svelte";
    import PreDebug from "@/components/dev/PreDebug.svelte";
    import { goto, invalidateAll } from "$app/navigation";
    import DeploymentProcessing from "../DeploymentProcessingProgress.svelte";
    import DeploymentProcessingProgress from "../DeploymentProcessingProgress.svelte";
    import { onMount } from "svelte";
    import DepInfo from "@/components/project-specifics/DepInfo.svelte";
    import { InlineLoading, Tile } from "carbon-components-svelte";

    const { proj_id } = $page.params;

    export let data;

    let messages: string[] = [];

    let redirecting = false;

    if (browser) {
        const sse_url = `/sse/projects/${$page.params.proj_id}`;
        console.log("SSE URL", sse_url);
        const value = source(sse_url).select("message");

        value.subscribe((message) => {
            console.log("Message", message, typeof message);
            // check if message is empty
            if (message.trim() === "") return;
            if (message.includes("REVALIDATE")) {
                console.log("REVALIDATE----Invoking invalidateAll");
                redirecting = true;
                setTimeout(() => {
                    invalidateAll();
                }, 1000);
                console.log("Invalidated");
                // delete event
            }
            try {
                messages = JSON.parse(message);
                // if(messages)
            } catch (e) {
                console.error("Error parsing message", e);
            }
        });
    }
</script>

<!-- -->
<div class="">
    <!-- <DepInfo
        project={data.project}
        currentDeployment={data.currentDeployment}
        activeDeployment={data.activeDeployment}
    /> -->
    <h2 class="text-lg py-2">Deployment processing status</h2>

    {#key messages}
        <DeploymentProcessingHistory {messages} />
    {/key}
</div>
{#if redirecting}
    <Tile class="my-3 w-full ">
        <h3 class="text-lg w-full flex items-center justify-center">
            <InlineLoading class="w-auto" />
            Redirecting to project page...
        </h3></Tile
    >
{/if}
<PreDebug data={messages} />
