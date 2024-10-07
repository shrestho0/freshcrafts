<script lang="ts">
	import { goto } from "$app/navigation";
	import { page } from "$app/stores";
	import type {
		EngineCommonResponseDto,
		EngineMySQLCreateError,
		EngineMySQLCreatePayload,
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
	import { slide } from "svelte/transition";

	const newDBData = {
		dbName: "",
		dbUser: "",
		dbPassword: "",
	};

	let loading = false;

	let errors: EngineMySQLCreateError = {
		dbName: "",
		dbUser: "",
		dbPassword: "",
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
			EngineMySQLCreatePayload,
			EngineMySQLCreateError
		>;
		// as {
		// 	success: boolean;
		// 	message?: string;
		// 	payload: {
		// 		id: '01J3FM5SKADJ81V6CYH1FFB694';
		// 		dbName: 'test__3';
		// 		dbUser: 'test__3';
		// 		dbPassword: 'test__3';
		// 		status: null;
		// 		reasonFailed: null;
		// 	};
		// 	errors: {
		// 		dbName: string;
		// 		dbUser: string;
		// 		dbPassword: string;
		// 	};
		// };

		if (res.success) {
			// redirect to the new database page. /mysql/:id
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

	let showDummies = false;
</script>

<h2 class="text-2xl pb-4">New MySQL Database</h2>

<div class="max-w-lg">
	<div class="dummy-data-section">
		<button
			class="text-sm text-gray-500 underline"
			on:click={() => {
				showDummies = !showDummies;
			}}
		>
			{showDummies ? "Hide" : "Show"} dummy data
		</button>
		<div class="">
			{#if showDummies}
				{#each Array(20) as _, i}
					<Button
						on:click={() => {
							newDBData.dbName = `test__${i}`;
							newDBData.dbUser = `test__${i}`;
							newDBData.dbPassword = `test__${i}`;
						}}>test__{i}</Button
					>
				{/each}
			{/if}
		</div>
	</div>
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
			labelText="Database Name"
			placeholder="db_name"
			minlength={3}
			bind:value={newDBData.dbName}
			invalid={Boolean(errors.dbName)}
			invalidText={errors.dbName}
			disabled={loading}
		/>
		<TextInput
			required
			type="text"
			id="db_user"
			name="db_user"
			labelText="Database User"
			placeholder="db_user"
			minlength={3}
			bind:value={newDBData.dbUser}
			invalid={Boolean(errors.dbUser)}
			invalidText={errors.dbUser}
			disabled={loading}
		/>
		<!-- one-time-code prevents from password save prompt -->
		<PasswordInput
			required
			autocomplete="one-time-code"
			id="db_pass"
			name="db_pass"
			labelText="Database Password"
			placeholder="########"
			minlength={8}
			invalid={Boolean(errors.dbPassword)}
			invalidText={errors.dbPassword}
			bind:value={newDBData.dbPassword}
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
