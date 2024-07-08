<script lang="ts">
	import HeaderAuthenticated from '@/components/HeaderAuthenticated.svelte';
	import PreDebug from '@/components/dev/PreDebug.svelte';
	export let data;
</script>

<HeaderAuthenticated />
<PreDebug {data} />
<slot />
