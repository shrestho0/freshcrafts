<script lang="ts">
    export let raw: string;
    export let lang: string;
    export let text: string;

    import { CodeSnippet } from "carbon-components-svelte";
    import { codeToHtml } from "shiki";
    import { onMount } from "svelte";

    let htmlX: string;
    let parsing = true;
    onMount(async () => {
        htmlX = await codeToHtml(text, {
            lang: lang,
            theme: "vitesse-dark",
        });

        parsing = false;
    });
</script>

<div class="py-3">
    {#if parsing}
        <div class="flex justify-center items-center">
            <div
                class="animate-spin rounded-full h-32 w-32 border-t-2 border-b-2 border-gray-900"
            ></div>
        </div>
    {:else}
        {@html htmlX.replace(
            "shiki vitesse-dark",
            "shiki vitesse-dark p-4 rounded m-4 max-w-2xl",
        )}
    {/if}
</div>
