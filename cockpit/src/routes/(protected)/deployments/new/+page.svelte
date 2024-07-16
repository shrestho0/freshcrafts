<script lang="ts">
	import WhatDeviceIsThat from '@/components/dev/WhatDeviceIsThat.svelte';
	import {
		ClickableTile,
		RadioTile,
		SelectableTile,
		Tile,
		TileGroup
	} from 'carbon-components-svelte';
	import { Upload, LogoGithub } from 'carbon-icons-svelte';
	import FromDevice from './FromDevice.svelte';
	import FromGithub from './FromGithub.svelte';

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
	let selected: number | null = 1;
</script>

<h1 class="text-2xl">New Deployment</h1>

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
				<LogoGithub />
			</div>
		</SelectableTile>
	{/each}
</div>

{#if selected === null}
	<p class="">Select an option</p>
{:else if items[selected].value === 'fromDevice'}
	<FromDevice />
{:else if items[selected].value === 'fromGithub'}
	<FromGithub />
{/if}

<!-- {#if items[selected].value === 'fromDevice'}
	<FromDevice />
{:else if selected === 1}
	<FromGithub />
{:else}
	<p>Select an option</p>
{/if} -->
