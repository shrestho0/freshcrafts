<!-- screen size in px -->

<script lang="ts">
	import { CodeSnippet } from 'carbon-components-svelte';
	import CustomCodeSnippet from '../CustomCodeSnippet.svelte';
	import { onMount } from 'svelte';

	let innerWidth: any;
	let innerHeight: any;
	let loading = true;
	onMount(() => {
		loading = false;
	});
</script>

<svelte:window bind:innerWidth bind:innerHeight />
{#if loading}
	<p>Device screen size loading...</p>
{:else}
	<CustomCodeSnippet classes="h-full">
		{`{
    width: ${innerWidth}px,
    height: ${innerHeight}px
}
`}
	</CustomCodeSnippet>
{/if}

<!-- <CodeSnippet class="h-full">
	{`{
    width: ${innerWidth}px,
    height: ${innerHeight}px
}
`}
</CodeSnippet> -->
