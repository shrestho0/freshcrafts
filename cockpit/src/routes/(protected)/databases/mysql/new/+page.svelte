<script lang="ts">
	import {
		Button,
		Form,
		FormItem,
		Loading,
		PasswordInput,
		TextInput
	} from 'carbon-components-svelte';

	const newDBData = {
		dbName: '',
		dbUser: '',
		dbPassword: ''
	};

	let loading = false;
	let error_message = '';
	async function handleFormSubmission() {
		console.log(newDBData);
		loading = true;
		error_message = '';
		const res = (await fetch('', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(newDBData)
		}).then((res) => res.json())) as {
			success: boolean;
			message?: string;
		};
		if (res.success) {
			// redirect to the new database page. /mysql/:id
		} else {
			// show error message
			error_message = res?.message ? res.message : 'An error occurred';
		}
		loading = false;
		console.log(res);
	}
</script>

<h2 class="text-2xl pb-4">New MySQL Database</h2>

<div class="max-w-lg">
	<form
		method="post"
		class="flex flex-col justify-center gap-3"
		autocomplete="off"
		on:submit|preventDefault={handleFormSubmission}
	>
		<TextInput
			required
			autocomplete="new-name"
			type="text"
			id="db_name"
			name="db_name"
			labelText="Database Name"
			placeholder="db_name"
			bind:value={newDBData.dbName}
			invalid={error_message?.includes('name')}
			invalidText={error_message}
			disabled={loading}
		/>
		<TextInput
			required
			autocomplete="new-user"
			type="text"
			id="db_user"
			name="db_user"
			labelText="Database User"
			placeholder="db_user"
			bind:value={newDBData.dbUser}
			invalid={error_message?.includes('user')}
			invalidText={error_message}
			disabled={loading}
		/>
		<PasswordInput
			required
			autocomplete="new-password"
			id="db_pass"
			name="db_pass"
			labelText="Database Password"
			placeholder="########"
			minlength={8}
			bind:value={newDBData.dbPassword}
			disabled={loading}
		/>

		<!-- {#if error_message}
			<p class="text-red-500">{error_message}</p>
		{/if} -->

		<div class="w-full">
			<Button type="reset" kind="secondary" disabled={loading}>Reset</Button>
			<Button type="submit" kind="primary" disabled={loading}>Create Database</Button>
		</div>
	</form>
</div>
{#if loading}
	<Loading />
{/if}
