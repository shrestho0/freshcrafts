<script lang="ts">
    import PreDebug from "@/components/dev/PreDebug.svelte";
    import { Tile } from "carbon-components-svelte";
    import SingleCard from "./SingleCard.svelte";
    import MySqlIcon from "@/ui/icons/MySQLIcon.svelte";
    import PostgresIcon from "@/ui/icons/PostgresIcon.svelte";
    import MongoDbIcon from "@/ui/icons/MongoDBIcon.svelte";
    import RedisIcon from "@/ui/icons/RedisIcon.svelte";
    import MySqlIconMonochrome from "@/ui/icons/MySqlIconMonochrome.svelte";
    import PostgresIconMonochrome from "@/ui/icons/PostgresIconMonochrome.svelte";
    import MongoDbIconMonochrome from "@/ui/icons/MongoDBIconMonochrome.svelte";
    import RedisIconMonochrome from "@/ui/icons/RedisIconMonochrome.svelte";
    import ProjectDeploymentStatusTag from "@/components/project-specifics/ProjectDeploymentStatusTag.svelte";
    import { ProjectDeploymentStatus } from "@/types/enums";
    import LucideServerIcon from "@/ui/icons/LucideServerIcon.svelte";
    import SingleDeployment from "./SingleDeployment.svelte";
    export let data;
</script>

<div class="max-w-7xl mx-auto pb-6 sm:px-6 lg:px-8" data-id="20">
    <h3 class="text-lg font-semibold mb-4" data-id="27">Database Services</h3>
    <Tile
        class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8"
        data-id="28"
    >
        <SingleCard
            title="MySQL"
            icon={MySqlIconMonochrome}
            count={data?.dbData?.payload?.activeMySqlCount}
            iconClass=""
        />

        <SingleCard
            title="PostgreSQL"
            icon={PostgresIconMonochrome}
            count={data?.dbData?.payload?.activePostgresCount}
            iconClass=""
        />

        <SingleCard
            title="MongoDB"
            icon={MongoDbIconMonochrome}
            count={data?.dbData?.payload?.activeMongoCount}
            iconClass=""
        />

        <SingleCard
            title="Redis"
            icon={RedisIconMonochrome}
            count={data?.dbData?.payload?.activeRedisCount}
            iconClass=""
        />
    </Tile>
    <h3 class="text-lg font-semibold mb-4" data-id="36">Recent Deployments</h3>
    <div class="rounded-lg border shadow-sm">
        {#if data?.depData?.payload && data.depData.payload.length > 0}
            {#each data.depData.payload as dep}
                <SingleDeployment {dep} />
            {/each}
        {:else}
            <div class="p-6" data-id="49">
                <p class="text-sm text-muted-foreground" data-id="50">
                    No deployments found
                </p>
            </div>
        {/if}
    </div>
</div>
<!-- <PreDebug {data} /> -->
