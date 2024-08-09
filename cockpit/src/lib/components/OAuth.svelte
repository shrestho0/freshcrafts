<!-- 

Refactor Required

-->

<script lang="ts">
import { LogoGithub, Unlink } from 'carbon-icons-svelte';
import { Button, InlineLoading, Loading } from 'carbon-components-svelte';
import { CircleCheckBig, CircleX, UnlinkIcon } from 'lucide-svelte';
import type { SetupPageOauthData } from '@/types/internal';
import { AuthProviderType } from '@/types/enums';
import GoogleIcon from '@/ui/css/icons/GoogleIcon.svelte';
export let data: SetupPageOauthData;

export let connectionCallback: ({
	provider,
	githubId,
	googleEmail
}: {
	provider: AuthProviderType;
	githubId?: string;
	googleEmail?: string;
}) => void = () => {
	console.log('connectionCallback not set');
};
export let disconnectionCallback: ({ provider }: { provider: AuthProviderType }) => void = ({
	provider
}: {
	provider: AuthProviderType;
}) => {
	console.log('default disconnectionCallback called', provider);

	if (provider === AuthProviderType.OAUTH_GITHUB) {
		data.githubStatus = 'idle';
		data.githubOAuthJson = null;
		data.oAuthGithubId = '';
	} else if (provider === AuthProviderType.OAUTH_GOOGLE) {
		data.googleStatus = 'idle';
		data.googleOAuthJson = null;
		data.oAuthGoogleEmail = '';
	}
};

// let githubUrl = initializeAndReturnGithubUrl();
// let googleUrl = initializeAndReturnGoogleUrl();

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
			window.clearInterval(pollTimer);
			// check if oc_data is set in localStorage
			const oauthData = localStorage.getItem('oc_data');
			// localStorage.removeItem('oc_data');
			if (oauthData) {
				console.log('OAuth data:', oauthData);
				try {
					const parsedData = JSON.parse(oauthData);
					if (parsedData.success) {
						if (oauthType == 'google') {
							data.googleStatus = 'connected';
							data.googleOAuthJson = parsedData.data;
							data.oAuthGoogleEmail = parsedData.data.oAuthGoogleEmail;
							connectionCallback({
								provider: AuthProviderType.OAUTH_GOOGLE,
								googleEmail: parsedData.data.oAuthGoogleEmail
							});
						} else if (oauthType == 'github') {
							data.githubStatus = 'connected';
							data.githubOAuthJson = parsedData.data;
							data.oAuthGithubId = parsedData.data.oAuthGithubId;
							connectionCallback({
								provider: AuthProviderType.OAUTH_GITHUB,
								githubId: parsedData.data.oAuthGithubId
							});
						}
						currentOk = true;
					} else {
						// if (oauthType == 'google') data.googleStatus == 'error';
						// else if (oauthType == 'github') data.githubStatus = 'error';
					}
				} catch (e) {
					// if (oauthType == 'google') data.googleStatus == 'error';
					// else if (oauthType == 'github') data.githubStatus = 'error';
				}
			} else {
				// if (oauthType == 'google') data.googleStatus == 'error';
				// if (oauthType == 'google') data.googleStatus == 'error';
			}
			console.log('Popup closed');
		}
	}, 500);

	if (!currentOk) {
		if (oauthType == 'google') data.googleStatus == 'error';
		if (oauthType == 'github') data.githubStatus == 'error';
	}
}

function handleOauthLogin(oauthType: 'github' | 'google') {
	console.log('Github login');

	let popup: Window | null;

	if (oauthType == 'github') {
		// open as oauth popup
		data.githubStatus = 'loading';
		popup = openOAuthPopup(data.githubLoginUrl);
		monitorPopup(popup, oauthType);
	} else if (oauthType == 'google') {
		data.googleStatus = 'loading';
		popup = openOAuthPopup(data.googleLoginUrl);
		monitorPopup(popup, oauthType);
	}
}
</script>

<div class="oauth-container oauth-container">
	<div class=" w-full max-w-full gap-3 flex flex-col">
		<div class="flex w-full">
			<Button
				kind="secondary"
				on:click={() => handleOauthLogin('github')}
				disabled={data.githubStatus === 'connected'}
			>
				<div class="inline-flex items-center pr-3">
					<LogoGithub class=" col-span-1 min-w-6 min-h-6" />
				</div>
				<p class="flex items-center justify-center gap-2">
					{#if data.githubStatus === 'loading'}
						Connecting with Github
						<InlineLoading class=" " />
					{:else if data.githubStatus === 'connected'}
						Connected with Github
						<CircleCheckBig class="text-green-600 h-5" />
					{:else if data.githubStatus === 'error'}
						Failed to connect with Github
						<CircleX class="text-red-500 h-5" />
					{:else if data.githubStatus === 'idle'}
						Connect with Github
					{/if}
				</p>
			</Button>
			{#if data.githubStatus === 'connected'}
				<Button
					kind="secondary"
					iconDescription="Disconnect"
					tooltipPosition="right"
					icon={Unlink}
					on:click={() => disconnectionCallback({ provider: AuthProviderType.OAUTH_GITHUB })}
				/>
			{/if}
		</div>
		<div class="flex w-full">
			<Button
				kind="secondary"
				on:click={() => handleOauthLogin('google')}
				disabled={data.googleStatus === 'connected'}
			>
				<div class="inline-flex items-center pr-3">
					<!-- <LogoGithub class=" col-span-1 min-w-6 min-h-6" /> -->
					<GoogleIcon />
				</div>
				<p class="flex items-center justify-center gap-2">
					{#if data.googleStatus === 'loading'}
						Connecting with Google
						<InlineLoading class=" " />
					{:else if data.googleStatus === 'connected'}
						Connected with Google
						<CircleCheckBig class="text-green-600 h-5" />
					{:else if data.googleStatus === 'error'}
						Failed to connect with Google
						<CircleX class="text-red-500 h-5" />
					{:else if data.googleStatus === 'idle'}
						Connect with Google
					{/if}
				</p>
			</Button>
			{#if data.googleStatus === 'connected'}
				<Button
					kind="secondary"
					iconDescription="Disconnect"
					tooltipPosition="right"
					icon={Unlink}
					on:click={() => disconnectionCallback({ provider: AuthProviderType.OAUTH_GOOGLE })}
				/>
			{/if}
		</div>
		<!-- <button
			on:click={() => handleOauthLogin('google')}
			class="w-full flex justify-between items-center max-w-full bx--btn--secondary dark:bx--btn--tertiary text-left px-4 min-h-[3rem] text-md"
			disabled={data.googleStatus === 'connected'}
		>
			<p class="flex items-center justify-center gap-2">
				{#if data.googleStatus === 'loading'}
					Connecting with Google
					<InlineLoading class=" " />
				{:else if data.googleStatus === 'connected'}
					Connected with Github
					<CircleCheckBig class="text-green-600 h-5" />
				{:else if data.googleStatus === 'error'}
					Failed to connect with Github
					<CircleX class="text-red-500 h-5" />
				{:else if data.googleStatus === 'idle'}
					Connect with Google
				{/if}
			</p>
			<svg
				xmlns="http://www.w3.org/2000/svg"
				x="0px"
				y="0px"
				class="w-6 h-6 fill-current"
				viewBox="0 0 30 30"
			>
				<path
					d="M 15.003906 3 C 8.3749062 3 3 8.373 3 15 C 3 21.627 8.3749062 27 15.003906 27 C 25.013906 27 27.269078 17.707 26.330078 13 L 25 13 L 22.732422 13 L 15 13 L 15 17 L 22.738281 17 C 21.848702 20.448251 18.725955 23 15 23 C 10.582 23 7 19.418 7 15 C 7 10.582 10.582 7 15 7 C 17.009 7 18.839141 7.74575 20.244141 8.96875 L 23.085938 6.1289062 C 20.951937 4.1849063 18.116906 3 15.003906 3 z"
				></path>
			</svg>
		</button> -->
	</div>
</div>
