<script lang="ts">
import { SelectableTile, TreeView } from 'carbon-components-svelte';

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

// function findById(id: any, items: any[]): any {
// 	for (const item of items) {
// 		if (item.id === id) return item;
// 		if (item.children) {
// 			const found = findById(id, item.children);
// 			if (found) return found;
// 		}
// 	}
// }

function findRelativePath(id: any, items: any[]): any {
	for (const item of items) {
		if (item.id === id) return item.text;
		if (item.children) {
			const found = findRelativePath(id, item.children);
			if (found) return item.text + '/' + found;
		}
	}
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
