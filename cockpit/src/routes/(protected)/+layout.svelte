<script lang="ts">
	import AuthenticatedHeader from '@/components/AuthenticatedHeader.svelte';
	import AuthenticatedSidenav from '@/components/AuthenticatedSidenav.svelte';
	import { Content, Grid, Row, Column, Loading } from 'carbon-components-svelte';
	import { onMount } from 'svelte';
	export let data;

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

	<Content>
		<Grid>
			<Row>
				<Column>
					<slot />
				</Column>
			</Row>
		</Grid>
	</Content>
{/if}
