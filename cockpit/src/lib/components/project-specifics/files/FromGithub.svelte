<script lang="ts">
	import PreDebug from "@/components/dev/PreDebug.svelte";
	import { InternalNewProjectType } from "@/types/enums";
	import {
		InlineLoading,
		Search,
		SelectableTile,
		Tile,
	} from "carbon-components-svelte";
	import { onMount } from "svelte";
	import { humanizedTimeDifference } from "@/utils/utils";
	import type { DetailedRepository } from "@/types/github-webhook-types";
	import { browser } from "$app/environment";
	import ArrowRightIcon from "@/ui/icons/ArrowRightIcon.svelte";
	import CarbonQuery from "@/ui/icons/CarbonQuery.svelte";
	import CarbonLocked from "@/ui/icons/CarbonLocked.svelte";

	// FIXME: Server side errors are not handled

	let loading = true;
	let original_repo_data = {
		total_count: 0,
		repos: [],
	} as {
		total_count: number;
		repos: DetailedRepository[];
	};

	const savedGithubStuff = {
		selected_repo: undefined,
		select_status: "showing_repos",
		project_url: "",
		// newly_created_project: undefined
		// file_data: undefined
	} as {
		selected_repo: any;
		select_status:
			| "showing_repos"
			| "repo_selected"
			| "repo_uploading"
			| "confirmed_selection"
			| "leaving_page";
		// FIXME: use appropriate type
		// newly_created_project: any;
		project_url: string;
		// file_data: any;
	};

	onMount(async () => {
		await loadGithubRepos();
		loading = false;
	});

	let repos_to_show: any[] = [];
	async function loadGithubRepos() {
		const res = await fetch(``, {
			method: "PATCH",
			headers: {
				"x-freshcraft-project-type":
					InternalNewProjectType.LIST_GITHUB_REPO,
			},
		})
			.then((res) => res.json())
			.catch((e) => {
				console.error(e);
				return {
					success: false,
				};
			});
		console.log(res.repos);
		if (res.success) {
			original_repo_data.repos = res.repos;
			original_repo_data.total_count = res.total_count;
			repos_to_show = res.repos;
		} else {
			handleError();
		}
	}

	function handleError() {}
	let search_query: string = "";
	$: {
		if (search_query.trim() === "") {
			// repos_to_show = original_repo_data.repos;
			if (original_repo_data?.repos?.length > 0) {
				repos_to_show = original_repo_data.repos;
			}
		} else {
			// repos_to_show = original_repo_data.repos.filter((repo: any) => {
			// 	return repo.name.includes(search_query);
			// });
			repos_to_show = [];
			if (original_repo_data?.repos?.length > 0) {
				for (const repo of original_repo_data.repos) {
					if (
						repo?.name
							?.toLowerCase()
							.includes(search_query?.toLowerCase())
					) {
						repos_to_show.push(repo);
					}
				}
			}
		}
	}

	const handleRepoSelect = (repo: any) => {
		savedGithubStuff.selected_repo = repo;
		savedGithubStuff.select_status = "repo_selected";
	};

	let err_msg = "";

	async function handleContinue() {
		if (!browser) return;

		savedGithubStuff.select_status = "repo_uploading";
		// continue to next step
		console.log("repo to send", savedGithubStuff.selected_repo);
		if (savedGithubStuff.selected_repo) {
			window.addEventListener("beforeunload", function (e) {
				if (savedGithubStuff.select_status == "leaving_page")
					return null;
				e.preventDefault();
			});

			//
			const res = await fetch("", {
				method: "PATCH",
				headers: {
					"Content-Type": "application/json",
					"x-freshcraft-project-type":
						InternalNewProjectType.CREATE_PROJECT_FROM_GITHUB,
				},
				body: JSON.stringify({
					...savedGithubStuff.selected_repo,
				}),
			})
				.then((res) => res.json())
				.catch((e) => {
					console.error(e);
					return {
						success: false,
					};
				});

			console.log(res);
			if (res?.success) {
				// redirect to /projects/:id/setup
				// window.removeEventListener('beforeunload', function (e) {
				// 	window.location.href = `/projects/${res.project_id}/setup`;
				// });
				// savedGithubStuff.newly_created_project = res?.project;
				const proj = res?.payload;
				savedGithubStuff.project_url = `/projects/${proj?.id}/initial-setup`;
				savedGithubStuff.select_status = "leaving_page";
				window.location.href = savedGithubStuff.project_url;
			} else {
				err_msg = res?.message || "Some error occurred";
			}

			console.log(res);
		}
	}

	function closedWin() {}

	const headers = [
		{ key: "name", value: "Name" },
		// { key: 'private', value: 'Private' },
		{ key: "updated_at", value: "updated_at" },
		{ key: "url", value: "Url" },
	];
</script>

<div class="flex flex-col gap-3 w-full">
	{#if loading}
		<div class="flex w-full items-center justify-cente">
			<InlineLoading description="Loading Repositories" />
		</div>
	{:else}
		<!-- <TableBody> -->

		{#if savedGithubStuff.select_status == "showing_repos"}
			<div class="title text-2xl">Import Git Repository</div>
			<div class="boxes">
				<Search icon={CarbonQuery} bind:value={search_query} />
			</div>
			{#each repos_to_show as repo, idx}
				<!-- <TableRow> -->
				<!-- <TableCell> -->
				<SelectableTile
					selected={savedGithubStuff.selected_repo == repo}
					on:click={() => handleRepoSelect(repo)}
					class="w-full flex gap-2 my-1 justify-between items-center"
				>
					<div class=" flex flex-col flex-col">
						<div class="flex gap-2">
							{repo["name"]}
							{#if repo["private"]}
								<CarbonLocked />
							{:else}
								<!-- <Unlocked /> -->
							{/if}
						</div>
						<div class="font-light">
							{humanizedTimeDifference(
								new Date(repo["pushed_at"]),
							)}
						</div>
					</div>
				</SelectableTile>

				<!-- {#each headers as h, idx}
				<TableCell>{repo[h.key]}</TableCell>
				{/each} -->
				<!-- </TableRow> -->
			{/each}
			<!-- </TableBody> -->
		{:else if savedGithubStuff.select_status == "repo_selected"}
			<SelectableTile
				selected={true}
				class="w-full flex gap-2 my-2 justify-between items-center"
				on:click={() => {
					savedGithubStuff.select_status = "showing_repos";
					savedGithubStuff.selected_repo = undefined;
				}}
			>
				<div class=" flex flex-col flex-col">
					<div class="flex gap-2">
						{savedGithubStuff.selected_repo["name"]}
						{#if savedGithubStuff.selected_repo["private"]}
							<CarbonLocked />
						{:else}
							<!-- <Unlocked /> -->
						{/if}
					</div>
					<div class="font-light">
						Pushed at:
						{humanizedTimeDifference(
							new Date(
								savedGithubStuff.selected_repo["pushed_at"],
							),
						)}
						| Updated at:
						{humanizedTimeDifference(
							new Date(
								savedGithubStuff.selected_repo["updated_at"],
							),
						)}
					</div>
				</div>
				<!-- More details will be here later -->
			</SelectableTile>
			<div class="w-full flex gap-4">
				<button
					class="w-full flex items-center justify-center gap-2 bg-[var(--cds-interactive-02)] text-white py-2"
					on:click={handleContinue}
				>
					Proceed <ArrowRightIcon class="w-5 h-5" />
				</button>
			</div>
		{:else if savedGithubStuff.select_status == "repo_uploading"}
			<!-- warning: don't close this page -->
			<Tile class="w-full flex flex-col items-center justify-center">
				<InlineLoading class="w-auto" />
				<div class="text-center">
					<div class="text-lg font-normal">
						Downloading repository from github.
					</div>
					<div class="text-md py-1">(Do not close this page.)</div>
				</div>
			</Tile>
		{:else if savedGithubStuff.select_status == "leaving_page"}
			<Tile>
				<div class="flex flex-col items-center justify-center">
					<div class="text-lg font-normal">
						Redirecting to project setup page.
					</div>
					<a class="text-md py-1" href={savedGithubStuff.project_url}
						>Click here if not redirected</a
					>
				</div>
			</Tile>
		{:else}
			<p>Something went wrong</p>
		{/if}
		{#if repos_to_show.length == 0}
			<p>No repositories found</p>
		{/if}
	{/if}
</div>

<!-- <PreDebug data={original_repo_data} /> -->
