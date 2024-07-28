<script lang="ts">
	import {
		DataTable,
		Toolbar,
		ToolbarContent,
		ToolbarSearch,
		Button,
		Table,
		TableBody,
		TableHead,
		TableHeader,
		TableRow,
		TableCell,
		DataTableSkeleton,
		Modal,
		CodeSnippet,
		InlineLoading,
		ToolbarMenu,
		ToolbarMenuItem,
		Pagination,
		PaginationSkeleton,
		Tag
	} from 'carbon-components-svelte';

	import { page } from '$app/stores';
	import PreDebug from '@/components/dev/PreDebug.svelte';
	import { onMount } from 'svelte';
	import { KeyIcon, KeyRound, RefreshCcw, Settings, Settings2 } from 'lucide-svelte';
	import {
		delay,
		fetchDataSelf,
		generateConnectionString,
		humanizedTimeDifference,
		toTitleCase,
		ulidToDate
	} from '@/utils/utils';
	import type { CommonPagination, EnginePaginatedDto } from '@/types/dtos';
	import type { DBMysql } from '@/types/entities';
	import { goto } from '$app/navigation';
	import { DBMysqlStatus } from '@/types/enums';

	export let data: EnginePaginatedDto<DBMysql>;
	const headers = [
		{ key: 'dbName', value: 'DB Name' },
		{ key: 'dbUser', value: 'DB User' },
		{ key: 'created', value: 'Created' }
	];

	const rows = data.content;

	let loading = true;
	onMount(() => {
		setTimeout(() => {
			loading = false;
		}, 100);
		console.log(data);
	});

	const editDBUrl = `/databases/mysql/:id`;

	// let selectedItemIdx: number | undefined = undefined;
	let selectedItemIdx: number | undefined = undefined;
	let selectedItem: DBMysql | undefined = undefined;

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
		regularData: EnginePaginatedDto<DBMysql>;
		searchList: DBMysql[];
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
		)) as unknown as EnginePaginatedDto<DBMysql>;
		console.log('regularData', res);

		// delay for 500ms
		await delay(300);
		dataStuff.regularData = res;
		dataStuff.status = 'DATA';
	}

	async function search() {
		//
		dataStuff.status = 'SEARCHING';
		const res = await fetchDataSelf(
			{
				query: dataStuff.query
			},
			'POST'
		);
		if (res) {
			console.log('x', res);
			dataStuff.searchList = res.payload;
		} else {
			// Something better maybe
			dataStuff.searchList = [];
		}

		console.warn('[DEBUG]: Search Response', res);

		// await new Promise((resolve) => setTimeout(resolve, 1000));
		await delay(300);
		dataStuff.status = 'SEARCH_RESULT';
	}

	let timer: NodeJS.Timeout;

	const debouncedUserInput = (v: string) => {
		// https://svelte.dev/repl/f55e23d0bf4b43b1a221cf8b88ef9904?version=3.12.1
		clearTimeout(timer);
		timer = setTimeout(async () => {
			if (dataStuff.query.trim()) {
				console.log('dataStuff.query', dataStuff.query);
				await search();
			} else {
				console.log('invalid search queries');
				handleSearchClear();
			}
		}, 300);
	};
	function handleSearchInput({ target: { value } }: any) {
		debouncedUserInput(value);
	}

	async function handleSearchClear() {
		// dataStuff.query = '';
		dataStuff.status = 'DATA';
		console.log('searchClear', dataStuff.query);
	}

	onMount(async () => {
		await fetchRegularData();
	});
</script>

{#if loading}
	<DataTableSkeleton />
{:else}
	<DataTable title={'MySQL Databases'} description="Manage mysql databases">
		<Toolbar>
			<ToolbarContent>
				<!-- on:input={handleSearchInput} -->
				<ToolbarSearch
					bind:value={dataStuff.query}
					on:keyup={handleSearchInput}
					on:clear={handleSearchClear}
				/>

				<Button href={$page.url.href + '/new'}>Create Database</Button>
			</ToolbarContent>
		</Toolbar>

		{#if dataStuff.status === 'DATA'}
			{#if dataStuff.regularData.content.length}
				<Table>
					<TableHead>
						<TableRow>
							{#each headers as h, idx}
								<TableHeader style={h.key == 'actions' ? '' : ''}>{h.value}</TableHeader>
							{/each}
						</TableRow>
					</TableHead>
					<TableBody>
						{#each dataStuff.regularData.content as row, idx}
							<TableRow
								on:click={() => {
									goto(editDBUrl.replace(':id', row['id']));
								}}
							>
								<TableCell style=""
									>{row['dbName']}
									{#if row['status'] === DBMysqlStatus.CREATED}
										<!-- nothing -->
									{:else if row['status'] === DBMysqlStatus.FAILED}
										<Tag type="magenta">Failed</Tag>
									{:else}
										<Tag type="high-contrast">{row['status']}</Tag>
									{/if}
								</TableCell>
								<TableCell style="">{row['dbUser']}</TableCell>
								<TableCell style="flex ">
									{humanizedTimeDifference(ulidToDate(row['id']))}
									<!-- <Button
										class="rounded"
										on:click={() => {
											// selectedItemIdx = idx;
											selectedItem = row;
										}}
										kind="secondary"
										icon={KeyIcon}
										iconDescription="Connections"
									></Button> -->
									<!-- <Button
										size="small"
										href={editDBUrl.replace(':id', row['id'])}
										kind="secondary"
										iconDescription="Settings"
									>
										Modify</Button
									> -->
								</TableCell>
							</TableRow>
						{/each}
					</TableBody>
				</Table>
			{:else}
				<div class="w-full py-4 flex items-center justify-center bg-[#f4f4f4]">No data found!</div>
			{/if}
		{:else if dataStuff.status === 'LOADING' || dataStuff.status === 'SEARCHING'}
			<DataTableSkeleton showHeader={false} showToolbar={false} class=" -my-1" />
			<!-- <div class="w-full py-4 flex items-center justify-center bg-[#f4f4f4]">
				<InlineLoading class="h-10 w-10 " description={toTitleCase(dataStuff.status)} />
			</div> -->
		{:else if dataStuff.status === 'SEARCH_RESULT'}
			{#if dataStuff.searchList.length}
				<Table>
					<TableBody>
						{#each dataStuff.searchList as row, idx}
							<TableRow>
								<TableCell>{row['dbName']}</TableCell>
								<TableCell>{row['dbUser']}</TableCell>
								<TableCell class="flex justify-end h-auto gap-2">
									<Button
										on:click={() => {
											// selectedItemIdx = idx;
											selectedItem = row;
										}}
										kind="secondary"
										icon={KeyIcon}
										iconDescription="Connections"
									></Button>
									<Button
										href={editDBUrl.replace(':id', row['id'])}
										kind="secondary"
										icon={Settings2}
										iconDescription="Settings"
									></Button>
								</TableCell>
							</TableRow>
						{/each}
					</TableBody>
				</Table>
			{:else}
				<div class="w-full py-4 flex items-center justify-center bg-[#f4f4f4]">
					No result found!
				</div>
			{/if}
		{/if}

		{#if dataStuff.status === 'DATA' && !dataStuff.regularData.empty}
			<Pagination
				totalItems={dataStuff.regularData.totalElements}
				pageSizes={[5, 10, 25]}
				page={dataStuff.regularData.pageable.pageNumber + 1}
				pageSize={dataStuff.regularData.pageable.pageSize}
				on:change={async (e) => {
					if (e.detail?.pageSize) {
						dataStuff.regularData.pageable.pageSize = e.detail?.pageSize;
						dataStuff.regularData.pageable.pageNumber = 0;
						console.warn('page size', e.detail?.pageSize);
					}
					if (e.detail?.page) {
						dataStuff.regularData.pageable.pageNumber = e.detail?.page - 1;
						console.warn('page size', e.detail?.page);
					}
					await fetchRegularData();
					console.log('on change', e);
				}}
				on:click:button--previous
				on:click:button--next
			/>
		{:else if dataStuff.status === 'SEARCH_RESULT'}
			<!--No pagination right now-->
		{:else if dataStuff.status === 'LOADING' || dataStuff.status === 'SEARCHING'}
			<!-- <PaginationSkeleton /> -->
		{/if}
	</DataTable>

	<PreDebug {data} />
{/if}

{#if selectedItem != undefined}
	<Modal
		preventCloseOnClickOutside
		open={true}
		on:close={() => {
			selectedItem = undefined;
		}}
		modalHeading={'DB Connection: ' + selectedItem['dbName']}
		passiveModal
	>
		<div>
			<h2>DB Name</h2>
			<CodeSnippet type="single">{selectedItem['dbName']}</CodeSnippet>
		</div>
		<div>
			<h2>DB User</h2>
			<CodeSnippet type="single">{selectedItem['dbUser']}</CodeSnippet>
		</div>
		<div>
			<h2>DB Password</h2>
			<CodeSnippet type="single">{selectedItem['dbPassword']}</CodeSnippet>
		</div>

		<div>
			<h2>Connection String (Remote)</h2>
			<CodeSnippet type="single">
				{generateConnectionString('mysql', selectedItem, 'remote')}
			</CodeSnippet>
		</div>

		<div>
			<h2>Connection String (Remote)</h2>
			<CodeSnippet type="single">
				{generateConnectionString('mysql', selectedItem, 'local')}
			</CodeSnippet>
		</div>

		<PreDebug data={selectedItem}></PreDebug>
	</Modal>
{/if}
<pre>
	{JSON.stringify(dataStuff, null, 2)}
</pre>
