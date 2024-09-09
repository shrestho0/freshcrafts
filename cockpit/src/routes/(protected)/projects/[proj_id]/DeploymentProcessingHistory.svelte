<script lang="ts">
    import { invalidateAll } from "$app/navigation";
    import { InlineLoading } from "carbon-components-svelte";

    export let messages: string[] = [];

    let filtered = filterMessages(messages);

    function filterMessages(messagesX: string[]) {
        let filteredX: string[] = [];

        // if current item includes "RUNNING" and next item includes "SUCCESS" or "FAILURE", then push next item
        // else push current item

        let i = 0;
        while (i < messagesX.length) {
            let currentItem = messagesX[i];
            const hasNextItem = i + 1 < messagesX.length;
            const nextItem = messagesX[i + 1];

            if (
                currentItem.startsWith("[RUNNING]") &&
                hasNextItem &&
                nextItem.search(/\[SUCCESS\]|\[FAILURE\]/) !== -1
            ) {
                filteredX.push(nextItem);
                i++;
            } else {
                filteredX.push(currentItem);
            }
            i++;
        }
        console.log("filteredX", filteredX);
        return filteredX;
    }
</script>

<div class="flex flex-col space-y-2">
    {#each filtered as message, idx}
        <InlineLoading
            status={message.includes("[RUNNING]")
                ? "active"
                : message.includes("[SUCCESS]")
                  ? "finished"
                  : message.includes("[FAILURE]")
                    ? "error"
                    : "inactive"}
            description={message
                ?.replace(/\[.*\]/, "")
                ?.replace(/__REVALIDATE__/, "")}
        />
    {/each}
</div>
