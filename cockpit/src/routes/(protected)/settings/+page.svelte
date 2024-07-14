<script lang="ts">
	import { browser } from '$app/environment';
	import { enhance, applyAction } from '$app/forms';
	import { AuthProviderType } from '@/types/enums';
	import OAuth from '@/components/OAuth.svelte';
	import {
		Column,
		Row,
		TextInput,
		Tile,
		Button,
		Form,
		FormGroup,
		PasswordInput
	} from 'carbon-components-svelte';
	import { LogoGithub } from 'carbon-icons-svelte';
	import { onMount, onDestroy } from 'svelte';
	import type { SetupPageOauthData } from '@/types/internal';
	export let data;
	const oauth = {
		title: 'OAuth',
		description: 'Setup OAuth accounts for signing in with Google, Github',
		component: OAuth,
		data: {
			githubLoginUrl: data?.githubLoginUrl,
			googleLoginUrl: data?.googleLoginUrl,
			githubStatus: data.systemConfig.systemUserOauthGithubEnabled ? 'connected' : 'idle',
			googleStatus: data.systemConfig.systemUserOauthGoogleEnabled ? 'connected' : 'idle',
			githubOAuthJson: null,
			googleOAuthJson: null,
			oAuthGoogleEmail: '',
			oAuthGithubId: ''
		} as SetupPageOauthData
	};
	onMount(() => {
		if (browser) {
			document.cookie = 'fromPage=link; path=/; max-age=600'; //
		}
	});
	onDestroy(() => {
		if (browser) {
			document.cookie = 'fromPage=link; path=/; max-age=0 ';
		}
	});

	const { providers } = data?.providers ?? {};
	let userWantsToChangeEmail = false;
	let userWantsToChangePassword = false;

	function enhancedFormSubmission() {}

	async function connectionCallback({
		provider,
		githubId,
		googleEmail
	}: {
		provider: AuthProviderType;
		githubId?: string;
		googleEmail?: string;
	}) {
		console.log('connectedCallback', {
			provider,
			githubId,
			googleEmail
		});

		const res = await fetch('', {
			method: 'PATCH',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({
				provider,
				githubId,
				googleEmail
			})
		}).then((res) => res.json());

		console.log('response', res);
	}
	function disconnectionCallback({ provider }: { provider: AuthProviderType }) {
		console.log('disconnectionCallback', oauth.data);
	}
</script>

<div>
	<h1 class="text-3xl">Settings page</h1>
	<div class=" flex py-4">
		{#if providers?.includes(AuthProviderType.EMAIL_PASSWORD)}
			<Column>
				<h2 class="text-xl">Email Password Settings</h2>
				<Form method="post" class="py-3">
					<TextInput
						name="name"
						id="name"
						labelText="Name"
						value={data?.user?.name}
						disabled={!userWantsToChangeEmail}
					/>
					<TextInput
						name="email"
						id="email"
						labelText="Email"
						value={data?.user?.email}
						disabled={!userWantsToChangeEmail}
					/>
					{#if userWantsToChangeEmail}
						<div class="my-2">
							<Button
								class=" py-2.5"
								kind="secondary"
								type="button"
								size="small"
								on:click={() => (userWantsToChangeEmail = !userWantsToChangeEmail)}
								>Cancel Action</Button
							>
							<Button size="small" class="py-2.5" type="submit">Request Change</Button>
						</div>
					{:else}
						<Button
							type="button"
							kind="secondary"
							size="small"
							class="py-2.5 my-2"
							on:click={() => (userWantsToChangeEmail = !userWantsToChangeEmail)}
							>Change Info</Button
						>
					{/if}
				</Form>
				<Form method="post">
					{#if userWantsToChangePassword}
						<FormGroup>
							<PasswordInput name="oldPassword" id="oldPassword" labelText="Old Password" />
							<PasswordInput name="newPassword" id="newPassword" labelText="New Password" />
						</FormGroup>
						<Button
							kind="secondary"
							type="button"
							size="small"
							class="py-2.5"
							on:click={() => (userWantsToChangePassword = !userWantsToChangePassword)}
							>Cancel Action</Button
						>
						<Button size="small" class="py-2.5" type="submit">Request Password Change</Button>
					{:else}
						<FormGroup class="flex items-center gap-2">
							<Button
								type="button"
								kind="secondary"
								size="small"
								class="py-2.5"
								on:click={() => (userWantsToChangePassword = !userWantsToChangePassword)}
								>Change Password
							</Button>
						</FormGroup>
					{/if}
				</Form>
			</Column>
		{:else}
			<Column>
				<form method="post" action="?/m" use:enhance={enhancedFormSubmission}>
					<FormGroup>
						<TextInput class="w-full" name="x-email" type="email" labelText="Email" />
						<PasswordInput class="w-full" name="x-password" type="password" labelText="Password" />
					</FormGroup>
				</form>
			</Column>
		{/if}
	</div>
	<Tile class="">
		<h2 class="text-xl font-normal">OAuth Connections</h2>
		<Row class="flex gap-3 p-4">
			<OAuth {connectionCallback} {disconnectionCallback} bind:data={oauth.data} />
		</Row>
	</Tile>

	<pre>
[User email]
[User Password field]
[OAuth google connection status and button]
[OAuth github connection status and button]
{JSON.stringify(oauth, null, 2)}
    {JSON.stringify(data, null, 2)}
        </pre>
</div>
