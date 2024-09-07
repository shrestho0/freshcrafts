<script lang="ts">
	import { page } from "$app/stores";
	import PreDebug from "@/components/dev/PreDebug.svelte";
	import type {
		EngineCommonResponseDto,
		EnginePaginatedDto,
		Pageable,
	} from "@/types/dtos";
	import type { AIChatHistory } from "@/types/entities";
	import { humanizedTimeDifference, ulidToDate } from "@/utils/utils";
	import SvelteMarkdown from "svelte-markdown";
	import {
		ClickableTile,
		ComposedModal,
		ModalBody,
		ModalHeader,
		Button,
		Pagination,
		ModalFooter,
		PaginationNav,
		Tile,
		SkeletonPlaceholder,
	} from "carbon-components-svelte";

	import { afterUpdate, getContext, onMount } from "svelte";
	import { toast } from "svelte-sonner";
	import { blur, crossfade, draw, fade, fly, slide } from "svelte/transition";
	import { quintOut } from "svelte/easing";

	let form: HTMLFormElement;

	const pageData = {
		page: 1,
		pageSize: 5,
		content: [],
		totalElements: 0,
		allowedSizes: [5, 10, 20, 50, 100],
	} as {
		page: number;
		pageSize: number;
		content: AIChatHistory[];
		totalElements: number;
		allowedSizes: number[];
	};
	let selectedIdx: number | null = null;
	$: selectedItem =
		selectedIdx == null ? null : pageData.content[selectedIdx];
	let errorMessage: string = "";
	let dataLoading = false;

	async function fetchData(p = 1, pageSize = 10) {
		dataLoading = true;
		const url = new URL($page.url.href);
		url.searchParams.set("page", (p - 1).toString());
		url.searchParams.set("pageSize", pageSize.toString());
		const d = (await fetch(url.toString(), {
			method: "GET",
			headers: {
				"Content-Type": "application/json",
			},
		}).then((res) => res.json())) as EnginePaginatedDto<AIChatHistory>;

		pageData.totalElements = d.totalElements;
		// pageData.page = p;
		// pageData.pageSize = pageSize;
		pageData.content = d.content;
		setTimeout(() => {
			dataLoading = false;
		}, 500);
		console.log("pageDataX", pageData, d);
	}

	onMount(async () => {
		await fetchData(pageData.page, pageData.pageSize);
	});

	async function deleteSelected() {
		dataLoading = true;
		errorMessage = "";
		const d = (await fetch("", {
			method: "DELETE",
			body: JSON.stringify({
				id: selectedItem?.id,
			}),
		}).then((data) => data.json())) as EngineCommonResponseDto;
		console.log(d);
		if (d.success) {
			toast.success(d?.message ?? "Deleted successfully");
			// invalidateAll();
			await fetchData(pageData.page, pageData.pageSize);
		} else {
			toast.error(d?.message ?? "Failed to delete message");
		}
		selectedIdx = null;
		setTimeout(() => {
			dataLoading = false;
		}, 500);
	}

	let condition = false;
	setInterval(() => {
		condition = !condition;
	}, 1000);
</script>

<div class="py-4">
	<h2 class="text-xl">Chat History</h2>
</div>
{#if dataLoading}
	<!-- <div class="bg-red-100 text-red-900 p-2 rounded my-2">{errorMessage}</div> -->
	<div>
		{#each Array(5) as i}
			<SkeletonPlaceholder class="w-full h-14 my-2" />
		{/each}
	</div>
{:else if pageData.content?.length > 0}
	{#each pageData.content as chat, idx}
		<button
			class=" w-full bx--link flex items-center justify-between rounded my-2 bx--tile bx--tile--clickable"
			on:click={() => {
				selectedIdx = idx;
			}}
		>
			<div class="flex items-center gap-2">
				<h2 class="text-lg">{chat.chatName}</h2>
				<p class="text-gray-600 text-sm">
					({chat.messages?.length} messages)
				</p>
			</div>
			<div>
				{humanizedTimeDifference(ulidToDate(chat.id))}
			</div>
		</button>
	{/each}
{/if}
<ComposedModal
	open={selectedIdx != null}
	on:close={() => {
		selectedIdx = null;
	}}
>
	<ModalHeader title={"Chat History: " + selectedItem?.chatName} />
	<ModalBody class="max-h-[500px]" style="overflow-y:auto;">
		{#if selectedItem}
			{#each selectedItem.messages as message}
				<div
					title={message.timestamp}
					class="p-2 w-full flex items-center gap-2 {message.role ===
					'user'
						? 'justify-end'
						: 'justify-start'} "
				>
					<div
						class="p-2 rounded-lg {message.role === 'user'
							? ' bg-[var(--cds-interactive-01)] text-white '
							: ' bg-gray-300 '} break-all"
					>
						<SvelteMarkdown source={message.content} />
						<!-- {message.content} -->
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
		<Button class="w-full" kind="danger" on:click={deleteSelected}
			>Delete</Button
		>
	</ModalFooter>
</ComposedModal>

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
		<!-- on:click:button--next={nextPage}
			on:click:button--previous={prevPage} -->
		<!-- on:click:button--next={() => {
					form.page.value = data.pageable.pageNumber + 1;
					form.submit();
				}} -->
	</div>
</section>
<PreDebug data={pageData} />
