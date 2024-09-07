<script lang="ts">
import WhatDeviceIsThat from '@/components/dev/WhatDeviceIsThat.svelte';
import {
	ClickableTile,
	RadioTile,
	Button,
	SelectableTile,
	Tile,
	TileGroup,
	Loading
} from 'carbon-components-svelte';
import FromDevice from './FromDevice.svelte';
import FromGithub from './FromGithub.svelte';
import type { EngineSystemConfigResponseDto } from '@/types/dtos';
import PreDebug from '@/components/dev/PreDebug.svelte';
import { onMount } from 'svelte';
import CarbonWarning from '@/ui/icons/CarbonWarning.svelte';
import CarbonLogoGithub from '@/ui/icons/CarbonLogoGithub.svelte';

let loading = true;

let items = [
	{
		title: 'From Github',
		description: 'Choose a project from github',
		value: 'fromGithub'
	},
	{
		title: 'From Device',
		description: 'Upload a file from your device',
		value: 'fromDevice'
	}
];
let selected: number | null = null;

export let data;
const sysConf = data?.sysConf as EngineSystemConfigResponseDto;

onMount(() => {
	loading = false;
});
</script>

{#if !loading}
	<h1 class="text-2xl">New Project</h1>
	<p class="text-gray-500">Choose a project source</p>
	<Tile class="warning my-2 flex gap-2 items-center">
		<CarbonWarning />
		Currently supporting nodejs projects only
	</Tile>

	<div class="w-full grid md:grid-cols-2 gap-4 py-4">
		{#each items as item, idx}
			<SelectableTile
				value={items[idx].value}
				selected={selected != null && items[selected].value === item.value}
				disabled={selected != null && items[selected].value !== item.value}
				on:select={(e) => {
					selected = idx;
				}}
				on:deselect={(e) => {
					selected = null;
				}}
				class="flex items-center justify-between px-4 py-6  "
			>
				<div class="w-full flex items-center justify-center gap-3">
					<h2 class="text-xl">{item.title}</h2>
					<CarbonLogoGithub />
				</div>
			</SelectableTile>
		{/each}
	</div>

	{#if selected === null}
		<p class="">Select an option</p>
	{:else if items[selected].value === 'fromDevice'}
		<FromDevice />
	{:else if items[selected].value === 'fromGithub'}
		{#if sysConf?.systemUserOauthGithubEnabled}
			<FromGithub />
		{:else}
			<Tile class="flex items-center justify-center text-lg gap-2">
				<CarbonWarning class="h-6 w-6" />
				Github not enabled. Enable from
				<a href="/settings" class="text-[var(--cds-link-01)] hover:text-[var(--cds-link-02)]"
					>settings</a
				> page
			</Tile>
		{/if}
	{/if}
	<PreDebug data={sysConf} />

	<!-- {#if items[selected].value === 'fromDevice'}
	<FromDevice />
{:else if selected === 1}
	<FromGithub />
{:else}
	<p>Select an option</p>
{/if} -->
{/if}
