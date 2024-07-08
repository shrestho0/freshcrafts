<script>
	import { Header, SkipToContent, HeaderNav, HeaderUtilities } from 'carbon-components-svelte';
	import HeaderThemeSwitcher from './HeaderThemeSwitcher.svelte';
</script>

<Header platformName="FreshCrafts" href="/">
	<svelte:fragment slot="skip-to-content">
		<SkipToContent />
	</svelte:fragment>
	<HeaderNav></HeaderNav>
	<HeaderUtilities>
		<HeaderThemeSwitcher />
	</HeaderUtilities>
</Header>
