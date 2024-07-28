<script lang="ts">
	import { browser } from '$app/environment';
	import { invalidateAll } from '$app/navigation';
	import { page } from '$app/stores';
	import PreDebug from '@/components/dev/PreDebug.svelte';
	import type { EngineMySQLGetOneError, EngineCommonResponseDto } from '@/types/dtos';
	import type { DBMysql } from '@/types/entities';
	import { DBMysqlStatus } from '@/types/enums';
	import {
		Button,
		InlineLoading,
		Modal,
		ModalBody,
		ModalHeader,
		Tile
	} from 'carbon-components-svelte';
	import { OctagonAlert, OctagonX } from 'lucide-svelte';
	import { source } from 'sveltekit-sse';
	import UpdateDbModal from './UpdateDBModal.svelte';
	import DeleteDbModal from './DeleteDBModal.svelte';
	import { enhance } from '$app/forms';

	export let data: EngineCommonResponseDto<DBMysql, EngineMySQLGetOneError>;

	if (browser && data?.payload?.status != DBMysqlStatus.OK) {
		// (data?.payload?.status == DBMysqlStatus.REQUESTED ||
		// 	data?.payload?.status == DBMysqlStatus.PENDING_DELETE)
		console.log('sse hit hobe');
		const { db_id } = $page.params;

		const sse_url = `/sse/databases/mysql/${db_id}`;

		const value = source(sse_url).select('message');
		if (value) {
			setTimeout(() => {
				invalidateAll();
			}, 1000);
			console.log('Value', value.json());
		}
	}

	let updateModalOpen = false;
	let deleteModalOpen = false;
</script>

<div class="w-full flex items-center justify-center">
	{#if data?.success}
		{#if data.payload.status === DBMysqlStatus.OK}
			<Tile class="w-full"
				>Freaking success.
				<pre>
- Update DB Options
- Delete DB Option
- A box to show the DB details
- An input to execute queries on the db [for later];
- Show tables and stuff [for later];

				</pre>

				<!-- update db form model open button -->
				<Button
					kind="secondary"
					on:click={() => {
						updateModalOpen = true;
					}}>Update DB Options</Button
				>

				<!-- delete db form model open button -->
				<Button
					kind="danger"
					on:click={() => {
						deleteModalOpen = true;
					}}>Delete DB Option</Button
				>
			</Tile>
		{:else if data.payload.status === DBMysqlStatus.FAILED}
			<Tile class="w-full flex flex-col items-center justify-center">
				<OctagonX class=" h-12 h-12" />
				<div class="text-center text-[var(--cds-interactive-02)]">
					<div class="text-lg font-normal">Failed to create MySQL Database</div>
					<div class="text-md py-1">
						Reason of failure: <span class=" ">{data.payload.reasonFailed} </span>
					</div>

					<!-- delete db form model open button -->
					<Button
						class="w-full "
						kind="secondary"
						size="small"
						on:click={() => {
							deleteModalOpen = true;
						}}>Delete Data</Button
					>
				</div>
			</Tile>
		{:else if data.payload.status === DBMysqlStatus.PENDING_DELETE}
			<Tile class="w-full flex flex-col items-center justify-center">
				<InlineLoading class="w-auto" />
				<div class="text-center text-[var(--cds-interactive-02)]">
					<div class="text-lg font-normal">
						Your request to delete the database is being processed
					</div>
					<div class="text-md py-1">(You will be notified when complete)</div>
				</div>
			</Tile>
		{:else if data.payload.status === DBMysqlStatus.REQUESTED}
			<Tile class="w-full flex flex-col items-center justify-center">
				<!-- <OctagonX class=" h-12 h-12" /> -->
				<InlineLoading class="w-auto" />
				<div class="text-center text-[var(--cds-interactive-02)]">
					<div class="text-lg font-normal">Your request is being processed</div>
					<div class="text-md py-1">(You will be notified when complete)</div>
				</div>
			</Tile>
		{:else if data.payload.status === DBMysqlStatus.UPDATE_REQUESTED}
			<Tile class="w-full flex flex-col items-center justify-center">
				<InlineLoading class="w-auto" />
				<div class="text-center text-[var(--cds-interactive-02)]">
					<div class="text-lg font-normal">
						{data?.payload?.updateMessage ??
							'Your request to update the database is being processed'}
					</div>
					<div class="text-md py-1">(You will be notified when complete)</div>
				</div>
			</Tile>
		{:else if data.payload.status === DBMysqlStatus.UPDATE_FAILED}
			<Tile class="w-full flex flex-col items-center justify-center">
				<OctagonX class=" h-12 h-12" />
				<div class="text-center text-[var(--cds-interactive-02)]">
					<div class="text-lg font-normal">Failed to update MySQL Database</div>
					<div class="text-md py-1">
						Reason of failure: <span class=" "
							>{data?.payload?.reasonFailed ?? 'Unknown error'}
						</span>
					</div>

					<!-- delete db form model open button -->
					<form action="?/revert" method="post" use:enhance>
						<Button type="submit" class="w-full " kind="secondary" size="small"
							>Revert Changes</Button
						>
					</form>
				</div>
			</Tile>
		{/if}
	{:else}
		<Tile class="w-full flex flex-col items-center justify-center">
			<OctagonAlert class=" h-12 h-12" />
			<div class="text-center text-lg font-normal text-[var(--cds-interactive-02)]">
				No MySQL Database found <br />
			</div>
		</Tile>
	{/if}
</div>
<PreDebug {data} />

{#if data?.payload}
	<UpdateDbModal bind:open={updateModalOpen} bind:db={data.payload} />
	<DeleteDbModal bind:open={deleteModalOpen} bind:db={data.payload} />
{/if}
