<script lang="ts">
import { Button, InlineNotification, Loading } from 'carbon-components-svelte';
import Account from './Account.svelte';
import Review from './Review.svelte';
import { onDestroy, onMount } from 'svelte';
import { browser } from '$app/environment';
import type { SetupPageAccountData, SetupPageOauthData } from '@/types/internal';
import OAuth from '@/components/OAuth.svelte';

export let data;
const { githubLoginUrl, googleLoginUrl } = data;

const steps = [
	{
		title: 'Welcome',
		description:
			'Welcome to the setup wizard. This wizard will guide you through the setup process.',
		component: null
	},
	{
		title: 'Account',
		description: 'Setup up your email password for the system.',
		component: Account,
		data: {
			email: '',
			password: '',
			name: ''
		} as SetupPageAccountData
	},
	{
		title: 'OAuth',
		description: 'Setup OAuth accounts for signing in with Google, Github',
		component: OAuth,
		data: {
			githubLoginUrl: githubLoginUrl,
			googleLoginUrl: googleLoginUrl,
			githubStatus: 'idle',
			googleStatus: 'idle',
			githubOAuthJson: null,
			googleOAuthJson: null,
			oAuthGoogleEmail: '',
			oAuthGithubId: ''
		} as SetupPageOauthData
	},
	{
		title: 'Confirm Setup',
		description: 'Review your setup and confirm your settings',
		component: Review
	},
	{
		title: 'Setup Complete!',
		description: 'Setup is complete. You can now login to your account.',
		component: null
	}
];
let currentStep = 0;

let sideLoadingIcon = false;

let error_message = '';
async function confirmSetup() {
	sideLoadingIcon = true;
	// on success
	const res = await fetch('', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({ account: steps[1].data, oauth: steps[2].data })
	}).then((res) => res.json());

	console.log('res', res);

	if (res?.success) {
		sideLoadingIcon = false;
		currentStep++;
	} else {
		sideLoadingIcon = false;
		error_message = res?.message ?? 'Failed to configure sysconf';
	}
	// setTimeout(() => {
	// 	sideLoadingIcon = false;
	// 	currentStep++;
	// }, 2000);
}

function cancelSetup() {
	// on cancel
	if (confirm('Are you sure you want to cancel the setup?')) {
		window.location.reload();
	}
}

onMount(() => {
	if (browser) {
		document.cookie = 'fromPage=setup; path=/; max-age=600'; //
	}
});
onDestroy(() => {
	if (browser) {
		document.cookie = 'fromPage=setup; path=/; max-age=0 ';
	}
});
</script>

<!-- <PreDebug data={steps} /> -->

<Loading active={sideLoadingIcon} />

<div class="w-full h-screen flex flex-col items-center justify-center pb-16 select-none">
	<div class=" flex flex-col justify-between min-h-[20%] w-[40rem] bg-[#393939]">
		{#if error_message}
			<InlineNotification
				title="Error:"
				subtitle={error_message}
				hideCloseButton
				class="py-0 my-0 bg-[#393939] text-white  "
			/>
		{/if}
		<!-- <div class="bg-red-700">ERROR:  {error_message}</div> -->

		<div class=" py-2">
			<div class="flex items-center justify-between">
				{#if !(currentStep === steps.length - 1)}
					<h1 class="text-lg font-light text-white px-4 py-2">Setup Step: {currentStep + 1}</h1>
				{/if}
				<!-- <LoaderCircle class=" animate-spin h-6 w-6 mr-4 {!sideLoadingIcon ? 'hidden' : 'block'}" /> -->
			</div>
			<div class="w-full h-full p-4 py-4">
				<div class="title">
					<h1 class="text-4xl font-bold">{steps[currentStep].title}</h1>
					<p class="text-md">{steps[currentStep].description}</p>
					{#if steps[currentStep].component}
						<div class="py-3">
							{#if currentStep === 3}
								<Review account={steps[1]} oauth={steps[2]} />
							{:else}
								<svelte:component
									this={steps[currentStep].component}
									bind:data={steps[currentStep].data}
								/>
							{/if}
						</div>
					{/if}
				</div>
			</div>
		</div>

		<div class="flex justify-between w-full">
			{#if currentStep === 3}
				<!-- <Button class="w-full" kind="secondary" on:click={cancelSetup}>Cancel</Button> -->
				<Button
					class="w-full"
					kind="secondary"
					on:click={() => {
						currentStep = Math.max(0, currentStep - 1);
					}}
				>
					Back
				</Button><Button class="w-full " kind="primary" on:click={confirmSetup}>Confirm</Button>
			{:else if currentStep === 4}
				<Button
					kind="tertiary"
					class="w-full btn-full"
					style="max-width:unset;text-align:center;"
					on:click={() => {
						window.location.href = '/signin';
					}}
					>Go to login page
				</Button>
			{:else}
				<Button
					class="w-full"
					kind="secondary"
					on:click={() => {
						currentStep = Math.max(0, currentStep - 1);
					}}
				>
					Back
				</Button>

				<Button
					kind="primary"
					class="w-full"
					on:click={() => {
						currentStep = Math.min(steps.length - 1, currentStep + 1);
					}}
				>
					Next
				</Button>
			{/if}
		</div>
	</div>
</div>

<pre>

	{JSON.stringify(steps, null, 2)}
</pre>

<style>
</style>
