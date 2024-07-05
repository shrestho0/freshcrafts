<script lang="ts">
	import { Button } from 'carbon-components-svelte';
	import Welcome from './Welcome.svelte';

	const steps = [
		{
			title: 'Welcome',
			description:
				'Welcome to the setup wizard. This wizard will guide you through the setup process.',
			component: Welcome
		},
		{
			title: 'Account',
			description: 'Setup up your email password for the system.',
			component: Welcome
		},
		{
			title: 'OAuth',
			description: 'Setup OAuth accounts for signing in with Google, Github',
			component: Welcome
		},
		{
			title: 'Complete',
			description: 'Setup is complete. You can now login to your account.',
			component: Welcome
		}
	];
	let currentStep = 0;
</script>

<div class="w-full h-screen flex flex-col items-center justify-center pb-16 select-none">
	<div class=" flex flex-col justify-between min-h-[20%] w-[40rem] bg-[#393939]">
		<div class=" py-2">
			<h1 class="text-lg font-light text-white px-4 py-2">Setup Step: {currentStep + 1}</h1>
			<div class="w-full h-full p-4">
				<div class="title">
					<h1 class="text-4xl font-bold">{steps[currentStep].title}</h1>
					<p class="text-md">{steps[currentStep].description}</p>
					<Welcome bind:step={steps[0]} />
				</div>
			</div>
		</div>

		<div class="footer w-full">
			{#if currentStep === steps.length - 1}
				<Button
					class="w-full "
					kind="primary"
					on:click={() => {
						console.log('Setup complete');
					}}
				>
					Finish
				</Button>
			{:else}
				<div class="flex justify-between">
					<Button
						class="w-full"
						kind="secondary"
						on:click={() => {
							currentStep = Math.max(0, currentStep - 1);
						}}
					>
						Back
					</Button>

					<Button
						kind="primary"
						class="w-full"
						on:click={() => {
							currentStep = Math.min(steps.length - 1, currentStep + 1);
						}}
					>
						Next
					</Button>
				</div>
			{/if}
		</div>
	</div>
</div>
