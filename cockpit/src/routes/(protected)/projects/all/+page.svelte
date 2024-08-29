<script lang="ts">
import { page } from '$app/stores';
import PreDebug from '@/components/dev/PreDebug.svelte';
import type { EngineCommonResponseDto, EnginePaginatedDto, Pageable } from '@/types/dtos';
import type { AIChatHistory, Project } from '@/types/entities';
import { humanizedTimeDifference, toTitleCase, ulidToDate } from '@/utils/utils';
import SvelteMarkdown from 'svelte-markdown';
import {
	ClickableTile,
	ComposedModal,
	ModalBody,
	ModalHeader,
	Button,
	Pagination,
	ModalFooter,
	Tag
} from 'carbon-components-svelte';
import { afterUpdate, getContext, onDestroy, onMount } from 'svelte';
import { toast } from 'svelte-sonner';
import { Files, Github } from 'lucide-svelte';
import { ProjectStatus, ProjectType } from '@/types/enums';

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
	content: Project[];
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
	}).then((res) => res.json())) as EnginePaginatedDto<Project>;

	pageData.totalElements = d.totalElements;
	// pageData.page = p;
	// pageData.pageSize = pageSize;
	pageData.content = d.content;
	console.log('pageDataX', pageData, d);
}

let interval: NodeJS.Timeout;
onMount(async () => {
	await fetchData(pageData.page, pageData.pageSize);

	interval = setInterval(async () => {
		await fetchData(pageData.page, pageData.pageSize);
	}, 5000);
});

onDestroy(() => {
	clearInterval(interval);
});
</script>

<div class="py-4">
	<h2 class="text-xl">Chat History</h2>
</div>

{#if pageData.content?.length > 0}
	{#each pageData.content as proj, idx}
		<ClickableTile
			href={`/projects/${proj.id}`}
			class="flex items-center justify-between rounded my-2"
			on:click={() => {
				selectedIdx = idx;
			}}
		>
			<div class="flex flex-col gap-2">
				<h2 class="text-lg">{proj.uniqueName}</h2>
				<p class="text-gray-600 text-sm flex items-center">
					<Tag
						type={proj.status == ProjectStatus.ACTIVE
							? 'teal'
							: proj.status == ProjectStatus.INACTIVE
								? 'magenta'
								: proj.status == ProjectStatus.PROCESSING_DEPLOYMENT
									? 'cool-gray'
									: 'info'}
						size="sm"
					>
						{toTitleCase(proj.status)}
					</Tag>
					{#if proj.type == ProjectType.LOCAL_FILES}
						(<Files size={16} />)
					{:else}
						(<Github size={16} />)
					{/if}
					<!-- ({chat.messages?.length} messages) -->
				</p>
			</div>

			<div class="flex flex-col">
				<div>
					Created: {humanizedTimeDifference(ulidToDate(proj.id))}
				</div>
				<div>
					Updated: {humanizedTimeDifference(ulidToDate(proj?.updatedAt))}
				</div>
			</div>
		</ClickableTile>
	{/each}
{/if}

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
