<script lang="ts">
	import { goto, invalidateAll } from '$app/navigation';
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
		HeaderSearch
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

	export let isSideNavOpen: boolean;

	async function handleLogout() {
		const bres = await fetch('/logout', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			}
		}).then((res) => res.json());

		if (bres?.success) {
			toast.success('Logout Successful', { duration: 3000 });
		}

		invalidateAll();

		console.log('Backend Response', bres);
	}
</script>

<!-- <Header platformName="FreshCrafts" href="/" persistentHamburgerMenu bind:isSideNavOpen> -->
<Header platformName="FreshCrafts" href="/" bind:isSideNavOpen>
	<svelte:fragment slot="skip-to-content">
		<SkipToContent />
	</svelte:fragment>

	<HeaderUtilities>
		<HeaderSearchThingy />
		<HeaderThemeSwitcher />
		<HeaderGlobalAction
			iconDescription="Settings"
			tooltipAlignment="start"
			icon={SettingsAdjust}
			on:click={() => {
				goto('/settings');
			}}
		/>
		<HeaderGlobalAction
			iconDescription="Logout"
			tooltipAlignment="end"
			icon={Logout}
			on:click={handleLogout}
		/>
	</HeaderUtilities>
</Header>
<!-- {#if isSideNavOpen}
	<div class="custom-overlay custom-overlay--active" style="z-index: 6000;"></div>
{/if} -->
