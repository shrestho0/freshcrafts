<script lang="ts">
import { page } from '$app/stores';
import PreDebug from '@/components/dev/PreDebug.svelte';
import type { EngineCommonResponseDto, EnginePaginatedDto, Pageable } from '@/types/dtos';
import type { SystemwideNotification } from '@/types/entities';
import { humanizedTimeDifference, toTitleCase, ulidToDate } from '@/utils/utils';
import { Tile, Pagination, Tag } from 'carbon-components-svelte';
import { onMount } from 'svelte';
import { toast } from 'svelte-sonner';
import { SystemWideNoitficationTypes } from '@/types/enums';
import SquareArrowOutUpRight from '@/ui/icons/SquareArrowOutUpRight.svelte';

let form: HTMLFormElement;

const pageData = {
	page: 1,
	pageSize: 5,
	content: [],
	totalElements: 0,
	allowedSizes: [5, 10, 20, 50, 100]
} as {
	page: number;
	pageSize: number;
	content: SystemwideNotification[];
	totalElements: number;
	allowedSizes: number[];
};
let selectedIdx: number | null = null;
$: selectedItem = selectedIdx == null ? null : pageData.content[selectedIdx];
let errorMessage: string = '';

async function fetchData(p = 1, pageSize = 10) {
	const url = new URL($page.url.href);
	url.searchParams.set('page', (p - 1).toString());
	url.searchParams.set('pageSize', pageSize.toString());
	const d = (await fetch(url.toString(), {
		method: 'GET',
		headers: {
			'Content-Type': 'application/json'
		}
	}).then((res) => res.json())) as EnginePaginatedDto<SystemwideNotification>;

	pageData.totalElements = d.totalElements;
	// pageData.page = p;
	// pageData.pageSize = pageSize;
	pageData.content = d.content;
	console.log('pageDataX', pageData, d);
}

onMount(async () => {
	await fetchData(pageData.page, pageData.pageSize);
});

async function deleteSelected() {
	errorMessage = '';
	const d = (await fetch('', {
		method: 'DELETE',
		body: JSON.stringify({
			id: selectedItem?.id
		})
	}).then((data) => data.json())) as EngineCommonResponseDto;
	console.log(d);
	if (d.success) {
		toast.success(d?.message ?? 'Deleted successfully');
		// invalidateAll();
		await fetchData(pageData.page, pageData.pageSize);
	} else {
		toast.error(d?.message ?? 'Failed to delete message');
	}
	selectedIdx = null;
}

function generateTagColor(type: SystemWideNoitficationTypes): any {
	switch (type) {
		case SystemWideNoitficationTypes.SUCCESS:
			return 'teal';
		case SystemWideNoitficationTypes.INFO:
			return 'warm-gray';
		case SystemWideNoitficationTypes.WARNING:
			return 'high-contrast';
		case SystemWideNoitficationTypes.ERROR:
			return 'red';
		default:
			return 'outline';
	}
	// if (type==SystemWideNoitficationTypes.SUCCESS) ? 'teal'
}
</script>

<div class="py-4">
	<h2 class="text-xl">Chat History</h2>
</div>

{#if pageData.content?.length > 0}
	<!-- on click, mark as read  -->
	<!-- {#key pageData.content} -->
	<!-- <div
			transition:slide={{
				duration: 3000
			}}
		> -->
	{#each pageData.content as chat, idx}
		<div class="flex items-center justify-between rounded my-2 select-none bx--tile">
			<div class="flex items-center gap-2">
				<!-- icon={!chat.markedAsRead ? CircleSolid : undefined} -->
				<Tag type={generateTagColor(chat.type)}>{toTitleCase(chat.type)}</Tag>
				{chat.message}
			</div>
			<div class="flex gap-3">
				{humanizedTimeDifference(ulidToDate(chat.id))}
				<SquareArrowOutUpRight />
			</div>
		</div>
	{/each}
	<!-- </div> -->
	<!-- {/key} -->
{/if}
<!-- 
<ComposedModal
	open={selectedIdx != null}
	on:close={() => {
		selectedIdx = null;
	}}
>
	<ModalHeader title={'Chat History: ' + selectedItem?.chatName} />
	<ModalBody class="max-h-[500px]" style="overflow-y:auto;">
		{#if selectedItem}
			{#each selectedItem.messages as message}
				<div
					title={message.timestamp}
					class="p-2 w-full flex items-center gap-2 {message.role === 'user'
						? 'justify-end'
						: 'justify-start'} "
				>
					<div
						class="p-2 bg-gray-300 rounded-lg {message.role === 'user'
							? ' bg-[var(--cds-interactive-01)] text-white '
							: ' bg-gray-300 '} break-all"
					>
						<SvelteMarkdown source={message.content} />
					</div>
				</div>
			{/each}
		{/if}
	</ModalBody>

	<ModalFooter class="w-full grid grid-cols-2">
		<Button
			kind="secondary"
			on:click={() => {
				selectedIdx = null;
			}}>Close</Button
		>
		<Button class="w-full" kind="danger" on:click={deleteSelected}>Delete</Button>
	</ModalFooter>
</ComposedModal> -->

<section class="w-full">
	<div class="w-full flex items-center">
		<Pagination
			class="w-full"
			bind:totalItems={pageData.totalElements}
			bind:page={pageData.page}
			bind:pageSize={pageData.pageSize}
			pageSizes={pageData.allowedSizes}
			on:change={async () => {
				await fetchData(pageData.page, pageData.pageSize);
			}}
		/>
	</div>
</section>
<PreDebug data={pageData} />
