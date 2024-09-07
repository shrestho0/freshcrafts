<script lang="ts">
import { invalidateAll } from '$app/navigation';
import { page } from '$app/stores';
import NotificationSseFrontendLogicHandler from '@/components/NotificationSSEFrontendLogicHandler.svelte';
import CarbonArrowLeft from '@/ui/icons/CarbonArrowLeft.svelte';
import LucidePowerOff from '@/ui/icons/LucidePowerOff.svelte';
import { Grid, Row, Column, Button } from 'carbon-components-svelte';
import { onDestroy, onMount } from 'svelte';

// check if error_back_page cookie exits
let has_fallback_cookie = false;
let error_fallback = {
	href: '/',
	title: 'Back to Home'
} as { href: string; title: string };

let interval: NodeJS.Timeout;
onMount(() => {
	// console.log('error_fallback_cookie', document.cookie);
	let error_fallback_cookie = document.cookie
		?.split('; ')
		?.find((row) => row.startsWith('error_fallback='))
		?.split('=')[1];
	console.log('error_fallback_cookie', error_fallback_cookie);

	try {
		let { href, title } = JSON.parse(decodeURIComponent(error_fallback_cookie ?? '{}') ?? '{}') as {
			href: string;
			title: string;
		};
		console.log('error_fallback_cookie', href, title);
		href && (error_fallback.href = href);
		title && (error_fallback.title = title);
		has_fallback_cookie = true;
	} catch (e) {
		console.error('Error parsing error_back_page cookie', e);
	}

	if ($page.status === 503) {
		interval = setInterval(() => {
			// window.location.reload();
			invalidateAll();
		}, 3000);
	}
});

onDestroy(() => {
	if (has_fallback_cookie) {
		// remove error_back_page cookie
		// document.cookie
	}

	if (interval) {
		clearInterval(interval);
	}
});
</script>

<NotificationSseFrontendLogicHandler notificationStuff={undefined} />

<div class="w-full h-screen flex items-center justify-center">
	<Grid>
		<Row>
			<Column
				class="flex  flex-col items-center gap-3 {$page.status === 503 ? 'animate-pulse' : ''}"
			>
				<!-- <p class="text-center">{JSON.stringify($page)}</p> -->

				<!-- back to home button -->
				{#if $page.status === 503}
					<LucidePowerOff class="bg-red-600 rounded-full  p-2 h-12 w-12" />
					<!-- <h1 class="text-4xl font-bold text-center">{$page.status}</h1> -->
					<p class="text-center text-xl">{$page?.error?.message}</p>
				{:else}
					<h1 class="text-4xl font-bold text-center">{$page.status}</h1>
					<p class="text-center">{$page?.error?.message}</p>
					<a
						tabindex="0"
						href={error_fallback.href}
						class="bx--btn bx--btn--tertiary flex items-center justify-between w-full gap-3"
					>
						<CarbonArrowLeft />
						<span class=" ">{error_fallback.title}</span>
					</a>
				{/if}
			</Column>
		</Row>
	</Grid>
</div>
