<script lang="ts">
	import { goto } from "$app/navigation";
	import { page } from "$app/stores";
	import type {
		EngineCommonResponseDto,
		EngineRedisCreateError,
		EngineRedisCreatePayload,
	} from "@/types/dtos";
	import {
		Button,
		Form,
		FormItem,
		Loading,
		PasswordInput,
		TextInput,
		InlineNotification,
	} from "carbon-components-svelte";

	const newDBData = {
		dbPrefix: "",
		username: "",
		password: "",
	};

	let loading = false;

	let errors: EngineRedisCreateError = {
		dbPrefix: "",
		username: "",
		password: "",
	};

	let error_message = "";

	async function handleFormSubmission() {
		console.log(newDBData);
		loading = true;
		error_message = "";
		const res = (await fetch("", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify(newDBData),
		}).then((res) => res.json())) as EngineCommonResponseDto<
			EngineRedisCreatePayload,
			EngineRedisCreateError
		>;

		if (res.success) {
			// redirect to the new database page. /mongodb/:id
			let url = $page.url.href;
			url = url.split("/").slice(0, -1).join("/");
			url += `/${res.payload.id}`;
			goto(url);
		} else {
			// show error message
			if (res?.message) error_message = res.message;
			if (res?.errors) errors = res.errors;
		}

		loading = false;
		console.log(res);
		JSON.parse("{}");
	}
</script>

<h2 class="text-2xl pb-4">New Redis</h2>

<div class="max-w-lg">
	{#each Array(20) as _, i}
		<Button
			on:click={() => {
				newDBData.dbPrefix = `test__${i}`;
				newDBData.username = `test__${i}`;
				newDBData.password = `test__${i}`;
			}}>test__{i}</Button
		>
	{/each}
	<form
		method="post"
		class="flex flex-col justify-center gap-3"
		autocomplete="off"
		on:submit|preventDefault={handleFormSubmission}
	>
		<TextInput
			required
			type="text"
			id="db_name"
			name="db_name"
			labelText="Database Prefix"
			placeholder="db_name"
			minlength={3}
			bind:value={newDBData.dbPrefix}
			invalid={Boolean(errors.dbPrefix)}
			invalidText={errors.dbPrefix}
			disabled={loading}
		/>
		<TextInput
			required
			type="text"
			id="db_user"
			name="db_user"
			labelText="Username"
			placeholder="db_user"
			minlength={3}
			bind:value={newDBData.username}
			invalid={Boolean(errors.username)}
			invalidText={errors.username}
			disabled={loading}
		/>
		<!-- one-time-code prevents from password save prompt -->
		<PasswordInput
			required
			autocomplete="one-time-code"
			id="db_pass"
			name="db_pass"
			labelText="Password"
			placeholder="########"
			minlength={8}
			invalid={Boolean(errors.password)}
			invalidText={errors.password}
			bind:value={newDBData.password}
			disabled={loading}
		/>

		{#if error_message}
			<InlineNotification title={"Error"} subtitle={error_message} />
		{/if}

		<div class="w-full">
			<Button type="reset" kind="secondary" disabled={loading}
				>Reset</Button
			>
			<Button type="submit" kind="primary" disabled={loading}
				>Create Database</Button
			>
		</div>
	</form>
</div>
{#if loading}
	<Loading />
{/if}
