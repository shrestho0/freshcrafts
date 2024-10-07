<script lang="ts">
	import { slide } from "svelte/transition";
	import {
		Header,
		HeaderUtilities,
		HeaderGlobalAction,
		SkipToContent,
	} from "carbon-components-svelte";
	import HeaderThemeSwitcher from "./HeaderThemeSwitcher.svelte";
	import HeaderSearchThingy from "./HeaderSearchThingy.svelte";

	import type { SystemwideNotification } from "@/types/entities";
	import { page } from "$app/stores";
	import { invalidateAll } from "$app/navigation";
	import ChatBox from "./ChatBox.svelte";
	import Circle from "@/ui/icons/Circle.svelte";
	import CarbonLogout from "@/ui/icons/CarbonLogout.svelte";
	import CarbonNotificationNew from "@/ui/icons/CarbonNotificationNew.svelte";
	import CarbonNotification from "@/ui/icons/CarbonNotification.svelte";
	import CarbonChat from "@/ui/icons/CarbonChat.svelte";
	import NotificationSseFrontendLogicHandler from "./NotificationSSEFrontendLogicHandler.svelte";

	export let isSideNavOpen: boolean;

	const pathname: string = $page.url.pathname;

	let notificationStuff = {
		hasNew: false,
		open: true,
		notifications: [],
	} as {
		hasNew: boolean;
		open: boolean;
		notifications: SystemwideNotification[];
	};

	// const notiSSEUrl = `/sse/notification`;
	// let value = source(notiSSEUrl).select('message');
	// if (browser && value) {
	// 	value.subscribe((data: string) => {
	// 		console.log(data);
	// 		if (data) {
	// 			const notification: SystemwideNotification = JSON.parse(data);
	// 			console.log('Notification', notification);
	// 			// if (!notification.markedAsRead) {
	// 			notificationStuff.notifications.push(notification);
	// 			notificationStuff.hasNew = true;

	// 			if (notification.actionHints?.startsWith('REDIRECT_PROJECTS_')) {
	// 				const [_, proj_id] = notification.actionHints.split('_');
	// 				if (proj_id) {
	// 					console.log('proj_id', proj_id);
	// 				}
	// 				if (pathname.startsWith('/projects')) {
	// 					console.log('pathname', pathname);
	// 				}
	// 			}
	// 		}
	// 	});
	// }

	async function handleLogout() {
		const bres = await fetch("/logout", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
		}).then((res) => res.json());

		// if (bres?.success) {
		// toast.success('Logout Successful', { duration: 3000 });
		// }

		invalidateAll();

		console.log("Backend Response", bres);
	}

	/**
	 * TODO: Filter notifications for notifications with same id.
	 * Thus we can send same notification in different state, latest one to be shown.
	 * But, then, sort against time sent/updated
	 * So, we need to keep track of datetime of updated, created will be found from _ulid
	 * TODO: There shouldn't be any markedAsRead=true message
	 */

	const gloaballyActiveables = {
		search: false,
		notification: false,
		chat: false,
	} as {
		search: boolean;
		notification: boolean;
		chat: boolean;
		selectedTheme: undefined | "g100" | "white";
	};
	function handleGlobalKeyEvents(event: KeyboardEvent) {
		if (event.altKey) {
			switch (event.key) {
				case "k":
					gloaballyActiveables.search = !gloaballyActiveables.search;
					break;
				case "n":
					gloaballyActiveables.notification =
						!gloaballyActiveables.notification;
					break;
				case "c":
					gloaballyActiveables.chat = !gloaballyActiveables.chat;
					break;
				case "l":
					handleLogout();
					break;
				case "m":
					gloaballyActiveables.selectedTheme =
						gloaballyActiveables.selectedTheme === "g100"
							? "white"
							: "g100";
				default:
					break;
			}
		}
		// if (event.key === 'k' && event.altKey) {
		// 	gloaballyActiveables.search = !gloaballyActiveables.search;
		// }
	}
</script>

<NotificationSseFrontendLogicHandler bind:notificationStuff />

<svelte:window on:keydown={handleGlobalKeyEvents} />

<!-- <Header platformName="FreshCrafts" href="/" persistentHamburgerMenu bind:isSideNavOpen> -->
<Header platformName="FreshCrafts (v0.1)" href="/" bind:isSideNavOpen>
	<svelte:fragment slot="skip-to-content">
		<SkipToContent />
	</svelte:fragment>

	<HeaderUtilities>
		<HeaderSearchThingy bind:active={gloaballyActiveables.search} />
		<HeaderThemeSwitcher
			selectedTheme={gloaballyActiveables.selectedTheme}
		/>

		<button
			type="button"
			aria-label="Search"
			aria-expanded="false"
			tabindex="0"
			class="bx--header-search-button bx--header__action"
			on:click={() => {
				gloaballyActiveables.notification =
					!gloaballyActiveables.notification;
				notificationStuff.hasNew = false;
			}}
		>
			{#if notificationStuff.hasNew}
				<!-- <NotificationNew class="w-5 h-5" /> -->
				<CarbonNotificationNew height="20" width="20" />
			{:else}
				<CarbonNotification height="20" width="20" />
			{/if}
		</button>

		<button
			class="bx--header-search-button bx--header__action"
			on:click={() => {
				gloaballyActiveables.chat = !gloaballyActiveables.chat;
			}}
		>
			<CarbonChat height="20" width="20" />
		</button>

		<HeaderGlobalAction
			iconDescription="Logout"
			tooltipAlignment="end"
			icon={CarbonLogout}
			on:click={handleLogout}
		/>
	</HeaderUtilities>
</Header>
{#if gloaballyActiveables.notification}
	<div
		transition:slide
		class="top-10 right-12 z-10 fixed bg-[#161616] p-3 m-2 w-[300px] text-gray-100"
	>
		{#if notificationStuff?.notifications?.length > 0}
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
					gloaballyActiveables.notification = false;
				}}
			>
				view all notifications
			</a>
		</div>
	</div>
{/if}

{#if gloaballyActiveables.chat}
	<ChatBox bind:open={gloaballyActiveables.chat} />
{/if}
