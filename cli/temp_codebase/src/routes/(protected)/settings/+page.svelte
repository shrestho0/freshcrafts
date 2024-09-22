<script lang="ts">
import { enhance } from '$app/forms';
import { invalidateAll } from '$app/navigation';
import Account from '@/components/Account.svelte';
import CommonOAuth from '@/components/CommonOAuth.svelte';
import PreDebug from '@/components/dev/PreDebug.svelte';
import ExpandableSection from '@/components/ExpandableSection.svelte';
import type { EngineCommonResponseDto, EngineSystemConfigResponseDto } from '@/types/dtos.js';
import CarbonLogoGithub from '@/ui/icons/CarbonLogoGithub.svelte';
import GoogleIcon from '@/ui/icons/GoogleIcon.svelte';
import type { ActionResult } from '@sveltejs/kit';
import {
	Button,
	ClickableTile,
	ExpandableTile,
	InlineNotification,
	PasswordInput,
	TextInput,
	Tile
} from 'carbon-components-svelte';
import { onMount } from 'svelte';
import { toast } from 'svelte-sonner';
export let data: {
	sysConf: EngineSystemConfigResponseDto;
	githubLoginUrl: string;
	googleLoginUrl: string;
	githubAppInstallUrl: string;
	azureChatApi: EngineCommonResponseDto<
		{
			azureChatApiEndpoint: string;
			azureChatApiKey: string;
		},
		null
	>;
};

let partialSysConf = {
	systemUserName: data?.sysConf?.systemUserName ?? '',
	systemUserEmail: data?.sysConf?.systemUserEmail ?? ''
	// systemUserPasswordHash: data?.sysConf?.systemUserPasswordHash ?? ''
} as Partial<EngineSystemConfigResponseDto>;
let errors: any = {
	userInfoUpdateError: '',
	passwordUpdateError: ''
};
function resetErrors() {
	errors = {
		userInfoUpdateError: '',
		passwordUpdateError: ''
	};
}

const expandables = {
	user_info: true,
	password_update: true,
	oauth_modify: true,
	azure_chat_api_modify: true
};

onMount(() => {});

function enhancedSaveUserInfo(type: 'userInfoUpdate' | 'passwordUpdate') {
	return async ({ result }: { result: ActionResult }) => {
		console.log('result', result);
		if (result.type == 'success') {
			if (type === 'passwordUpdate') {
				toast('Password updated successfully');
			} else if (type === 'userInfoUpdate') {
				toast('User info updated successfully');
			}
			invalidateAll();
		} else if (result.type == 'failure') {
			if (type === 'passwordUpdate') {
				errors.passwordUpdateError = result?.data?.message;
			} else if (type === 'userInfoUpdate') {
				errors.userInfoUpdateError = result?.data?.message;
			}
		}
	};
}

function enhancedAzureApiKeySave() {
	return async ({ result }: { result: ActionResult }) => {
		if (result.type == 'success') {
			toast('Azure Chat API saved successfully');
			invalidateAll();
		} else {
			toast('Failed to save Azure Chat API');
		}
	};
}
</script>

<section class="select-none">
	<ExpandableSection title="Update User Info" bind:open={expandables.user_info}>
		<form
			action="?/saveUserInfo"
			method="post"
			class="w-full"
			use:enhance={() => enhancedSaveUserInfo('userInfoUpdate')}
		>
			<TextInput
				size="xl"
				class="w-full"
				name="systemUserName"
				type="name"
				labelText="Name"
				bind:value={partialSysConf.systemUserName}
			/>
			<TextInput
				size="xl"
				class="w-full"
				name="systemUserEmail"
				type="email"
				labelText="Email"
				bind:value={partialSysConf.systemUserEmail}
			/>

			{#if errors.userInfoUpdateError}
				<InlineNotification
					kind="error"
					class="w-full"
					title="Error"
					subtitle={errors.userInfoUpdateError}
				/>
			{/if}

			<Button on:click={resetErrors} class="w-full my-3   " type="submit">Update</Button>
		</form>
	</ExpandableSection>

	<ExpandableSection title="Update Password" bind:open={expandables.password_update}>
		<form
			action="?/updatePassword"
			method="post"
			class="w-full"
			use:enhance={() => enhancedSaveUserInfo('passwordUpdate')}
		>
			<PasswordInput
				autocomplete="one-time-code"
				size="xl"
				class="w-full"
				name="oldPassword"
				required
				type="password"
				bind:value={partialSysConf.systemUserPasswordHash}
				labelText="Old Password"
			/>
			<PasswordInput
				autocomplete="one-time-code"
				size="xl"
				class="w-full"
				required
				name="newPassword"
				type="password"
				labelText="New Password"
			/>

			{#if errors.passwordUpdateError}
				<InlineNotification
					kind="error"
					class="w-full"
					title="Error"
					subtitle={errors.passwordUpdateError}
				/>
			{/if}

			<Button on:click={resetErrors} class="w-full my-3" type="submit">Update Password</Button>
		</form>
	</ExpandableSection>

	<ExpandableSection title="OAuth Connections" bind:open={expandables.oauth_modify}>
		{#key data?.sysConf}
			<div class="w-full flex flex-col gap-3 items-center">
				<CommonOAuth
					connTitle="Github"
					icon={CarbonLogoGithub}
					url={data?.githubAppInstallUrl}
					removeConnFormEndpoint="?/removeGithub"
					connEnabled={data?.sysConf?.systemUserOauthGithubEnabled}
				/>
				<CommonOAuth
					connTitle="Google"
					icon={GoogleIcon}
					url={data?.googleLoginUrl}
					removeConnFormEndpoint="?/removeGoogle"
					connEnabled={data?.sysConf.systemUserOauthGoogleEnabled}
				/>
			</div>
		{/key}
	</ExpandableSection>

	<ExpandableSection title="Azure Chat API" bind:open={expandables.azure_chat_api_modify}>
		{#if !data?.azureChatApi?.success}
			<InlineNotification
				kind="warning-alt"
				class="w-full"
				subtitle={data?.azureChatApi?.message ?? 'Failed to fetch Azure Chat API'}
			/>
		{/if}
		<form
			action="?/saveAzureChatApi"
			method="post"
			class="w-full"
			use:enhance={enhancedAzureApiKeySave}
		>
			<PasswordInput
				autocomplete="one-time-password"
				value={data?.azureChatApi?.payload?.azureChatApiEndpoint}
				size="xl"
				class="w-full"
				name="azureChatApiEndpoint"
				labelText="Azure Chat API Endpoint"
			/>
			<PasswordInput
				autocomplete="one-time-password"
				value={data?.azureChatApi?.payload?.azureChatApiKey}
				size="xl"
				class="w-full"
				name="azureChatApiKey"
				labelText="Azure Chat API Key"
			/>
			<Button class="w-full my-3" type="submit">Save</Button>
		</form>
	</ExpandableSection>

	<!-- <ClickableTile
		class="w-full flex items-center text-lg justify-between"
		on:click={() => {
			expandables.oauth_modify = !expandables.oauth_modify;
		}}
	>
		<h2>OAuth Connections</h2>
		{#if expandables.oauth_modify}
			<ChevronUp />
		{:else}
			<ChevronDown />
		{/if}
	</ClickableTile> -->
	<!-- 
	{#if expandables.oauth_modify}

	{/if} -->

	<PreDebug {data} />
</section>
