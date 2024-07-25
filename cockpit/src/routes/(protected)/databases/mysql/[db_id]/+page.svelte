<script lang="ts">
	import { page } from '$app/stores';
	import PreDebug from '@/components/dev/PreDebug.svelte';
	import type { EngineMySQLGetOneError, EngineCommonResponseDto } from '@/types/dtos';
	import type { DBMysql } from '@/types/entities';
	import { Button, TextInput } from 'carbon-components-svelte';
	import { source } from 'sveltekit-sse';

	export let data: EngineCommonResponseDto<DBMysql, EngineMySQLGetOneError>;

	///// FIXME: Temporary Stuff
	// Delete stuff done
	const eventToSend: DBMysql = {
		id: '',
		dbName: '',
		dbUser: '',
		dbPassword: '',
		status: null,
		reasonFailed: null
	};

	const { db_id } = $page.params;
	const sse_url = `/sse/databases/mysql/${db_id}`;
	const value = source(sse_url).select('message');

	async function sendEvent() {
		const res = await fetch(sse_url, {
			method: 'PATCH',
			headers: {
				'Content-Type': 'application/json',
				// TODO: Delete this page
				authorization: 'Bearer VERY_SECRET_TOKEN'
			},
			body: JSON.stringify(eventToSend)
		}).then((data) => data.json());
		console.log(res);
	}
</script>

DBID
<PreDebug {data} />
<PreDebug data={eventToSend} />

{#each Object.keys(eventToSend) as k}
	<TextInput labelText={k} bind:value={eventToSend[k]} />
{/each}
<Button on:click={sendEvent}>Send Event</Button>

{$value}
