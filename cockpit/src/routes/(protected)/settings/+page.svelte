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
		PasswordInput,
		InlineLoading
	} from 'carbon-components-svelte';
	import { LogoGithub } from 'carbon-icons-svelte';
	import { onMount, onDestroy } from 'svelte';
	import type { SetupPageOauthData } from '@/types/internal';
	import { toast } from 'svelte-sonner';
	import type { ActionResult } from '@sveltejs/kit';
	import { invalidateAll } from '$app/navigation';
	export let data;
	const oauth = {
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

	const { providers } = data?.providers ?? {};
	let userWantsToChangeEmail: 'idle' | 'loading' | 'editing' = 'idle';
	let userWantsToChangePassword: 'idle' | 'loading' | 'editing' = 'idle';

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
				update_type: 'oauth_connect',
				provider,
				githubId,
				googleEmail
			})
		}).then((res) => res.json());

		if (res?.success) {
			toast.success(provider + 'Connected successfully');
		} else {
			toast.error('Failed to connect with oauth provider.');
		}

		console.log('response', res);
	}
	async function disconnectionCallback({ provider }: { provider: AuthProviderType }) {
		console.log('disconnectionCallback', provider);

		const res = await fetch('', {
			method: 'PATCH',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({
				update_type: 'oauth_disconnect',
				provider
			})
		}).then((res) => res.json());

		console.log('response', res);

		if (res.success) {
			if (provider === AuthProviderType.OAUTH_GITHUB) {
				oauth.data.githubStatus = 'idle';
				oauth.data.githubOAuthJson = null;
				oauth.data.oAuthGithubId = '';
			} else if (provider === AuthProviderType.OAUTH_GOOGLE) {
				oauth.data.googleStatus = 'idle';
				oauth.data.googleOAuthJson = null;
				oauth.data.oAuthGoogleEmail = '';
			}
			toast.success(provider + ' disconnected successfully');
		} else {
			toast.error('Failed to disconnect');
		}
	}

	function enhancedFormSubmission() {
		return async ({ result }: { result: ActionResult }) => {
			switch (result.type) {
				case 'success':
					toast.success(result?.data?.message);
					applyAction(result);
					if (userWantsToChangeEmail) userWantsToChangeEmail = 'idle';
					if (userWantsToChangePassword) userWantsToChangePassword = 'idle';
					invalidateAll();
					break;
				case 'failure':
					toast.error(result?.data?.message);
					break;
				default:
					break;
			}
		};
	}

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
</script>

<div>
	<h1 class="text-3xl">Settings page</h1>
	<div class=" flex py-4">
		{#if providers?.includes(AuthProviderType.EMAIL_PASSWORD)}
			<Column>
				<h2 class="text-xl">Email Password Settings</h2>
				<form
					method="post"
					class="py-3 bx--form"
					action="?/changeInfo"
					use:enhance={enhancedFormSubmission}
				>
					<TextInput
						name="name"
						id="name"
						labelText="Name"
						value={data?.user?.name}
						disabled={userWantsToChangeEmail != 'editing'}
					/>
					<TextInput
						name="email"
						id="email"
						labelText="Email"
						value={data?.user?.email}
						disabled={userWantsToChangeEmail != 'editing'}
					/>
					{#if userWantsToChangeEmail == 'editing'}
						<div class="my-2">
							<Button
								class=" py-2.5"
								kind="secondary"
								type="button"
								size="small"
								on:click={() => {
									userWantsToChangeEmail = 'idle';
									invalidateAll();
								}}>Cancel Action</Button
							>
							<Button size="small" class="py-2.5" type="submit">Request Change</Button>
						</div>
					{:else if userWantsToChangeEmail === 'idle'}
						<Button
							type="button"
							kind="secondary"
							size="small"
							class="py-2.5 my-2"
							on:click={() => (userWantsToChangeEmail = 'editing')}>Change Info</Button
						>
					{:else if userWantsToChangeEmail === 'loading'}
						<InlineLoading description="Loading" />
					{/if}
				</form>
				<form
					method="post"
					class="py-3 bx--form"
					action="?/changePassword"
					use:enhance={enhancedFormSubmission}
				>
					{#if userWantsToChangePassword === 'editing'}
						<FormGroup>
							<PasswordInput name="oldPassword" id="oldPassword" labelText="Old Password" />
							<PasswordInput name="newPassword" id="newPassword" labelText="New Password" />
						</FormGroup>

						<Button
							kind="secondary"
							type="reset"
							size="small"
							class="py-2.5"
							on:click={() => (userWantsToChangePassword = 'idle')}>Cancel Action</Button
						>
						<Button size="small" class="py-2.5" type="submit">Request Password Change</Button>
					{:else}
						<FormGroup class="flex items-center gap-2">
							<Button
								type="button"
								kind="secondary"
								size="small"
								class="py-2.5"
								on:click={() => (userWantsToChangePassword = 'editing')}
								>Change Password
							</Button>
						</FormGroup>
					{/if}
				</form>
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
</div>

<!-- <pre>
	[User email]
	[User Password field]
	[OAuth google connection status and button]
	[OAuth github connection status and button]
	{JSON.stringify(oauth, null, 2)}
		{JSON.stringify(data, null, 2)}
			</pre> -->
