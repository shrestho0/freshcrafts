<script lang="ts">
import type { EngineSystemConfigResponseDto } from '@/types/dtos';
import { Column, PasswordInput, Row, TextInput } from 'carbon-components-svelte';

export let data: Partial<EngineSystemConfigResponseDto> | undefined;
export let errors = {
	systemUserName: '',
	systemUserEmail: '',
	systemUserPasswordHash: ''
};
console.log('errrors', errors);
</script>

{#if data}
	<Row>
		<Column class="w-full flex flex-col gap-2">
			<TextInput
				size="xl"
				class="w-full"
				name="systemUserName"
				type="name"
				labelText="Name"
				bind:value={data.systemUserName}
			/>
			<TextInput
				size="xl"
				class="w-full"
				name="systemUserEmail"
				type="email"
				labelText="Email"
				invalid={Boolean(errors?.systemUserEmail)}
				invalidText={errors?.systemUserEmail}
				bind:value={data.systemUserEmail}
			/>

			<PasswordInput
				autocomplete="one-time-code"
				size="xl"
				class="w-full"
				name="systemUserPasswordHash"
				type="password"
				invalid={Boolean(errors?.systemUserPasswordHash)}
				invalidText={errors?.systemUserPasswordHash}
				bind:value={data.systemUserPasswordHash}
				labelText="Password"
			/>
		</Column>
	</Row>
{:else}
	Data is not defined
{/if}
<!-- 
<style>
.setup-form {
	width: 100%;
}
</style> -->
