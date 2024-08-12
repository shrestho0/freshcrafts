<script lang="ts">
import PreDebug from '@/components/dev/PreDebug.svelte';
import type { EnginePaginatedDto } from '@/types/dtos.js';
import type { SystemwideNotification } from '@/types/entities';
import { SystemWideNoitficationTypes } from '@/types/enums';
import { delay, fetchDataSelf } from '@/utils/utils';
import { Button, TextArea, TextInput } from 'carbon-components-svelte';
import { onMount } from 'svelte';
import { source } from 'sveltekit-sse';

const notiSSEUrl = `/sse/notification`;
const value = source(notiSSEUrl).select('message');
// let d = {
// 	id: 'some string',
// 	message: 'some string',
// 	actionUrlHints: 'GOTO_MYSQL__VIEW', // convert hints to url
// 	markedAsRead: false,
// 	type: SystemWideNoitficationTypes.SUCCESS
// } as SystemwideNotification;

// async function sendEvent() {
// 	const res = await fetch(notiSSEUrl, {
// 		method: 'PATCH',
// 		headers: {
// 			'Content-Type': 'application/json',
// 			// TODO: Delete this page
// 			authorization: 'Bearer VERY_SECRET_TOKEN'
// 		},
// 		body: JSON.stringify(d)
// 	}).then((data) => data.json());
// 	console.log(res);
// }

export let data;

let dataStuff = {
	status: 'LOADING',
	query: '',
	searchList: [],
	regularData: {
		totalElements: 0,
		content: [],
		pageable: {
			pageNumber: 0,
			pageSize: 5,
			sort: {
				sorted: false,
				empty: false,
				unsorted: false
			},
			offset: 0,
			paged: false,
			unpaged: false
		},
		size: 0,
		number: 0,
		sort: {
			sorted: false,
			empty: false,
			unsorted: false
		},
		first: false,
		last: false,
		numberOfElements: 0,
		empty: false
	}
} as unknown as {
	status: 'DATA' | 'LOADING' | 'SEARCHING' | 'SEARCH_RESULT';
	query: string;
	regularData: EnginePaginatedDto<SystemwideNotification>;
	// searchList: SystemwideNotification[];
	content: SystemwideNotification[];
};

async function fetchRegularData() {
	dataStuff.status = 'LOADING';
	const res = (await fetchDataSelf(
		{
			page: dataStuff.regularData.pageable.pageNumber,
			pageSize: dataStuff.regularData.pageable.pageSize
		},
		'POST',
		null
	)) as unknown as EnginePaginatedDto<SystemwideNotification>;
	console.log('regularData', res);

	// delay for 500ms
	await delay(300);
	dataStuff.regularData = res;
	dataStuff.status = 'DATA';
}

onMount(async () => {
	await fetchRegularData();
});
</script>

<!-- <form on:submit|preventDefault>
	{#each Object.keys(d) as k}
		<TextInput labelText={k} bind:value={d[k]} />
	{/each}

	<Button on:click={sendEvent}>SendEvent</Button>

	<pre>
	{$value}
</pre>
</form> -->
<PreDebug {data} />
