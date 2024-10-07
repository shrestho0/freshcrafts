<script lang="ts">
	import { SelectableTile, TreeView } from "carbon-components-svelte";
	import { onMount } from "svelte";

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
	export let selectedFileRelativeUrl = "";
	let expandedIds: any[] = [];

	$: {
		if (activeId) {
			selectedFileRelativeUrl = findRelativePath(activeId, items);
		}
	}

	onMount(() => {
		if (selectedFileRelativeUrl) {
			// console.log(selectedFileRelativeUrl, findItemidFromPath(selectedFileRelativeUrl, items));
			activeId = findItemidFromPath(selectedFileRelativeUrl, items);
			if (activeId) {
				// find parent id to expand
				let parentId = findPredecessorIds(activeId, items);
				if (parentId) {
					expandedIds.push(parentId);
				}
			}
			console.log("activeId", activeId);
		}
	});

	function findPredecessorIds(id: any, items: any[]): any {
		for (const item of items) {
			if (item.children) {
				if (
					item.children.some((child: { id: any }) => child.id === id)
				) {
					expandedIds.push(item.id);
					return item.id;
				}
				const found = findPredecessorIds(id, item.children);
				if (found) return found;
			}
		}
	}

	function findRelativePath(id: any, items: any[]): any {
		for (const item of items) {
			if (item.id === id) return item.text;
			if (item.children) {
				const found = findRelativePath(id, item.children);
				if (found) return item.text + "/" + found;
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
			if (item.relativePath === path) {
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
	class=" bg-gray-300 dark:bg-[var(--cds-interactive-02)] flex items-center outline-none focus:outline-none focus-visible:outline-none"
	selected={selectedFileRelativeUrl === "."}
	on:select={() => {
		activeId = null;
		selectedFileRelativeUrl = ".";
	}}
	on:deselect={() => {
		activeId = null;
		selectedFileRelativeUrl = "";
	}}
>
	{#if selectedFileRelativeUrl === "."}
		Root directory selected
	{:else}
		Select root directory
	{/if}
</SelectableTile>

<TreeView children={items} bind:activeId bind:expandedIds />
<!-- <PreDebug data={items} /> -->
