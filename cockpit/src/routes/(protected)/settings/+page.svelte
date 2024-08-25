<script lang="ts">
import { enhance } from '$app/forms';
import { invalidateAll } from '$app/navigation';
import Account from '@/components/Account.svelte';
import CommonOAuth from '@/components/CommonOAuth.svelte';
import PreDebug from '@/components/dev/PreDebug.svelte';
import type { EngineSystemConfigResponseDto } from '@/types/dtos.js';
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
import { ArrowDown, ChevronDown, ChevronUp, LogoGithub } from 'carbon-icons-svelte';
import { onMount } from 'svelte';
import { toast } from 'svelte-sonner';
export let data: {
	sysConf: EngineSystemConfigResponseDto;
	githubLoginUrl: string;
	googleLoginUrl: string;
	githubAppInstallUrl: string;
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
	oauth_modify: true
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
</script>

<section class="select-none">
	<ClickableTile
		class="w-full flex items-center text-lg justify-between"
		on:click={() => {
			expandables.user_info = !expandables.user_info;
		}}
	>
		<h2>Update User Info</h2>
		{#if expandables.user_info}
			<ChevronUp />
		{:else}
			<ChevronDown />
		{/if}</ClickableTile
	>
	{#if expandables.user_info}
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
	{/if}

	<ClickableTile
		class="w-full flex items-center text-lg justify-between"
		on:click={() => {
			expandables.password_update = !expandables.password_update;
		}}
	>
		<h2>Update Password</h2>
		{#if expandables.password_update}
			<ChevronUp />
		{:else}
			<ChevronDown />
		{/if}
	</ClickableTile>
	{#if expandables.password_update}
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
	{/if}

	<ClickableTile
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
	</ClickableTile>

	{#if expandables.oauth_modify}
		{#key data?.sysConf}
			<div class="w-full flex flex-col gap-3 items-center">
				<CommonOAuth
					connTitle="Github"
					icon={LogoGithub}
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
	{/if}

	<PreDebug {data} />
</section>
