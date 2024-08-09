import SomeSSEStore from '@/stores/SomeSSEStore.server';
import { json, type RequestHandler } from '@sveltejs/kit';
import { writable } from 'svelte/store';

// const XStore = writable<string[]>([])
// src/routes/custom-event/+server.js
import { produce } from 'sveltekit-sse';

/**
 * @param {number} milliseconds
 * @returns
 */
function delay(milliseconds: number | undefined) {
	return new Promise(function run(resolve) {
		setTimeout(resolve, milliseconds);
	});
}

// TODO: Keep track of idle time.
// TODO: Close if idle for more than 10 minutes.
// TODO: Or maybe not. We'll see

export function POST() {
	return produce(async function start({ emit }) {
		SomeSSEStore.subscribe(async (data) => {
			const { error } = emit('message', `the time is ${JSON.stringify(data)}`);
			await delay(1000);
		});
	});
}

export async function PATCH({ request }) {
	const data = await request.json();
	console.log(data);
	if (data?.message) {
		console.log('updating some sse store from +server.ts Post');
		// SomeSSEStore.set(data)
		SomeSSEStore.set({
			message: data?.message,
			time: new Date().getTime()
		});
	}

	return json({
		whatever: 'something'
	});
}
