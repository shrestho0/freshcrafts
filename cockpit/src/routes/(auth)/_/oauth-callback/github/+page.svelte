<script lang="ts">
import { browser } from '$app/environment';
import { goto } from '$app/navigation';
import { onMount } from 'svelte';

export let data;
onMount(() => {
	if (browser) {
		if (data?.success) {
			try {
				if (data?.closeWindow || (window.opener && window.opener !== window)) {
					window.close();
				}
			} catch (e) {
				console.log('error closing window', e);
			}
			// goto('/dashboard');
			window.location.href = '/dashboard';
		} else {
			console.log('data from oauth redirect page', data);
		}
		// localStorage.setItem('oc_data', JSON.stringify(data));
		// if (data?.closeWindow) {
		// 	window.close();
		// 	console.log('data from oauth redirect page', data);
		// } else if (data?.redirect) {
		// 	window.location.href = data?.redirect ? data?.redirect : '/';
		// }
	}
});
</script>

OAUTH CALLBACK PAGE
<pre>

	{JSON.stringify(data, null, 2)}
</pre>
