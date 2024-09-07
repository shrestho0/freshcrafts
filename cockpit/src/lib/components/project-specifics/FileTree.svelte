<script lang="ts">
import { SelectableTile, TreeView } from 'carbon-components-svelte';
import { onMount } from 'svelte';

// export let items: {
// 	name: string;
// 	children?: (typeof items)[];
// 	isDir: boolean;
// 	isFile: boolean;
// 	level: number;
// 	id: number;
// }[];
export let items: any[];
let activeId: any;
export let selectedFileRelativeUrl = '';
$: {
	if (activeId) {
		selectedFileRelativeUrl = findRelativePath(activeId, items);
	}
}

onMount(() => {
	if (selectedFileRelativeUrl) {
		console.log(selectedFileRelativeUrl, findItemidFromPath(selectedFileRelativeUrl, items));
		activeId = findItemidFromPath(selectedFileRelativeUrl, items);
	}
});

function findRelativePath(id: any, items: any[]): any {
	for (const item of items) {
		if (item.id === id) return item.text;
		if (item.children) {
			const found = findRelativePath(id, item.children);
			if (found) return item.text + '/' + found;
		}
	}
}

function findItemidFromPath(path: string, items: any[]): any {
	//dfs
	// for (const item of items) {
	// 	if (item.text === path) return item.id;
	// 	if (item.children) {
	// 		const found = findItemidFromPath(path, item.children);
	// 		if (found) return found;
	// 	}
	// }

	// bfs is better for this case
	const queue = [...items];
	while (queue.length > 0) {
		const item = queue.shift();
		if (item.text === path) {
			return item.id;
		}
		if (item.children) {
			queue.push(...item.children);
		}
	}
	return null;
}
</script>

<SelectableTile
	class=" bg-gray-300 hover:bg-gray-300 flex items-center "
	selected={selectedFileRelativeUrl === '.'}
	on:select={() => {
		activeId = null;
		selectedFileRelativeUrl = '.';
	}}
	on:deselect={() => {
		activeId = null;
		selectedFileRelativeUrl = '';
	}}
>
	{#if selectedFileRelativeUrl === '.'}
		Root directory selected
	{:else}
		Select root directory
	{/if}
</SelectableTile>

<TreeView children={items} bind:activeId />
