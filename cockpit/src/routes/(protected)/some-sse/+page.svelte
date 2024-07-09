<script>
	import { Button } from 'carbon-components-svelte';
	import { source } from 'sveltekit-sse';
	const value = source('/some-sse').select('message');

	let d = '';
	async function sendEvent() {
		const res = await fetch('', {
			method: 'PATCH',
			headers: {},
			body: JSON.stringify({
				message: d,
				time: new Date()
			})
		}).then((data) => data.json());
		console.log(res);
	}
</script>

<input type="text" bind:value={d} />
<Button on:click={sendEvent}>SendEvent</Button>
<div class="text-white">
	{$value}
</div>
