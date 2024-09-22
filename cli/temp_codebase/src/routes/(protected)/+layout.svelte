<script lang="ts">
	import { page } from "$app/stores";
	import AuthenticatedHeader from "@/components/AuthenticatedHeader.svelte";
	import AuthenticatedSidenav from "@/components/AuthenticatedSidenav.svelte";
	import { Grid, Row, Column, Loading } from "carbon-components-svelte";
	import { onMount } from "svelte";
	import { fade, fly } from "svelte/transition";

	let loading = true;
	let isSideNavOpen = false;
	onMount(() => {
		loading = false;
	});
</script>

<!-- <HeaderAuthenticated bind:isSideNavOpen /> -->

{#if loading}
	<Loading class="bg-[var(--cds-ui-background)]" />
{:else}
	<AuthenticatedHeader bind:isSideNavOpen />
	<AuthenticatedSidenav bind:isSideNavOpen />

	<!-- {#key $page.url}
	{/key} -->
	<!-- <Content> -->
	{#key $page.url}
		<main id="main-content" class="bx--content" style="" in:fade>
			<Grid>
				<Row>
					<Column>
						<slot />
					</Column>
				</Row>
			</Grid>
		</main>
	{/key}
	<!-- </Content> -->
{/if}
