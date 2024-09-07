<script lang="ts">
import { enhance } from '$app/forms';
import { invalidateAll } from '$app/navigation';
import { page } from '$app/stores';
import PreDebug from '@/components/dev/PreDebug.svelte';
import type { EngineSystemConfigResponseDto } from '@/types/dtos';
import X from '@/ui/icons/X.svelte';
import type { ActionResult } from '@sveltejs/kit';
import { Button, InlineLoading } from 'carbon-components-svelte';
import { onMount } from 'svelte';

export let removeConnFormEndpoint = '';
export let icon: any;
export let connEnabled: boolean;
export let connTitle: string;

export let url: string;

export let btnClass = 'flex gap-3 bx--btn text-white bg-[var(--cds-interactive-02)]';

function openOAuthPopup(oauthUrl: string) {
	connStatus = 'loading';
	const popup = window.open(oauthUrl, `${connTitle} OAuth`, 'width=800,height=600, popup=1');
	return popup;
}

function monitorPopup(popup: Window | null) {
	if (!popup) {
		console.error('no popup');
		return;
	}

	let currentOk = false;
	popup.addEventListener('close', (e) => {
		console.log('close event');
	});
	popup.addEventListener('message', (e) => {
		console.log('message from external window', e);
	});

	const pollTimer = window.setInterval(() => {
		if (popup.closed) {
			console.log('popup closed');
			invalidateAll();
			(() => {
				window.clearInterval(pollTimer);
			})();
			// window.location.href = '/_/setup?step=oauth';

			// console.log('invalidateAll called');
		}
	}, 500);
}

function handleOauthLogin() {
	console.log('{ConnTitle} login');

	let popup: Window | null;
	popup = openOAuthPopup(url);
	monitorPopup(popup);
}

let connStatus: 'connected' | 'await_connect' | 'loading' = 'await_connect';
onMount(() => {
	if (connEnabled) {
		console.log(`${connTitle} OAuth is enabled`);
		connStatus = 'connected';
	} else {
		console.log(`${connTitle}} OAuth is not enabled`);
	}
});

function enhancedConnRemoval() {
	console.log('formxx');
	connStatus = 'loading';
	return async ({ result }: { result: ActionResult }) => {
		console.log('result', result);
		if (result.type == 'success') {
			connStatus = 'await_connect';
		}
	};
}
</script>

<!-- 
<PreDebug data={{ sysconf }} /> -->

<!-- {# if connStatus === 'loading' || connStatus === 'connected'} -->
<section class="flex gap-0 items-center w-full">
	{#if connStatus === 'connected'}
		<div class={btnClass}>
			<svelte:component this={icon} />
			<div>Connected with {connTitle}</div>
		</div>
		<form method="post" action={removeConnFormEndpoint} use:enhance={enhancedConnRemoval}>
			<Button kind="danger" iconDescription={'Disconnect ' + connTitle} icon={X} type="submit" />
		</form>
	{:else if connStatus === 'loading'}
		<div class={btnClass}>
			<InlineLoading />
			<div>Communicating with Backend and {connTitle}...</div>
		</div>
	{:else if connStatus === 'await_connect'}
		<button class={btnClass} on:click={() => handleOauthLogin()}>
			<svelte:component this={icon} />
			<div>Connect with {connTitle}</div>
			<X />
		</button>
	{:else}
		<div>Unknown status</div>
	{/if}
</section>
