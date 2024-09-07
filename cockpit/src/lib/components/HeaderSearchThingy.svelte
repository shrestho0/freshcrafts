<script lang="ts">
import { HeaderSearch } from 'carbon-components-svelte';
export let active: boolean;
// Dummy Data
const data = [
	{
		href: '/',
		text: 'Dashboard',
		description: 'Dashboard'
	},
	{
		href: '/projects/all',
		text: 'All Projects',
		description: 'Manage your projects'
	},
	{
		href: '/projects/new',
		text: 'Create New Project',
		description: 'Create a new project from your github repo or local files'
	},
	{
		href: '/databases/mysql',
		text: 'Manage MySQL Databases',
		description: 'Manage your MySQL databases'
	},
	{
		href: '/databases/mysql/new',
		text: 'Create MySQL Database',
		description: 'Create a new MySQL database'
	},

	{
		href: '/databases/postgres',
		text: 'Manage Postgres Databases',
		description: 'Manage your postgres databases'
	},
	{
		href: '/databases/postgres/new',
		text: 'Create Postgres Database',
		description: 'Create a new Postgres database'
	},
	{
		href: '/databases/mongodb',
		text: 'Manage Mongo Databases',
		description: 'Manage your mongo databases'
	},
	{
		href: '/databases/mongodb/new',
		text: 'Create Mongo Database',
		description: 'Create a new Mongo database'
	},
	{
		href: '/chat/history',
		text: 'Chat History',
		description: 'View chat history'
	}
];
let ref: HTMLInputElement;
let value = '';
let selectedResultIndex = 0;
let events: any[] = [];
$: lowerCaseValue = value.toLowerCase();
$: results =
	value.length > 0
		? data.filter((item) => {
				return (
					item.text.toLowerCase().includes(lowerCaseValue) ||
					item.description.includes(lowerCaseValue)
				);
			})
		: [];

// TODO: the whole thing
// $: console.log('ref', ref);
// $: console.log('active', active);
// $: console.log('value', value);
// $: console.log('selectedResultIndex', selectedResultIndex);

const onActive = () => {
	events = [...events, { type: 'active' }];
};
const onInactive = () => {
	events = [...events, { type: 'inactive' }];
};
const onClear = () => {
	events = [...events, { type: 'clear' }];
};
const onSelect = (e: { detail: any }) => {
	events = [...events, { type: 'select', detail: e.detail }];
	console.log('onSelect', e.detail);
	window.location.href = e?.detail?.selectedResult?.href ?? '';
};
</script>

<HeaderSearch
	bind:ref
	bind:active
	bind:value
	bind:selectedResultIndex
	placeholder="Search services. Open this by pressing (Alt+K)"
	{results}
	on:active={onActive}
	on:inactive={onInactive}
	on:clear={onClear}
	on:select={onSelect}
/>
