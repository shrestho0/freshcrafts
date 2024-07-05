<script lang="ts">
	import '@/ui/css/app.css';
	import { Button, Select, SelectItem } from 'carbon-components-svelte';
	import { onMount } from 'svelte';
	import { Toaster, toast } from 'svelte-sonner';
</script>

<!-- <Select bind:selected={selectedTheme}>
	<SelectItem value="g100">Dark</SelectItem>
	<SelectItem value="white">Light</SelectItem>
</Select>
{selectedTheme} -->

<slot />

<Toaster
	toastOptions={{
		unstyled: true,

		classes: {
			toast: 'custom-toast',
			error: 'bg-red-400',
			success: 'text-green-400',
			warning: 'text-yellow-400',
			info: 'custom-toast--info'
			// info: 'bg-blue-400 text-[var(--cds-interactive-01)]'
		}
	}}
/>

<style>
	html[theme='g100'] > .custom-toast {
		background-color: var(----cds-interactive-01) !important;
		font-size: 30px;
	}
</style>
