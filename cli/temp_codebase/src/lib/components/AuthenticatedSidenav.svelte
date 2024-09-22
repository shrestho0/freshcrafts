<script lang="ts">
	import { page } from "$app/stores";
	import MongoDbIcon from "@/ui/icons/MongoDBIcon.svelte";
	import MySqlIcon from "@/ui/icons/MySQLIcon.svelte";
	import PostgresIcon from "@/ui/icons/PostgresIcon.svelte";
	import RedisIcon from "@/ui/icons/RedisIcon.svelte";
	import {
		SideNav,
		SideNavItems,
		SideNavMenu,
		SideNavMenuItem,
		SideNavLink,
	} from "carbon-components-svelte";
	import { onMount } from "svelte";

	export let isSideNavOpen: boolean;

	type MenuItemType = {
		title: string;
		href: string;
		icon?: any;
	};
	type MenuWithSubmenuType = MenuItemType & {
		serviceType: string;
		children?: MenuItemType[];
	};

	const navBarLink: MenuWithSubmenuType[] = [
		{
			serviceType: "dashboard",
			title: "Dashboard",
			href: "/dashboard",
			children: undefined,
		},
		{
			serviceType: "projects",
			title: "Projects",
			href: "/projects",
			children: [
				{ title: "All Projects", href: "/projects/all" },
				{ title: "New Project", href: "/projects/new" },

				// TODO: these are children of each deployment
				// { title: 'Metrics', href: '/deployments/metrics' }, // data from watchdog
				// { title: 'Logs', href: '/deployments/logs' },
				// { title: 'Settings', href: '/deployments/env' }
			],
		},
		{
			serviceType: "databases",
			title: "Databases",
			href: "/databases",
			children: [
				{
					title: "MySQL",
					href: "/databases/mysql",
					icon: MySqlIcon,
				},
				{
					title: "PostgreSQL",
					href: "/databases/postgres",
					icon: PostgresIcon,
				},
				{
					title: "MongoDB",
					href: "/databases/mongodb",
					icon: MongoDbIcon,
				},
				{
					title: "Redis",
					href: "/databases/redis",
					icon: RedisIcon,
				},
			],
		},
		{
			serviceType: "settings",
			title: "Settings",
			href: "/settings",
			children: undefined,
		},
		{
			serviceType: "chat-history",
			title: "Chat History",
			href: "/chat/history",
			children: undefined,
		},
		{
			serviceType: "notifications",
			title: "Notifications",
			href: "/notifications",
			children: undefined,
		},
		{
			serviceType: "service-status",
			title: "Service Status",
			href: "/service-status",
			children: undefined,
		},
	];

	// We have make it reactive
	$: relativeUrl = "/" + $page.url.pathname.split("/").slice(1).join("/");
	$: serviceType = relativeUrl.split("/")[1] ?? "";
	onMount(() => {
		console.log("relativeUrl", relativeUrl);
		console.log("serviceType", serviceType);
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
						>
							<div class="flex">
								{#if child.icon}
									<svelte:component
										this={child.icon}
										classes="w-6 h-6 mr-2 "
									/>
								{/if}
								{child.title}
							</div>
						</SideNavMenuItem>
					{/each}
				</SideNavMenu>
			{:else if item.href}
				<SideNavLink
					isSelected={item.href == relativeUrl}
					href={item.href}
					text={item.title}
				/>
			{/if}
		{/each}
	</SideNavItems>
</SideNav>
