<script lang="ts">
import CarbonChevronDown from '@/ui/icons/CarbonChevronDown.svelte';
import CarbonChevronUp from '@/ui/icons/CarbonChevronUp.svelte';
import { ClickableTile } from 'carbon-components-svelte';
export let open = false;
export let title = '';
export let classes = '';
</script>

<ClickableTile
	class="flex items center justify-between {classes}"
	on:click={() => {
		open = !open;
	}}
>
	<slot name="title">
		{title}
	</slot>
	{#if open}
		<CarbonChevronUp />
	{:else}
		<CarbonChevronDown />
	{/if}
</ClickableTile>
{#if open}
	<slot></slot>
{/if}
