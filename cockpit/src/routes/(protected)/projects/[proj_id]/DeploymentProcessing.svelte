<script lang="ts">
import { browser } from '$app/environment';
import { invalidateAll } from '$app/navigation';
import { page } from '$app/stores';
import type { ProjectDeployment } from '@/types/entities';
import { InlineLoading } from 'carbon-components-svelte';
import { source } from 'sveltekit-sse';

export let sse: boolean;
let projectId: string = $page.params.proj_id;

export let messages: string[] = [];
export let onFailure: () => void = () => {};
export let onSuccess: () => void = () => {};
export let onRevalidate: () => void = () => {
	invalidateAll();
};

let filtered = filterMessages(messages);

if (browser && sse) {
	const sse_url = `/sse/projects/${projectId}`;
	console.log('SSE URL', sse_url);
	const value = source(sse_url).select('message');

	value.subscribe((message) => {
		// check if message is empty
		if (message.trim() === '') return;

		if (message.includes('FAILURE')) {
			console.log('FAILURE----Invoking onFailure');
			if (onFailure) onFailure();
		}
		if (message.includes('SUCCESS')) {
			console.log('SUCCESS----Invoking onSuccess');
			if (onSuccess) onSuccess();
		}

		if (message.includes('REVALIDATE')) {
			console.log('REVALIDATE----Invoking invalidateAll');
			// invalidateAll();
			if (onRevalidate) onRevalidate();
		}

		console.log('Message', message);
		// messages.push(message);

		messages = [...messages, message];
		filtered = filterMessages(messages);
		// setTimeout(() => {
		// 	invalidateAll();
		// }, 500);
	});
}

// messages = messages.map((msg: string) => msg.trim());
// messages = messages.filter((msg: string) => msg.trim() !== '');
// remove duplicate
// messages = messages.filter((msg: string, idx: number) => {
// 	if (idx === 0) return true;
// 	return msg !== messages[idx - 1];
// });

// {...$$restProps}

function filterMessages(messagesX: string[]) {
	let _filteredX: string[] = [];
	let filteredX: string[] = [];

	for (let i = 0; i < messagesX.length; i++) {
		messagesX[i] = messagesX[i].trim();

		if (!messagesX[i]) {
			continue;
		}
		if (_filteredX.includes(messagesX[i])) {
			continue;
		}

		if (_filteredX.includes(messagesX[i])) {
			continue;
		}

		_filteredX.push(messagesX[i]);
	}

	let ff = [];
	// for (let i = 0; i < _filteredX.length; i++) {
	// 	const currentItem = _filteredX[i];
	// 	const hasNextItem = i + 1 < _filteredX.length;
	// 	const nextItem = _filteredX[i + 1];
	// 	if (
	// 		currentItem.startsWith('[RUNNING]') &&
	// 		hasNextItem &&
	// 		nextItem.search(/\[SUCCESS\]|\[FAILURE\]/) !== -1
	// 	) {
	// 		// ff.push(currentItem);
	// 		ff.push(nextItem);
	// 		i++;
	// 		continue;
	// 	} else {
	// 		ff.push(currentItem);
	// 	}
	// }

	let i = 0;
	while (i < _filteredX.length) {
		const currentItem = _filteredX[i];
		const hasNextItem = i + 1 < _filteredX.length;
		const nextItem = _filteredX[i + 1];

		if (
			currentItem.startsWith('[RUNNING]') &&
			hasNextItem &&
			nextItem.search(/\[SUCCESS\]|\[FAILURE\]/) !== -1
		) {
			// ff.push(currentItem);
			ff.push(nextItem);
			i++;
			// continue;
		} else {
			ff.push(currentItem);
		}

		i++;
	}

	return ff;
}
</script>

<div class="flex flex-col space-y-2">
	{#each filtered as message, idx}
		<InlineLoading
			description={message?.replace(/\[.*\]/, '')}
			status={message.startsWith('[RUNNING]')
				? 'active'
				: message.startsWith('[SUCCESS]')
					? 'finished'
					: message.startsWith('[FAILURE]')
						? 'error'
						: 'inactive'}
		/>
	{/each}
</div>
