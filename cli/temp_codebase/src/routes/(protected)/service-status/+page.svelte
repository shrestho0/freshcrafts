<script lang="ts">
import { invalidateAll } from '$app/navigation';
import PreDebug from '@/components/dev/PreDebug.svelte';
import type { EngineCommonResponseDto } from '@/types/dtos';
import CarbonArrowLeft from '@/ui/icons/CarbonArrowLeft.svelte';
import CarbonArrowRight from '@/ui/icons/CarbonArrowRight.svelte';
import LucidePowerOff from '@/ui/icons/LucidePowerOff.svelte';
import { humanizedTimeDifference, toTitleCase, ulidToDate } from '@/utils/utils';
import { afterUpdate, onDestroy, onMount } from 'svelte';

import CarbonCheckMarkFilled from '@/ui/icons/CarbonCheckMarkFilled.svelte';
import CarbonCloseFilled from '@/ui/icons/CarbonCloseFilled.svelte';

export let data: EngineCommonResponseDto;

let timeout: NodeJS.Timeout;
let lastUpdated: Date | undefined;
let lastUpdatedWatchDog: Date | undefined;
onMount(() => {
	timeout = setInterval(() => {
		invalidateAll();
	}, 2000);

	if (data.success) {
		lastUpdatedWatchDog = ulidToDate(data?.payload?.[0]?.timestamp);
	}
});

afterUpdate(() => {
	console.log('after update');
	lastUpdated = new Date();
});

onDestroy(() => {
	timeout && clearInterval(timeout);
});
</script>

<h2 class="text-2xl">Watch Dog Service Status</h2>

<p class="py-2">Last Updated: {lastUpdated}</p>
<p class="py-2">Last Updated Watchdog: {lastUpdatedWatchDog}</p>

{#if data.success}
	{#if data?.payload?.length > 0}
		<div class="grid grid-cols-2">
			{#each data?.payload as sd}
				<div class="text-xl py-3">
					<h2 class=" py-3">
						{toTitleCase(sd?.id)}
					</h2>
					{#each Object.keys(sd) as key}
						{#if key !== 'id' && key !== 'timestamp' && sd[key] !== null}
							<p class="flex items-center gap-2 py-1">
								{#if JSON.parse(sd[key]) == true}
									<CarbonCheckMarkFilled
										class="fill-[var(--cds-support-02)] h-5 w-5 rounded-full "
									/>
								{:else}
									<CarbonCloseFilled class="fill-[--cds-support-01] h-5 w-5 rounded-full " />
								{/if}
								{toTitleCase(key?.toString().replace('_', ' '))}
							</p>
						{/if}
					{/each}
				</div>
			{/each}
		</div>
	{:else}
		Service Status Not Available
	{/if}
{/if}
<PreDebug {data} />
