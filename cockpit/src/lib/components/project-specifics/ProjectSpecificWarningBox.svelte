<script lang="ts">
	import CarbonWarning from "@/ui/icons/CarbonWarning.svelte";
	import type { SvelteComponent } from "svelte";

	export let msg: string;
	export let actionUrl: string;
	export let actionText: string;
	export let iconLeft: ConstructorOfATypedSvelteComponent | undefined =
		undefined;
	export let iconRight: ConstructorOfATypedSvelteComponent | undefined =
		undefined;
</script>

<div
	class="w-full flex flex-col items-center justify-center gap-2 bx--tile py-8"
>
	<slot name="big-icon">
		<CarbonWarning class=" fill-[var(--cds-interactive-02)] h-12 w-12" />
	</slot>
	{#if msg}
		<div class="text-center">
			<div class="text-lg font-normal">{msg}</div>
		</div>
	{/if}
	<a
		on:click
		href={actionUrl}
		class=" w-full flex gap-2 items-center justify-center max-w-sm py-2.5 rounded mt-3 bx--btn--secondary"
	>
		<svelte:component this={iconLeft} class="h-4 w-4 mr-2" />
		{actionText}
		<svelte:component this={iconRight} class="h-4 w-4 mr-2" />
	</a>
</div>
