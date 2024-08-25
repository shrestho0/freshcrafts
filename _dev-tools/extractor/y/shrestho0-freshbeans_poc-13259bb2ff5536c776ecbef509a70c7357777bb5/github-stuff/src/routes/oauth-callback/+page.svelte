<script lang="ts">
	import { goto } from '$app/navigation';
	import { onMount } from 'svelte';

	export let data;

	onMount(() => {
		if (data.success) {
			goto('/');
		}
	});
</script>

<pre>{JSON.stringify(data)}</pre>
