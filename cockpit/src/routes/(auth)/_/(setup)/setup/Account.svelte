<script lang="ts">
	import type { SetupPageAccountData } from '@/types/internal';
	import { Column, PasswordInput, Row, TextInput } from 'carbon-components-svelte';

	export let data: SetupPageAccountData;
</script>

<Row>
	<Column class="flex flex-col gap-2">
		<TextInput
			size="xl"
			class="w-full"
			name="x-name"
			type="name"
			labelText="Name"
			bind:value={data.name}
		/>
		<TextInput
			size="xl"
			class="w-full"
			name="x-email"
			type="email"
			labelText="Email"
			bind:value={data.email}
		/>

		<PasswordInput
			size="xl"
			class="w-full"
			name="x-password"
			type="password"
			bind:value={data.password}
			labelText="Password"
		/>
	</Column>
</Row>

<style>
	.setup-form {
		width: 100%;
	}
</style>
