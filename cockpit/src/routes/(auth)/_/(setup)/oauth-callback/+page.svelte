<script lang="ts">
	import { CodeSnippet, CodeSnippetSkeleton } from 'carbon-components-svelte';

	export let data;
</script>

OAUTH CALLBACK PAGE

<CodeSnippet>
	{JSON.stringify(data, null, 2)}
</CodeSnippet>
