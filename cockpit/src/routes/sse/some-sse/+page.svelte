<script>
	import { Button, TextInput } from 'carbon-components-svelte';
	let d = 'world';
	import { writable } from 'svelte/store';
	import { source } from 'sveltekit-sse';

	// const messages = writable([]);
	// const evtSource = new EventSource('/some-sse');

	// evtSource.onmessage = function (event) {
	// 	console.log(event);
	// 	// var dataobj = { server: JSON.parse(event.data) };
	// 	// console.log('from sse', dataobj);
	// 	// messages.update((arr) => arr.concat(dataobj));
	// };
	const value = source('/sse/some-sse').select('message');

	async function sendEvent() {
		const res = await fetch('/sse/some-sse', {
			method: 'PATCH',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({
				message: d,
				time: new Date()
			})
		}).then((data) => data.json());
		console.log(res);
	}
</script>

<!-- {JSON.stringify(messages)} -->
<TextInput type="text" bind:value={d} />

<Button on:click={sendEvent}>SendEvent</Button>

<pre>
	{$value}
</pre>
