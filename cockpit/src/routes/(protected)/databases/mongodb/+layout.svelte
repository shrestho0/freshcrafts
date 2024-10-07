<script>
    import CarbonWarning from "@/ui/icons/CarbonWarning.svelte";
    import { Tile } from "carbon-components-svelte";

    export let data;

    const depServiceStatus =
        data?.dependenciesStatus?.secondary_mongo == "true";
    const systemServiceStatus =
        data?.systemServiceStatus?.wizard_mongo == "true";

    const depServiceName = "Secondary_Mongo";
    const systemServiceName = "Wizard_Mongo";
</script>

{#if depServiceStatus == false || systemServiceStatus == false}
    <Tile class="my-3 flex flex-col rounded items-center justify-center w-full">
        <p class="flex items-center">
            <CarbonWarning class="mr-2" />
            {#if depServiceStatus == false && systemServiceStatus == false}
                Dependency {depServiceName} and Service {systemServiceName} are not
                running.
            {:else if depServiceStatus == false}
                Dependency {depServiceName} is not running.
            {:else if systemServiceStatus == false}
                Service {systemServiceName} is not running.
            {/if}
        </p>
        If page does not re-direct, don't worry, actions will be proceed as soon
        as the services are up.
    </Tile>
{/if}

<slot />
