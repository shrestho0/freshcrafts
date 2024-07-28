<script lang="ts">
	import { enhance } from '$app/forms';
	import { goto, invalidateAll } from '$app/navigation';
	import PreDebug from '@/components/dev/PreDebug.svelte';
	import type { DBMysql } from '@/types/entities';
	import { DBMysqlStatus } from '@/types/enums';
	import type { ActionResult } from '@sveltejs/kit';
	import {
		Button,
		ComposedModal,
		ModalBody,
		ModalFooter,
		ModalHeader
	} from 'carbon-components-svelte';

	export let db: DBMysql;
	export let open: boolean;

	function enhancedFormSubmission() {
		// FIXME: Errors not handled
		return async ({ result }: { result: ActionResult }) => {
			console.log('[DEBUG]: enhancedFormSubmission:', result);
			switch (result.type) {
				case 'success':
					db.status = DBMysqlStatus.PENDING_DELETE;

					const timeout = setTimeout(() => {
						// window.location.href = '/databases/mysql';
						goto('/databases/mysql');
						clearTimeout(timeout);
					}, 1000);

					break;
				default:
					break;
			}
		};
	}
</script>

<ComposedModal bind:open class="select-none" preventCloseOnClickOutside>
	<ModalHeader title="Delete Database" closeClass="hidden" />
	<ModalBody>
		<p>Are you sure you want to delete this database?</p>
		<p>This action cannot be undone.</p>
		<PreDebug data={{ db }} title="" />
	</ModalBody>
	<ModalFooter class="w-full grid grid-cols-2">
		<Button
			kind="secondary"
			on:click={() => {
				open = false;
			}}>Cancel</Button
		>
		<form action="?/delete" method="post" use:enhance={enhancedFormSubmission}>
			<Button
				type="submit"
				class="w-full"
				kind="danger"
				on:click={() => {
					console.log('Delete');
					// do something then close
					open = false;
				}}>Delete</Button
			>
		</form>
	</ModalFooter>
</ComposedModal>
