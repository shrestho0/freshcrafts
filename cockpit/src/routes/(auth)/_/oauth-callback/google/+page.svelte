<script lang="ts">
	import { browser } from '$app/environment';
	import { page } from '$app/stores';
	import { CodeSnippet, CodeSnippetSkeleton } from 'carbon-components-svelte';
	import { onMount } from 'svelte';

	export let data;
	onMount(() => {
		if (browser) {
			localStorage.setItem('oc_data', JSON.stringify(data));
			if (data?.closeWindow) {
				window.close();
			}
		}
	});
</script>

OAUTH CALLBACK PAGE

{JSON.stringify(data, null, 2)}
