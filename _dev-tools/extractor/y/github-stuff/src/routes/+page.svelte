<script lang="ts">
	import { browser } from '$app/environment';

	import { PUBLIC_GITHUB_CLIENT_ID } from '$env/static/public';
	import { ulid } from 'ulid';

	export let data;

	let github_base_url = 'https://github.com/login/oauth/authorize';
	let github_login_url = new URL(github_base_url);
	github_login_url.searchParams.append('client_id', PUBLIC_GITHUB_CLIENT_ID);
	github_login_url.searchParams.append(
		'redirect_uri',
		'http://localhost:5173/oauth-callback?p=github'
	);
	github_login_url.searchParams.append('scope', 'user:email repo workflow');
	const state = ulid(); // request state to verify later

	github_login_url.searchParams.append('state', state);

	if (browser) {
		document.cookie = `gh_state=${state}; path=/;`;
	}
</script>

{#if data?.user}
	<pre>{JSON.stringify(data.user, null, 2)}</pre>
	Repos: ({data.repos.length})
	<ul>
		{#each data.repos as repo}
			<!-- {#if repo.owner.login == data.user.login} -->
			<details>
				<summary>
					{repo.name}
				</summary>
				<pre>{JSON.stringify(repo, null, 2)}</pre>
			</details>
			<!-- {/if} -->
		{/each}
	</ul>
{:else}
	<a href={github_login_url.toString()}> Login with github </a>
{/if}
