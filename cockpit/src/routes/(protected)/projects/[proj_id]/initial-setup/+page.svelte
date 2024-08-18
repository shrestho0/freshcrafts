<script lang="ts">
import PreDebug from '@/components/dev/PreDebug.svelte';
import { ProjectSetupCommand } from '@/types/enums.js';
import {
	SelectableTile,
	Tab,
	TabContent,
	Tabs,
	TextArea,
	TextInput,
	Tile
} from 'carbon-components-svelte';
import dotenv from 'dotenv';
import { PlusSquare } from 'lucide-svelte';
export let data;
const projectSetup = {
	name: '',
	nameChecking: false,
	nameOk: true,
	isInvalid: false,
	envFileContent: '',
	envKV: [{ key: '', value: '' }],
	selectedIdx: 0,
	messages: ['Env File Content', 'Key-Value Pairs'],
	placeholder: `\nKEY1=value1\nKEY2=value2\n...`,
	errorMessage: ''
} as {
	name: string | number | null | undefined;
	nameChecking: boolean;
	nameOk: boolean;
	isInvalid: boolean;
	envFileContent: string;
	envKV: { key: string; value: string }[];
	selectedIdx: number;
	messages: string[];
	placeholder: string;
	errorMessage: string;
};

$: console.log(projectSetup);
$: if (projectSetup.selectedIdx === 0) {
	console.log('envFileContent', projectSetup.envFileContent);
	// sync with envKV
	projectSetup.errorMessage = '';
	try {
		// projectSetup.envKV = projectSetup.envFileContent.split('\n').map((line) => {
		// 	if (!line) return { key: '', value: '' };
		// 	if (!line.includes('=')) throw new Error('Invalid format');
		// 	const [key, value] = line.split('=', 2);

		// 	return { key, value };
		// });
		// projectSetup.envKV = dotenv.parse(projectSetup.envFileContent)) as unknown as {
		// 	key: string;
		// 	value: string;
		// }[];

		projectSetup.envKV = Object.entries(
			dotenv.parse(projectSetup.envFileContent) as unknown as {
				key: string;
				value: string;
			}
		).map(([key, value]) => ({ key, value }));
	} catch (e: any) {
		projectSetup.errorMessage = e?.message;
	}
} else {
	// console.log('envKV', projectSetup.envKV);
	try {
		// join only if key and value are present
		// projectSetup.envFileContent = projectSetup.envKV
		// 	.filter(({ key, value }) => key && value)
		// 	.map(({ key, value }) => `${key}=${value}`)
		// 	.join('\n');

		projectSetup.envFileContent = projectSetup.envKV
			.map(({ key, value }) => `${key}=${value}`)
			.join('\n');
	} catch (e: any) {
		projectSetup.errorMessage = e?.message;
	}
}
async function checkName() {
	const res = await fetch('', {
		method: 'PATCH',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({
			command: ProjectSetupCommand.CHECK_UNIQUE_NAME,
			data: projectSetup.name?.toString()?.trim()
		})
	})
		.then((res) => res.json())
		.catch((e) => console.error(e));
	console.log(res);
	return res?.success;
}

let timer: NodeJS.Timeout;
const debouncedUserInput = async (v: string) => {
	// https://svelte.dev/repl/f55e23d0bf4b43b1a221cf8b88ef9904?version=3.12.1
	clearTimeout(timer);
	projectSetup.nameChecking = true;
	projectSetup.errorMessage = '';
	const pattern = /^[a-zA-Z0-9_-]+$/;
	if (!pattern.test(v)) {
		projectSetup.isInvalid = true;
		projectSetup.nameChecking = false;
		return;
	} else {
		projectSetup.isInvalid = false;
	}
	timer = setTimeout(async () => {
		const ok = await checkName();
		projectSetup.nameOk = ok;
		if (ok) {
			console.log('setupStuff', projectSetup.name);
		} else {
			console.log('invalid search queries');
		}
		projectSetup.nameChecking = false;
	}, 500);
};
function handleSearchInput({ target: { value } }: any) {
	debouncedUserInput(value);
}
</script>

{JSON.stringify(projectSetup)}

<!-- <button
	class="w-full"
	on:click={() => {
		checkName();
	}}>sds</button
> -->

<!-- Initial Setup Page -->
<div class="intro">
	<h1 class="text-[24px]">Initial Setup</h1>
	<p class="text-[16px]">
		Let's start by setting up the environment for your project. You can either upload your .env file
		or add key-value pairs
	</p>
</div>

<div class="text-[16px] py-2">Project Name (unique):</div>
<div>Note: If content changed or empty, do nothing;else, check every 500 ms interval</div>

<TextInput
	bind:value={projectSetup.name}
	on:keyup={handleSearchInput}
	invalid={Boolean(!projectSetup.nameOk && projectSetup.name)}
	invalidText="Project Name already exists"
	placeholder="anonymous-monkey"
	pattern="^[a-zA-Z0-9_-]+$"
	helperText={projectSetup.nameChecking
		? 'Checking...'
		: projectSetup.name && projectSetup.isInvalid
			? 'Invalid characters'
			: projectSetup.name && projectSetup.nameOk
				? 'Name is available'
				: projectSetup.name
					? 'Name is not available'
					: 'Only letters, numbers, hyphens, and underscores are allowed'}
/>

<div class="text-[16px] py-2">Env Setup</div>
<div class="w-full flex gap-0 items-center justify-between">
	{#each projectSetup.messages as option, idx}
		<button
			role="tab"
			tabindex="-1"
			class="w-full focus:outline-none text-center bx--tile bx--tile--selectable"
			class:bx--tile--is-selected={projectSetup.selectedIdx === idx}
			on:click={() => {
				if (projectSetup.selectedIdx === idx) return;
				projectSetup.selectedIdx = idx;
			}}
		>
			<span class="bx--tile__checkmark"
				><svg
					xmlns="http://www.w3.org/2000/svg"
					viewBox="0 0 32 32"
					fill="currentColor"
					preserveAspectRatio="xMidYMid meet"
					width="16"
					height="16"
					role="img"
					aria-label="Tile checkmark"
					><title>Tile checkmark</title><path
						d="M16,2A14,14,0,1,0,30,16,14,14,0,0,0,16,2ZM14,21.5908l-5-5L10.5906,15,14,18.4092,21.41,11l1.5957,1.5859Z"
					></path><path
						fill="none"
						d="M14 21.591L9 16.591 10.591 15 14 18.409 21.41 11 23.005 12.585 14 21.591z"
						data-icon-path="inner-path"
					></path></svg
				></span
			> <span class="bx--tile-content">{option}</span>
		</button>
	{/each}
</div>
<!-- {JSON.stringify(projectSetup)} -->

{#if projectSetup.selectedIdx === 0}
	<TextArea
		placeholder={projectSetup.placeholder}
		bind:value={projectSetup.envFileContent}
		helperText="Put content of your .env file here"
	/>
{:else if projectSetup.selectedIdx === 1}
	<div class="add-more-btn pt-3 text-right">
		<button
			class="  w-full bx--btn--secondary bx--btn--sm"
			on:click={() => {
				projectSetup.envKV = [...projectSetup.envKV, { key: '', value: '' }];
			}}
		>
			Add Key-Value Pair 🔥
		</button>
	</div>
	<div class="flex flex-col gap-2 py-3">
		{#each projectSetup.envKV as x, idx}
			<div class="flex flex-1 gap-2">
				<TextInput size="sm" bind:value={x.key} placeholder="KEY" />
				<TextInput size="sm" bind:value={x.value} placeholder="value" />
				<button
					class="bx--btn bx--btn--secondary bx--btn--sm"
					on:click={() => {
						projectSetup.envKV = projectSetup.envKV.filter((_, i) => i !== idx);
					}}
				>
					Remove
				</button>
			</div>
		{/each}
	</div>
{/if}
{#if projectSetup.errorMessage}
	<div class="text-red-500">{projectSetup.errorMessage}</div>
{/if}
<PreDebug {data} />
