<script lang="ts">
import { enhance } from '$app/forms';
import { invalidateAll } from '$app/navigation';
import type { EngineMySQLUpdateError } from '@/types/dtos';
import type { DBMysql } from '@/types/entities';
import { DBMysqlStatus } from '@/types/enums';

import type { ActionResult } from '@sveltejs/kit';
import {
	Button,
	ComposedModal,
	InlineNotification,
	ModalBody,
	ModalFooter,
	ModalHeader,
	PasswordInput,
	TextInput
} from 'carbon-components-svelte';
import { slide } from 'svelte/transition';

const newDBData = {
	newDBName: '',
	newDBUser: '',
	newUserPassword: '',
	reset: () => {
		newDBData.newDBName = '';
		newDBData.newDBUser = '';
		newDBData.newUserPassword = '';
	}
};

export let open: boolean;

let loading = false;

let errors: EngineMySQLUpdateError & {
	reset: () => void;
} = {
	newDBName: '',
	newDBUser: '',
	newUserPassword: '',
	reset: () => {
		errors.newDBName = '';
		errors.newDBUser = '';
		errors.newUserPassword = '';
	}
};

let error_message = '';

function handleFormSubmission() {
	return async ({ result }: { result: ActionResult }) => {
		console.log(result);
		loading = false;
		switch (result.type) {
			case 'success':
				open = false;
				invalidateAll();
				newDBData.reset();
				errors.reset();
				setTimeout(() => {
					// temporary fix as sse data comes before this thing is updated
					invalidateAll();
				}, 1000);
				break;
			case 'failure':
				loading = false;
				error_message = result?.data?.message;
				if (result?.data?.errors) {
					if (result?.data?.errors?.newDBName) {
						errors.newDBName = result?.data?.errors?.newDBName;
					}
					if (result?.data?.errors?.newDBUser) {
						errors.newDBUser = result?.data?.errors?.newDBUser;
					}
					if (result?.data?.errors?.newUserPassword) {
						errors.newUserPassword = result?.data?.errors?.newUserPassword;
					}
				}
				break;
		}
	};
}
</script>

<form
	action="?/update"
	method="post"
	class="flex flex-col justify-center gap-3"
	autocomplete="off"
	use:enhance={handleFormSubmission}
>
	<ComposedModal bind:open preventCloseOnClickOutside>
		<ModalHeader title="Create Database" closeClass="hidden" />
		<ModalBody class=" overflow-y-hidden">
			<div class="max-w-xl w-auto">
				<!-- <TextInput
					autocomplete="one-time-code"
					type="text"
					id="newDBUser"
					name="newDBUser"
					labelText="Database User"
					placeholder="newDBUser"
					minlength={3}
					bind:value={newDBData.newDBUser}
					invalid={Boolean(errors.newDBUser)}
					invalidText={errors.newDBUser}
					disabled={loading}
				/>

				<PasswordInput
					autocomplete="one-time-code"
					id="newUserPassword"
					name="newUserPassword"
					labelText="Database Password"
					placeholder="############"
					minlength={8}
					invalid={Boolean(errors.newUserPassword)}
					invalidText={errors.newUserPassword}
					bind:value={newDBData.newUserPassword}
					disabled={loading}
				/> -->
				<TextInput
					type="text"
					id="newDBName"
					name="newDBName"
					labelText="Database Name"
					placeholder="newDBName"
					minlength={3}
					bind:value={newDBData.newDBName}
					invalid={Boolean(errors.newDBName)}
					invalidText={errors.newDBName}
					disabled={loading}
				/>

				{#if error_message}
					<div transition:slide>
						<InlineNotification title={'Error'} subtitle={error_message} hideCloseButton />
					</div>
				{/if}
			</div>
		</ModalBody>

		<ModalFooter>
			<Button
				type="reset"
				kind="secondary"
				disabled={loading}
				on:click={() => {
					open = false;
					error_message = '';
					newDBData.reset();
					errors.reset();
				}}>Cancel</Button
			>
			<Button type="submit" kind="primary" disabled={loading}>Update Database Access</Button>
		</ModalFooter>
	</ComposedModal>
</form>
