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
	class="dark:bg-[var(--cds-ui-background)] "
	expansionBreakpoint={1500}
>
	<SideNavItems>
		{#each navBarLink as item}
			{#if item.children}
				<!-- style={item.serviceType === serviceType ? 'background-color: var(--cds-ui-03)' : ''} -->
				<SideNavMenu
					text={item.title}
					expanded={true}
					class="dark:text-[#f4f4f4]/80"
				>
					<!-- expanded={serviceType === item.serviceType} -->
					<!-- text={child.title}
							class=" {child.href == relativeUrl
								? ' bg-stone-200 bg-[var(--cds-ui-03] dark:bg-[#f4f4f4]/20 dark:text-[#f4f4f4]/80 '
								: ''} " -->
					{#each item.children as child}
						<li class="bx--side-nav__menu-item">
							<a
								href={child.href}
								class="bx--side-nav__submenu flex h-[2rem] min-h-[2rem] pl-[2rem] {child.href ==
								relativeUrl
									? ' bg-stone-200 bg-[var(--cds-ui-03] dark:bg-[#f4f4f4]/20 dark:text-[#f4f4f4]/80 '
									: '  dark:text-[#f4f4f4]/80 dark:hover:bg-[#f4f4f4]/30 '}"
								style="font-weight: normal;"
							>
								{#if child.icon}
									<svelte:component
										this={child.icon}
										classes="w-5 h-5 mr-2 "
									/>
								{/if}
								{child.title}
							</a>
						</li>
					{/each}
				</SideNavMenu>
			{:else if item.href}
				<li class="bx--side-nav__menu-item">
					<a
						href={item.href}
						class="bx--side-nav__link {item.href == relativeUrl
							? ' bg-stone-200 bg-[var(--cds-ui-03] dark:bg-[#f4f4f4]/20 dark:text-[#f4f4f4]/80 '
							: ' dark:text-[#f4f4f4]/80 dark:hover:bg-[#f4f4f4]/30 '}"
						>{item.title}</a
					>
				</li>
			{/if}
		{/each}
	</SideNavItems>
</SideNav>
