<script lang="ts">
	import { page } from '$app/stores';
	import {
		SideNav,
		SideNavItems,
		SideNavMenu,
		SideNavMenuItem,
		SideNavLink
	} from 'carbon-components-svelte';
	import { onMount } from 'svelte';

	export let isSideNavOpen: boolean;

	type MenuItemType = {
		title: string;
		href: string;
	};
	type MenuWithSubmenuType = MenuItemType & {
		serviceType: string;
		children?: MenuItemType[];
	};

	const navBarLink: MenuWithSubmenuType[] = [
		{
			serviceType: 'dashboard',
			title: 'Dashboard',
			href: '/dashboard',
			children: undefined
		},
		{
			serviceType: 'deployments',
			title: 'Deployments',
			href: '/deployments',
			children: [
				{ title: 'All Deployments', href: '/deployments/all' },
				{ title: 'New Deployment', href: '/deployments/new' }

				// TODO: these are children of each deployment
				// { title: 'Metrics', href: '/deployments/metrics' }, // data from watchdog
				// { title: 'Logs', href: '/deployments/logs' },
				// { title: 'Settings', href: '/deployments/env' }
			]
		},
		{
			serviceType: 'databases',
			title: 'Databases',
			href: '/databases',
			children: [
				{
					title: 'MySQL',
					href: '/databases/mysql'
				},
				{
					title: 'PostgreSQL',
					href: '/databases/postgresql'
				},
				{
					title: 'MongoDB',
					href: '/databases/mongodb'
				}
			]
		},
		{
			serviceType: 'settings',
			title: 'Settings',
			href: '/settings',
			children: undefined
		}
	];

	// We have make it reactive
	$: relativeUrl = '/' + $page.url.pathname.split('/').slice(1).join('/');
	$: serviceType = relativeUrl.split('/')[1] ?? '';
	onMount(() => {
		console.log('relativeUrl', relativeUrl);
		console.log('serviceType', serviceType);
	});
</script>

<SideNav
	bind:isOpen={isSideNavOpen}
	class="dark:bg-[var(--cds-ui-background)]  "
	expansionBreakpoint={1500}
>
	<SideNavItems>
		{#each navBarLink as item}
			{#if item.children}
				<!-- style={item.serviceType === serviceType ? 'background-color: var(--cds-ui-03)' : ''} -->
				<SideNavMenu text={item.title} expanded={true}>
					<!-- expanded={serviceType === item.serviceType} -->
					{#each item.children as child}
						<SideNavMenuItem
							class="dark:text-[var(--cds-ui-text-01)] dark:hover:text-[var(--cds-ui-text-02)] dark:hover:bg-[var(--cds-ui-background)] dark:bg-[var(--cds-ui-background)] dark:hover:bg-opacity-10 dark:bg-opacity-10"
							text={child.title}
							isSelected={child.href == relativeUrl}
							href={child.href}
						/>
					{/each}
				</SideNavMenu>
			{:else if item.href}
				<SideNavLink isSelected={item.href == relativeUrl} href={item.href} text={item.title} />
			{/if}
		{/each}
	</SideNavItems>
</SideNav>
