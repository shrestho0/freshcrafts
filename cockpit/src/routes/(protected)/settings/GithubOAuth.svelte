<script lang="ts">
import { invalidateAll } from '$app/navigation';
import { page } from '$app/stores';
import { PUBLIC_GITHUB_OAUTH_CALLBACK_URL } from '$env/static/public';
import PreDebug from '@/components/dev/PreDebug.svelte';
import type { EngineSystemConfigResponseDto } from '@/types/dtos';
import { Button } from 'carbon-components-svelte';
import { LogoGithub } from 'carbon-icons-svelte';
import { onMount } from 'svelte';

export let sysconf: EngineSystemConfigResponseDto;

const redirect_url = $page.url.origin + PUBLIC_GITHUB_OAUTH_CALLBACK_URL;
console.log('redirect_url', redirect_url);

const gh_urls = {
	install: 'https://github.com/apps/freshcrafts/installations/new' + '?redirect_url=' + redirect_url
	// install: $page.data.githubLoginUrl
	// authorize: ''
};

function openOAuthPopup(oauthUrl: string) {
	const popup = window.open(oauthUrl, 'Github OAuth', 'width=800,height=600, popup=1');
	return popup;
}

function monitorPopup(popup: Window | null, oauthType: 'github' | 'google') {
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
			window.clearInterval(pollTimer);
			invalidateAll();
		}
	}, 500);
}

function handleOauthLogin(oauthType: 'github' | 'google') {
	console.log('Github login');

	let popup: Window | null;
	popup = openOAuthPopup(gh_urls.install);
	monitorPopup(popup, oauthType);
}

let githubStatus: 'connected' | 'idle' | 'disconnected' | 'loading' = 'idle';
onMount(() => {
	if (sysconf.systemUserOauthGithubEnabled) {
		console.log('Github OAuth is enabled');
		githubStatus = 'connected';
	}
});
</script>

<PreDebug data={{ sysconf }} />

<Button class="flex gap-3" kind="secondary" on:click={() => handleOauthLogin('github')}>
	<LogoGithub />
	{#if githubStatus === 'connected'}
		<div>Connected with Github</div>
	{:else if githubStatus === 'loading'}
		<div>Connecting with Github...</div>
	{:else if githubStatus === 'disconnected'}
		<div>Github Disconnected. Uninstall app from github</div>
	{:else}
		<div>Connect with Github</div>
	{/if}
</Button>
