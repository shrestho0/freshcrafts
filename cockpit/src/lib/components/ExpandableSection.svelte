<script lang="ts">
import { ClickableTile } from 'carbon-components-svelte';
import { ChevronDown, ChevronUp } from 'carbon-icons-svelte';
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
		<ChevronUp />
	{:else}
		<ChevronDown />
	{/if}
</ClickableTile>
{#if open}
	<slot></slot>
{/if}
