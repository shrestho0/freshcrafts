<script lang="ts">
import type { SystemwideNotification } from '@/types/entities';
import { SystemWideNoitficationTypes } from '@/types/enums';
import { Button, TextArea, TextInput } from 'carbon-components-svelte';
import { source } from 'sveltekit-sse';

const notiSSEUrl = `/sse/notification`;
const value = source(notiSSEUrl).select('message');
let d = {
	id: 'some string',
	message: 'some string',
	actionUrlHints: 'GOTO_MYSQL__VIEW', // convert hints to url
	markedAsRead: false,
	type: SystemWideNoitficationTypes.SUCCESS
} as SystemwideNotification;

async function sendEvent() {
	const res = await fetch(notiSSEUrl, {
		method: 'PATCH',
		headers: {
			'Content-Type': 'application/json',
			// TODO: Delete this page
			authorization: 'Bearer VERY_SECRET_TOKEN'
		},
		body: JSON.stringify(d)
	}).then((data) => data.json());
	console.log(res);
}
</script>

<form on:submit|preventDefault>
	{#each Object.keys(d) as k}
		<TextInput labelText={k} bind:value={d[k]} />
	{/each}

	<Button on:click={sendEvent}>SendEvent</Button>

	<pre>
	{$value}
</pre>
</form>
