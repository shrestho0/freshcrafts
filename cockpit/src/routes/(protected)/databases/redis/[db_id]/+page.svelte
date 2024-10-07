<script lang="ts">
	import { browser } from "$app/environment";
	import { invalidateAll } from "$app/navigation";
	import { page } from "$app/stores";
	import PreDebug from "@/components/dev/PreDebug.svelte";
	import type {
		EngineCommonResponseDto,
		EngineRedisDBGetOneError,
	} from "@/types/dtos";
	import type { DBRedis } from "@/types/entities";
	import { DBRedisStatus } from "@/types/enums";
	import {
		Button,
		CodeSnippet,
		InlineLoading,
		Tile,
		RadioButtonGroup,
		RadioButton,
	} from "carbon-components-svelte";
	import { source } from "sveltekit-sse";
	import UpdateDbModal from "./UpdateDBAccessModal.svelte";
	import DeleteDbModal from "./DeleteDBModal.svelte";
	import { enhance } from "$app/forms";

	import {
		generateRedisShellCode,
		generateRedisConnUrl,
		generateRedisEnv,
		getRedisConnectionHost,
		getRedisConnectionPort,
	} from "@/utils/db-stuff";
	import OctagonX from "@/ui/icons/OctagonX.svelte";
	import OctagonAlert from "@/ui/icons/OctagonAlert.svelte";
	import CarbonApiKey from "@/ui/icons/CarbonApiKey.svelte";
	import CarbonDelete from "@/ui/icons/CarbonDelete.svelte";

	export let data: EngineCommonResponseDto<DBRedis, EngineRedisDBGetOneError>;

	if (browser && data?.payload && data?.payload?.status != DBRedisStatus.OK) {
		// (data?.payload?.status == DBRedisStatus.REQUESTED ||
		// 	data?.payload?.status == DBRedisStatus.PENDING_DELETE)
		console.log("sse hit hobe");
		const { db_id } = $page.params;

		const sse_url = `/sse/databases/redis/${db_id}`;

		const value = source(sse_url).select("message");
		if (value) {
			setTimeout(() => {
				invalidateAll();
			}, 1000);
			console.log("Value", value.json());
		}
	}

	let updateDBAccessModalOpen = false;
	let updateDBNameModalOpen = false;
	let deleteModalOpen = false;

	const connectionOptions = {
		remote_or_local: "remote",
		conn_string_types: ["command", "connection-uri", ".env", "raw"],
		selectedIdx: 0,
	} as {
		remote_or_local: "remote" | "local";
		conn_string_types: string[];
		selectedIdx: number;
	};
</script>

<div class="w-full">
	{#if data?.success}
		{#if data.payload.status === DBRedisStatus.OK}
			<div class="w-full">
				<Tile class=" ">
					<div class="title py-2 text-lg">
						<div class="col-span-1">
							<div class="flex gap-2">
								<RadioButtonGroup
									name="remote_or_local"
									bind:selected={connectionOptions.remote_or_local}
								>
									<RadioButton
										labelText="Remote"
										value="remote"
									/>
									<RadioButton
										labelText="Local"
										value="local"
									/>
								</RadioButtonGroup>
							</div>
						</div>
					</div>
					<div class="the-conn-string w-full">
						<div class="p-2">
							Note: Use <CodeSnippet
								code={data.payload.dbPrefix}
								type="inline"
							>
								{data.payload.dbPrefix}</CodeSnippet
							> as the prefix for your keys
						</div>
						<div
							role="navigation"
							class="bx--tabs bx--tabs--container"
						>
							<ul
								role="tablist"
								class="bx--tabs__nav bx--tabs__nav--hidden"
							>
								{#each connectionOptions.conn_string_types as c, idx}
									<li
										tabindex="-1"
										role="presentation"
										class="bx--tabs__nav-item {idx ==
										connectionOptions.selectedIdx
											? 'bx--tabs__nav-item--selected'
											: ''}"
									>
										<!-- svelte-ignore a11y-click-events-have-key-events -->
										<!-- svelte-ignore a11y-missing-attribute -->
										<a
											role="button"
											tabindex="-69"
											on:click|preventDefault={() => {
												connectionOptions.selectedIdx =
													idx;
											}}
											class="bx--tabs__nav-link">{c}</a
										>
									</li>
								{/each}
							</ul>
						</div>
						{#if connectionOptions.selectedIdx == 0}
							<div>
								<CodeSnippet
									type="multi"
									light={true}
									code={generateRedisShellCode(
										data.payload,
										connectionOptions.remote_or_local ===
											"remote",
									)}
								></CodeSnippet>
							</div>
						{:else if connectionOptions.selectedIdx == 1}
							<div>
								<CodeSnippet
									type="multi"
									light={true}
									showMoreLess={false}
									code={generateRedisConnUrl(
										data.payload,
										connectionOptions.remote_or_local ===
											"remote",
									)}
								></CodeSnippet>
							</div>
						{:else if connectionOptions.selectedIdx == 2}
							<CodeSnippet
								type="multi"
								light={true}
								code={generateRedisEnv(
									data.payload,
									connectionOptions.remote_or_local ===
										"remote",
								)}
							></CodeSnippet>
						{:else if connectionOptions.selectedIdx == 3}
							<div class="flex flex-col gap-2 py-2">
								Connection Host:
								<CodeSnippet
									light={true}
									type="single"
									code={getRedisConnectionHost(
										connectionOptions.remote_or_local ===
											"remote",
									)}
								></CodeSnippet>
								Connection Port:
								<CodeSnippet
									light={true}
									type="single"
									code={getRedisConnectionPort(
										connectionOptions.remote_or_local ===
											"remote",
									)}
								></CodeSnippet>
								DB Prefix:

								<CodeSnippet
									light={true}
									type="single"
									code={data.payload.dbPrefix}
								></CodeSnippet>
								Username:
								<CodeSnippet
									light={true}
									type="single"
									code={data.payload.username}
								></CodeSnippet>
								Password:
								<CodeSnippet
									light={true}
									type="single"
									code={data.payload.password}
								></CodeSnippet>
							</div>
						{/if}
					</div>
				</Tile>
				<div class="w-full my-4">
					<Button
						class="w-full"
						icon={CarbonApiKey}
						kind="secondary"
						on:click={() => {
							updateDBAccessModalOpen = true;
						}}>Update Access</Button
					>

					<!-- <Button
						class="w-full"
						icon={CarbonEdit}
						kind="secondary"
						on:click={() => {
							// updateDBAccessModalOpen = true;
							updateDBNameModalOpen = true;
						}}>Update Database Name</Button
					> -->

					<Button
						class="w-full"
						icon={CarbonDelete}
						kind="danger"
						on:click={() => {
							deleteModalOpen = true;
						}}>Delete Database</Button
					>
				</div>
			</div>
			<div
				class="shell-access w-full flex items-center justify-center p-8"
				style="border:2px dashed gray;"
			>
				Redis Shell Access (Coming Soon)
			</div>
		{:else if data.payload.status === DBRedisStatus.FAILED}
			<Tile class="w-full flex flex-col items-center justify-center">
				<OctagonX class=" h-12 w-12" />
				<div class="text-center">
					<div class="text-lg font-normal">
						Failed to create Redis Database
					</div>
					<div class="text-md py-1">
						Reason of failure: <span class=" "
							>{data.payload.reasonFailed}
						</span>
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
		{:else if data.payload.status === DBRedisStatus.PENDING_DELETE}
			<Tile class="w-full flex flex-col items-center justify-center">
				<InlineLoading class="w-auto" />
				<div class="text-center">
					<div class="text-lg font-normal">
						Your request to delete the database is being processed
					</div>
					<div class="text-md py-1">
						(You will be notified when complete)
					</div>
				</div>
			</Tile>
		{:else if data.payload.status === DBRedisStatus.REQUESTED}
			<Tile class="w-full flex flex-col items-center justify-center">
				<!-- <OctagonX class=" h-12 h-12" /> -->
				<InlineLoading class="w-auto" />
				<div class="text-center">
					<div class="text-lg font-normal">
						Your request is being processed
					</div>
					<div class="text-md py-1">
						(You will be notified when complete)
					</div>
				</div>
			</Tile>
		{:else if data.payload.status === DBRedisStatus.UPDATE_REQUESTED}
			<Tile class="w-full flex flex-col items-center justify-center">
				<InlineLoading class="w-auto" />
				<div class="text-center">
					<div class="text-lg font-normal">
						{data?.payload?.updateMessage ??
							"Your request to update the database is being processed"}
					</div>
					<div class="text-md py-1">
						(You will be notified when complete)
					</div>
				</div>
			</Tile>
		{:else if data.payload.status === DBRedisStatus.UPDATE_FAILED}
			<Tile class="w-full flex flex-col items-center justify-center">
				<OctagonX class=" h-12 h-12" />
				<div class="text-center">
					<div class="text-lg font-normal">
						Failed to update Redis Database
					</div>
					<div class="text-md py-1">
						Reason of failure: <span class=" "
							>{data?.payload?.reasonFailed ?? "Unknown error"}
						</span>
					</div>

					<!-- delete db form model open button -->
					<form action="?/revert" method="post" use:enhance>
						<Button
							type="submit"
							class="w-full "
							kind="secondary"
							size="small">Revert Changes</Button
						>
					</form>
				</div>
			</Tile>
		{/if}
	{:else}
		<Tile class="w-full flex flex-col items-center justify-center">
			<OctagonAlert class=" h-12 h-12" />
			<div class="text-center text-lg font-normal">
				No Redis Database found <br />
			</div>
		</Tile>
	{/if}
</div>
<!-- <PreDebug {data} /> -->

{#if data?.payload}
	<UpdateDbModal bind:open={updateDBAccessModalOpen} />
	<DeleteDbModal bind:open={deleteModalOpen} bind:db={data.payload} />
{/if}
