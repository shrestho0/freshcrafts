<script lang="ts">
import PreDebug from '@/components/dev/PreDebug.svelte';
import { InternalNewProjectType } from '@/types/enums';
import {
	Button,
	DataTable,
	DataTableSkeleton,
	Toolbar,
	ToolbarContent,
	ToolbarMenu,
	ToolbarMenuItem,
	ToolbarSearch
} from 'carbon-components-svelte';

import { onMount } from 'svelte';

let loading = true;
let hasNext = false;

onMount(async () => {
	await loadGithubRepos();
	loading = false;
});

let repos: any[] = [];

let repos_to_show: any[] = [];

let search_query: string = '';

let currentPage = 1;
let perPage = 10;

async function loadGithubRepos(page: number = currentPage, per_page: number = perPage) {
	const res = await fetch(`?per_page=${per_page}&page=${page}`, {
		method: 'GET',
		headers: {
			'x-freshcraft-project-type': InternalNewProjectType.GITHUB_REPO
		}
	})
		.then((res) => res.json())
		.catch((e) => {
			console.error(e);
			return {
				success: false
			};
		});
	console.log(res.repos);
	repos = res?.repos?.data;
	repos_to_show = repos;
}

function nextPage() {
	currentPage++;
	loadGithubRepos(currentPage);
}

function searchRepos() {
	const q = search_query.trim();
	if (q === '') {
		repos_to_show = repos;
		return;
	}
	repos_to_show = repos.filter((repo: any) => {
		return repo.name.includes(q);
	});
	// loadGithubRepos(search_query);
}
</script>

{#if loading}
	<DataTableSkeleton />
{:else}
	<DataTable
		title={`Github Repositories: ${repos.length}`}
		description="Import Git Repository"
		size="short"
		headers={[
			{ key: 'name', value: 'Name' },
			{ key: 'private', value: 'Private' },
			{ key: 'updated_at', value: 'updated_at' },
			{ key: 'url', value: 'Url' }
		]}
		rows={repos_to_show}
	>
		<Toolbar>
			<ToolbarContent>
				<ToolbarSearch
					bind:value={search_query}
					name="one-time-password"
					on:input={(e) => {
						searchRepos();
					}}
				/>
			</ToolbarContent>
		</Toolbar>
	</DataTable>
	{hasNext}
	{#if hasNext}
		<Button on:click={() => nextPage()}>Load More</Button>
	{/if}
{/if}

<PreDebug data={repos} />
