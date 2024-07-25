<script lang="ts">
	import { goto, invalidateAll } from '$app/navigation';
	import { slide } from 'svelte/transition';
	import { page } from '$app/stores';
	import {
		Header,
		HeaderUtilities,
		HeaderGlobalAction,
		SideNav,
		SideNavItems,
		SideNavMenu,
		SideNavMenuItem,
		SideNavLink,
		SkipToContent,
		Content,
		Grid,
		Row,
		Column,
		Button,
		HeaderSearch,
		ToastNotification,
		NotificationActionButton,
		Dropdown,
		DropdownSkeleton
	} from 'carbon-components-svelte';
	import Logout from 'carbon-icons-svelte/lib/Logout.svelte';
	import SettingsAdjust from 'carbon-icons-svelte/lib/SettingsAdjust.svelte';
	import UserAvatarFilledAlt from 'carbon-icons-svelte/lib/UserAvatarFilledAlt.svelte';
	import { onMount } from 'svelte';
	import { toast } from 'svelte-sonner';
	import HeaderThemeSwitcher from './HeaderThemeSwitcher.svelte';
	import HeaderSearchThingy from './HeaderSearchThingy.svelte';
	import { HtmlReference } from 'carbon-icons-svelte';
	import ClientScreenSize from './dev/ClientScreenSize.svelte';
	import { Notification as NotificationIcon, NotificationNew } from 'carbon-icons-svelte';
	import { source } from 'sveltekit-sse';
	import { browser } from '$app/environment';
	import { Circle } from 'lucide-svelte';
	import type { SystemwideNotification } from '@/types/entities';

	export let isSideNavOpen: boolean;

	const notificationStuff = {
		hasNew: false,
		open: false,
		notifications: []
	} as { hasNew: boolean; open: boolean; notifications: SystemwideNotification[] };

	const notiSSEUrl = `/sse/notification`;
	let value = source(notiSSEUrl).select('message');
	if (browser && value) {
		value.subscribe((data) => {
			console.log(data);
			if (data) {
				notificationStuff.notifications.push(JSON.parse(data));
				notificationStuff.hasNew = true;
			}
		});
	}

	async function handleLogout() {
		const bres = await fetch('/logout', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			}
		}).then((res) => res.json());

		// if (bres?.success) {
		// toast.success('Logout Successful', { duration: 3000 });
		// }

		invalidateAll();

		console.log('Backend Response', bres);
	}

	/**
	 * TODO: Filter notifications for notifications with same id.
	 * Thus we can send same notification in different state, latest one to be shown.
	 * But, then, sort against time sent/updated
	 * So, we need to keep track of datetime of updated, created will be found from ulid
	 * TODO: There shouldn't be any markedAsRead=true message
	 */
</script>

<!-- <Header platformName="FreshCrafts" href="/" persistentHamburgerMenu bind:isSideNavOpen> -->
<Header platformName="FreshCrafts" href="/" bind:isSideNavOpen>
	<svelte:fragment slot="skip-to-content">
		<SkipToContent />
	</svelte:fragment>

	<HeaderUtilities>
		<HeaderSearchThingy />
		<HeaderThemeSwitcher />
		<!-- <HeaderGlobalAction
			icon={notificationStuff.hasNew ? NotificationNew : NotificationIcon}
			on:click={() => {
				notificationStuff.open = !notificationStuff.open;
			}}
		/> -->
		<button
			type="button"
			aria-label="Search"
			aria-expanded="false"
			tabindex="0"
			class="bx--header-search-button bx--header__action"
			on:click={() => {
				notificationStuff.open = !notificationStuff.open;
				notificationStuff.hasNew = false;
			}}
		>
			{#if notificationStuff.hasNew}
				<!-- <NotificationNew class="w-5 h-5" /> -->
				<NotificationNew height="20" width="20" />
			{:else}
				<NotificationIcon height="20" width="20" />
			{/if}
		</button>

		<HeaderGlobalAction
			iconDescription="Logout"
			tooltipAlignment="end"
			icon={Logout}
			on:click={handleLogout}
		/>
	</HeaderUtilities>
</Header>
{#if notificationStuff.open}
	<div
		transition:slide
		class="top-10 right-12 z-10 fixed bg-[#161616] p-3 m-2 w-[300px] text-gray-100"
	>
		{#if notificationStuff.notifications.length > 0}
			{#each notificationStuff.notifications as noti}
				<div class="flex items-center gap-2">
					<Circle fill="white" class="h-3 w-3" />
					{noti.markedAsRead}
					{noti.message}
				</div>
			{/each}
		{:else}
			No new notification
		{/if}

		<div class="py-2">
			<a
				href="/notifications"
				on:click={() => {
					notificationStuff.open = false;
				}}
			>
				view all notifications
			</a>
		</div>
	</div>
{/if}
