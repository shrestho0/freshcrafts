<script lang="ts">
	import { HeaderSearch } from 'carbon-components-svelte';
	import type { Wind } from 'lucide-svelte';
	import type { KeyboardEventHandler } from 'svelte/elements';

	// Dummy Data
	const data = [
		{
			href: '/',
			text: 'Kubernetes Service',
			description:
				'Deploy secure, highly available apps in a native Kubernetes experience. IBM Cloud Kubernetes Service creates a cluster of compute hosts and deploys highly available containers.'
		},
		{
			href: '/',
			text: 'Red Hat OpenShift on IBM Cloud',
			description:
				'Deploy and secure enterprise workloads on native OpenShift with developer focused tools to run highly available apps. OpenShift clusters build on Kubernetes container orchestration that offers consistency and flexibility in operations.'
		},
		{
			href: '/',
			text: 'Container Registry',
			description:
				'Securely store container images and monitor their vulnerabilities in a private registry.'
		},
		{
			href: '/',
			text: 'Code Engine',
			description: 'Run your application, job, or container on a managed serverless platform.'
		}
	];
	let ref: HTMLInputElement;
	let active = false;
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

	function handleCtrlKSearchBarOpen(event: KeyboardEvent) {
		if (event.key === 'k' && event.altKey) {
			active = true;
			console.log(event);
		}
	}
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
		events = [...events, { type: 'select', ...e.detail }];
	};
</script>

<svelte:window on:keydown={handleCtrlKSearchBarOpen} />

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
