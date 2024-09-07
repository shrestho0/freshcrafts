<script lang="ts">
import { browser } from '$app/environment';
import CarbonAsleepFilled from '@/ui/icons/CarbonAsleepFilled.svelte';
import CarbonBrightnessContrast from '@/ui/icons/CarbonBrightnessContrast.svelte';
import { HeaderGlobalAction, SkeletonPlaceholder } from 'carbon-components-svelte';
import { onMount } from 'svelte';

let loading = true;
let selectedTheme: string;
let themeIcon: any;

$: themeIcon = selectedTheme === 'g100' ? CarbonBrightnessContrast : CarbonAsleepFilled;

onMount(() => {
	if (!selectedTheme) {
		selectedTheme = localStorage.getItem('theme') || 'g100';
	}

	// subscribe to localStorage theme change
	window.addEventListener('storage', (e) => {
		if (e.key === 'theme') {
			if (e.newValue) {
				selectedTheme = e.newValue?.toString();
			}
		}
	});

	loading = false;
});

$: {
	if (browser) {
		document.documentElement.setAttribute('theme', selectedTheme);
		document.documentElement.setAttribute(
			'data-theme',
			selectedTheme === 'g100' ? 'dark' : 'light'
		);
	}
}
</script>

{#if !loading}
	<HeaderGlobalAction
		iconDescription="Toggle Mode"
		tooltipAlignment="start"
		bind:icon={themeIcon}
		on:click={() => {
			// localStorage.setItem('theme', selectedTheme === 'g100' ? 'white' : 'g100');
			selectedTheme = selectedTheme === 'g100' ? 'white' : 'g100';
			localStorage.setItem('theme', selectedTheme);
		}}
	/>
{/if}
