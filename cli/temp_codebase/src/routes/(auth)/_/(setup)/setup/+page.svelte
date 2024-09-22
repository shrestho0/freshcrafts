<script lang="ts">
import { toTitleCase } from '@/utils/utils';
import { Button, InlineNotification, Loading, InlineLoading } from 'carbon-components-svelte';
import { onMount } from 'svelte';
import Account from '../../../../../lib/components/Account.svelte';
import type { SetupPageAccountData, SetupPageOauthData } from '@/types/internal';
import PreDebug from '@/components/dev/PreDebug.svelte';
import type { EngineSystemConfigResponseDto } from '@/types/dtos';
import CommonOAuth from '@/components/CommonOAuth.svelte';
import { page } from '$app/stores';
import {
	PUBLIC_GITHUB_APP_INSTALLATION_URL,
	PUBLIC_GITHUB_OAUTH_CALLBACK_URL,
	PUBLIC_GOOGLE_OAUTH_CALLBACK_URL
} from '$env/static/public';
import GoogleIcon from '@/ui/icons/GoogleIcon.svelte';
import Review from './Review.svelte';
import { enhance } from '$app/forms';
import type { ActionResult } from '@sveltejs/kit';
import { browser } from '$app/environment';
import CarbonLogoGithub from '@/ui/icons/CarbonLogoGithub.svelte';
const steps = [
	{
		key: 'welcome',
		title: 'Welcome to FreshCrafts',
		description:
			'Welcome to the setup wizard. This wizard will guide you through the setup process.',

		prevButtonText: 'Cancel',
		prevButtonCallback: () => {
			window.location.href = '/';
		},
		nextButtonText: 'Get Started',
		nextButtonCallback: () => {
			nextStep();
		}
	},

	{
		key: 'oauth',
		title: 'OAuth',
		description: 'Setup OAuth for your system. Github repos permission will be set from here',
		prevButtonText: 'Back',
		prevButtonCallback: () => {
			prevStep();
		},
		nextButtonText: 'Next',
		nextButtonCallback: () => {
			nextStep();
		}
	},
	{
		key: 'account',
		title: 'Account',
		description: 'Setup up your email password for the system.',
		prevButtonText: 'Back',
		prevButtonCallback: () => {
			prevStep();
		},
		nextButtonText: 'Next',
		nextButtonCallback: () => {
			nextStep();
		}
	},
	{
		key: 'review',
		title: 'Review',
		description: 'Review your setup before finalizing',
		prevButtonText: 'Back',
		prevButtonCallback: () => {
			prevStep();
		}
	}
];

let currentStepIdx = 0;
let stepLoading = true;
let stepErrorMessage = '';
let errors = {
	systemUserEmail: '',
	systemUserName: '',
	systemUserPasswordHash: ''
} as any;
$: currentStep = steps[currentStepIdx];

export let data;
let sysConf: EngineSystemConfigResponseDto;
$: {
	if (browser) {
		sysConf = data?.sysConf as EngineSystemConfigResponseDto;
		console.log('sysConf', sysConf);
	}
}

// $: {
// 	sysConf = data?.sysConf as EngineSystemConfigResponseDto;
// }

onMount(() => {
	const paramStep = $page.url.searchParams.get('step');
	if (paramStep) {
		const stepIdx = findIndexByKey(paramStep as keyof typeof steps);
		if (stepIdx !== -1) {
			currentStepIdx = stepIdx;
		}

		// remove the query param
		history.replaceState({}, '', $page.url.pathname);
	}
	stepLoading = false;
});

function findIndexByKey(key: keyof typeof steps) {
	return steps.findIndex((step) => step.key === key);
}
function hasNextStep() {
	return currentStepIdx < steps.length - 1;
}
function hasPrevStep() {
	return currentStepIdx > 0;
}
function prevStep() {
	if (hasPrevStep()) {
		currentStepIdx--;
	}
}
function nextStep() {
	if (hasNextStep()) {
		currentStepIdx++;
	}
}

function enhanceUserPassSubmission() {
	return async ({ result }: { result: ActionResult }) => {
		Object.keys(errors).forEach((key) => {
			errors[key] = '';
		});

		if (result.type === 'success') {
			console.log('success');
			// nextStep();
			window.location.href = '/';
		} else if (result.type === 'failure') {
			console.log('failure', result?.data);
			errors = result?.data ?? {
				systemUserEmail: '',
				systemUserName: '',
				systemUserPasswordHash: ''
			};
			// stepErrorMessage = result?.data?.message ?? 'An error occurred';
			// window.history.pushState({}, '', '/_/setup?step=account');

			currentStepIdx = 1;
		}
	};
}
</script>

<Loading active={stepLoading} />
<div class="w-full h-screen flex flex-col items-center justify-center pb-16">
	<div class=" flex flex-col justify-between min-h-[20%] w-[40rem] bg-[#393939]">
		{#if stepErrorMessage}
			<InlineNotification
				title="Error:"
				subtitle={stepErrorMessage}
				hideCloseButton
				class="py-0 my-0 bg-[#393939] text-white  "
			/>
		{/if}
		<!-- <div class="bg-red-700">ERROR:  {error_message}</div> -->

		<div class=" py-2">
			<div class="flex items-center justify-between">
				<!-- {#if !(currentStep === steps.length - 1)} -->
				<h1 class="text-lg font-light text-white px-4 py-2">
					Setup Step: {currentStepIdx + 1}
				</h1>
				<!-- {/if} -->
				<!-- <LoaderCircle class=" animate-spin h-6 w-6 mr-4 {!sideLoadingIcon ? 'hidden' : 'block'}" /> -->
			</div>
			<div class="w-full h-full p-4 py-4">
				<div class="title">
					<h1 class="text-4xl font-bold">{currentStep.title}</h1>
					<p class="text-md">{currentStep.description}</p>
					<!-- {#if steps[currentStep].component} -->
					<div class="py-3">
						{#if currentStep?.key === 'account'}
							<Account bind:data={sysConf} bind:errors />
						{:else if currentStep?.key === 'oauth'}
							<div class="flex flex-col gap-3">
								{#key sysConf}
									<CommonOAuth
										connTitle="Github"
										icon={CarbonLogoGithub}
										url={data?.githubAppInstallUrl}
										removeConnFormEndpoint="?/removeGithub"
										connEnabled={sysConf.systemUserOauthGithubEnabled}
									/>

									<CommonOAuth
										connTitle="Google"
										icon={GoogleIcon}
										url={data?.googleLoginUrl}
										removeConnFormEndpoint="?/removeGoogle"
										connEnabled={sysConf.systemUserOauthGoogleEnabled}
									/>
								{/key}
							</div>
						{:else if currentStep?.key == 'review'}
							<Review sysConfig={sysConf} />
						{/if}
					</div>
					<!-- {#if currentStep === 3}
								<Review account={steps[1]} oauth={steps[2]} />
							{:else if currentStep === 2}
								<CommonOAuth />
							{:else}
								<svelte:component
									this={steps[currentStep].component}
									bind:data={steps[currentStep].data}
								/>
							{/if} -->
					<!-- {/if} -->
				</div>
			</div>
		</div>

		<div class="flex justify-between w-full">
			{#if currentStep && currentStep?.prevButtonText && currentStep?.prevButtonCallback}
				<Button kind="secondary" class="w-full" on:click={currentStep.prevButtonCallback}>
					{currentStep.prevButtonText}
				</Button>
			{/if}

			{#if currentStep && currentStep?.nextButtonText && currentStep?.nextButtonCallback}
				<Button kind="primary" class="w-full " on:click={currentStep.nextButtonCallback}>
					{currentStep.nextButtonText}
				</Button>
			{/if}

			{#if currentStep && currentStep?.key == 'review'}
				<form
					method="post"
					action="?/saveUserInfo"
					class="w-full"
					on:submit={() => {
						errors = {
							systemUserEmail: '',
							systemUserName: '',
							systemUserPasswordHash: ''
						};
					}}
					use:enhance={enhanceUserPassSubmission}
				>
					<input type="hidden" name="systemUserEmail" value={sysConf.systemUserEmail} />
					<input type="hidden" name="systemUserName" value={sysConf.systemUserName} />
					<input
						type="hidden"
						name="systemUserPasswordHash"
						value={sysConf.systemUserPasswordHash}
					/>
					<Button kind="primary" class="w-full " type="submit">Finalize Setup</Button>
				</form>
			{/if}
		</div>
	</div>
</div>

<!-- <PreDebug classes="bg-black text-white" {data}></PreDebug> -->
