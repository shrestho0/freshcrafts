<script>
	import { browser } from '$app/environment';
	import { websocket } from '@ubermanu/sveltekit-websocket/stores';
	import { writable } from 'svelte/store';

	let messageFromClient = {
		message: '',
		time: ''
	};
	let messageFromServer = {
		message: '',
		time: ''
	};
	const socket = browser ? new WebSocket($websocket.url) : null;

	const connected = writable(false);

	socket?.addEventListener('open', () => connected.set(true));
	socket?.addEventListener('close', () => connected.set(false));

	socket?.addEventListener('message', (event) => {
		messageFromServer.message = event.data;
		messageFromServer.time = new Date().toLocaleTimeString();
	});
</script>

<p>
	Websocket connection status: {$connected ? '🟢' : '🔴'}
</p>

<input
	type="text"
	name=""
	id=""
	on:input={() => {
		messageFromClient.message = event?.target?.value;
		messageFromClient.time = new Date().toLocaleTimeString();
		socket?.send(messageFromClient.message);
	}}
/>

<p>
	Server Message: {JSON.stringify(messageFromServer)}
</p>
