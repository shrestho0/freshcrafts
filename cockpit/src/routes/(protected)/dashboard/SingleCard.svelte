<script lang="ts">
    export let title: string;
    export let count: number;
    export let icon: any;
    export let iconClass: string = "text-muted-foreground";
    export let bottomText: string = "Active instances";
</script>

<div
    class="rounded-lg dark:hover:bg-[var(--cds-interactive-02)] hover:bg-[var(--cds-ui-02)]
                 transition-colors duration-200 ease-in-out

    "
>
    <div class="p-6 flex flex-row items-center justify-between space-y-0 pb-1">
        <h3 class="whitespace-nowrap tracking-tight text-sm font-medium">
            {title}
        </h3>

        {#if icon}
            <svelte:component
                this={icon}
                class="h-5 w-5 {iconClass} dark:fill-white fill-current "
            />
        {/if}
    </div>
    <div class="p-6">
        <div class="text-2xl font-bold">{count}</div>
        <p class="text-xs text-muted-foreground">{bottomText}</p>
    </div>
</div>
