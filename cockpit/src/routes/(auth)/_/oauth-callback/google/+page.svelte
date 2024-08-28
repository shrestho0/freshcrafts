<script lang="ts">
import { browser } from '$app/environment';
import { goto } from '$app/navigation';
import { onMount } from 'svelte';

export let data;
onMount(() => {
	if (browser) {
		if (data?.success) {
			if (data?.closeWindow || (window.opener && window.opener !== window)) {
				window.close();
			}
			// goto('/dashboard');
			window.location.href = '/dashboard';
		}
	}
});
</script>

OAUTH CALLBACK PAGE
<pre>
	
		{JSON.stringify(data, null, 2)}
	</pre>
