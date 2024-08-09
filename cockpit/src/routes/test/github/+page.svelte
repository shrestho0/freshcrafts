<script>
import { CodeSnippet } from 'carbon-components-svelte';
export let data;
</script>

<pre class="w-full h-[400[x]">
	{JSON.stringify(data, null, 2)}
</pre>
